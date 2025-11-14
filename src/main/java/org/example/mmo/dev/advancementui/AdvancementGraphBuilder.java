package org.example.mmo.dev.advancementui;

import net.kyori.adventure.text.Component;
import net.minestom.server.advancements.FrameType;
import net.minestom.server.item.Material;
import org.example.mmo.dev.advancementui.definition.SkillNodeDefinition;
import org.example.mmo.dev.advancementui.definition.SkillTreeDefinition;
import org.example.mmo.dev.advancementui.definition.SkillTreeDisplayDefinition;
import org.example.mmo.dev.advancementui.definition.SkillTreeLayoutConstraints;
import org.example.mmo.dev.advancementui.model.SkillNodePrototype;
import org.example.mmo.dev.advancementui.model.SkillTreeDisplayPrototype;
import org.example.mmo.dev.advancementui.model.SkillTreePrototype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Transforme une {@link SkillTreeDefinition} en {@link SkillTreePrototype} prêt à être instancié.
 */
public final class AdvancementGraphBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdvancementGraphBuilder.class);

    public SkillTreePrototype build(SkillTreeDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        SkillTreeDisplayPrototype displayPrototype = toDisplayPrototype(definition);

        Map<String, SkillNodePrototype> nodeMap = new LinkedHashMap<>();
        for (SkillNodeDefinition node : definition.nodes()) {
            populateNodeMap(definition.id(), node, null, nodeMap);
        }

        validateParentLinks(definition.id(), nodeMap);
        nodeMap = applyAutoLayoutIfNeeded(definition.id(), nodeMap, definition.layout());
        validateLayout(definition.id(), definition.layout(), nodeMap);

        return new SkillTreePrototype(definition.id(), displayPrototype, Map.copyOf(nodeMap), definition.layout());
    }

    private SkillTreeDisplayPrototype toDisplayPrototype(SkillTreeDefinition definition) {
        SkillTreeDisplayDefinition display = definition.display();
        Component title = Component.text(display.title() != null ? display.title() : definition.id());
        Component description = Component.text(display.description() != null ? display.description() : "");
        Material icon = resolveMaterial(display.icon(), "display icon of " + definition.id());
        FrameType frameType = resolveFrame(display.frameType(), FrameType.TASK);
        return new SkillTreeDisplayPrototype(title, description, icon, frameType,
                display.x(), display.y(), display.background());
    }

    private void populateNodeMap(String treeId, SkillNodeDefinition node, String parentId,
                                 Map<String, SkillNodePrototype> nodeMap) {
        SkillNodePrototype prototype = toNodePrototype(treeId, node, parentId);
        if (nodeMap.putIfAbsent(prototype.id(), prototype) != null) {
            throw new IllegalStateException("Duplicate node id '" + prototype.id() + "' in tree " + treeId);
        }
        for (SkillNodeDefinition child : node.children()) {
            populateNodeMap(treeId, child, prototype.id(), nodeMap);
        }
    }

    private SkillNodePrototype toNodePrototype(String treeId, SkillNodeDefinition node, String parentOverride) {
        if (node.id() == null || node.id().isBlank()) {
            throw new IllegalStateException("Node without id in tree " + treeId);
        }
        Component title = Component.text(node.title() != null ? node.title() : node.id());
        Component description = Component.text(node.description() != null ? node.description() : "");
        Material icon = resolveMaterial(node.icon(), "icon for node " + node.id() + " in tree " + treeId);
        FrameType frameType = resolveFrame(node.frameType(), FrameType.TASK);
        Map<String, Object> metadata = node.metadata() == null ? Map.of() : Map.copyOf(node.metadata());
        String parentId = parentOverride != null ? parentOverride : node.parentId();
        return new SkillNodePrototype(
                node.id(),
                title,
                description,
                icon,
                frameType,
                node.x(),
                node.y(),
                node.toast(),
                node.hidden(),
                node.secret(),
                parentId,
                metadata
        );
    }

    private void validateParentLinks(String treeId, Map<String, SkillNodePrototype> nodeMap) {
        for (SkillNodePrototype prototype : nodeMap.values()) {
            if (prototype.parentId() == null) {
                continue;
            }
            if (!nodeMap.containsKey(prototype.parentId())) {
                throw new IllegalStateException("Node '" + prototype.id() + "' in tree '" + treeId
                        + "' references missing parent '" + prototype.parentId() + "'");
            }
        }
    }

    private void validateLayout(String treeId, SkillTreeLayoutConstraints constraints,
                                Map<String, SkillNodePrototype> nodeMap) {
        if (constraints == null) {
            return;
        }
        if (!Boolean.TRUE.equals(constraints.autoArrange())) {
            for (SkillNodePrototype prototype : nodeMap.values()) {
                if (constraints.minX() != null && prototype.x() < constraints.minX()) {
                    throw new IllegalStateException("Node '" + prototype.id() + "' in tree '" + treeId
                            + "' has X position " + prototype.x() + " < minX " + constraints.minX());
                }
                if (constraints.maxX() != null && prototype.x() > constraints.maxX()) {
                    throw new IllegalStateException("Node '" + prototype.id() + "' in tree '" + treeId
                            + "' has X position " + prototype.x() + " > maxX " + constraints.maxX());
                }
                if (constraints.minY() != null && prototype.y() < constraints.minY()) {
                    throw new IllegalStateException("Node '" + prototype.id() + "' in tree '" + treeId
                            + "' has Y position " + prototype.y() + " < minY " + constraints.minY());
                }
                if (constraints.maxY() != null && prototype.y() > constraints.maxY()) {
                    throw new IllegalStateException("Node '" + prototype.id() + "' in tree '" + treeId
                            + "' has Y position " + prototype.y() + " > maxY " + constraints.maxY());
                }
            }
        }

        Float minDistance = constraints.minDistance();
        if (minDistance == null || minDistance <= 0) {
            return;
        }

        float minDistanceSq = minDistance * minDistance;
        SkillNodePrototype[] prototypes = nodeMap.values().toArray(SkillNodePrototype[]::new);
        for (int i = 0; i < prototypes.length; i++) {
            SkillNodePrototype a = prototypes[i];
            for (int j = i + 1; j < prototypes.length; j++) {
                SkillNodePrototype b = prototypes[j];
                float dx = a.x() - b.x();
                float dy = a.y() - b.y();
                if ((dx * dx + dy * dy) < minDistanceSq) {
                    LOGGER.warn("Nodes '{}' and '{}' in tree '{}' are closer than minDistance {}",
                            a.id(), b.id(), treeId, minDistance);
                }
            }
        }
    }

    private FrameType resolveFrame(String raw, FrameType fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return FrameType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Unknown FrameType '{}', defaulting to {}", raw, fallback);
            return fallback;
        }
    }

    private Material resolveMaterial(String raw, String context) {
        if (raw == null || raw.isBlank()) {
            return Material.STONE;
        }

        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        String key = normalized.contains(":") ? normalized : "minecraft:" + normalized;
        Material material = Material.fromKey(key);
        if (material != null) {
            return material;
        }

        for (Material candidate : Material.values()) {
            if (candidate.key().value().equalsIgnoreCase(raw) || candidate.key().asString().equalsIgnoreCase(raw)) {
                return candidate;
            }
            if (candidate.name().equalsIgnoreCase(raw)) {
                return candidate;
            }
        }

        LOGGER.warn("Unknown material '{}' for {}, falling back to STONE", raw, context);
        return Material.STONE;
    }

    private Map<String, SkillNodePrototype> applyAutoLayoutIfNeeded(String treeId,
                                                                    Map<String, SkillNodePrototype> original,
                                                                    SkillTreeLayoutConstraints layout) {
        if (layout == null || !Boolean.TRUE.equals(layout.autoArrange())) {
            return original;
        }
        float horizontalStep = layout.horizontalStep() != null && layout.horizontalStep() > 0 ? layout.horizontalStep() : 3.0f;
        float verticalSpacing = layout.verticalSpacing() != null && layout.verticalSpacing() > 0 ? layout.verticalSpacing() : 2.0f;

        Map<String, SkillNodePrototype> adjusted = new LinkedHashMap<>();
        Map<String, java.util.List<String>> children = new LinkedHashMap<>();
        for (SkillNodePrototype prototype : original.values()) {
            children.computeIfAbsent(prototype.parentId(), key -> new java.util.ArrayList<>()).add(prototype.id());
        }
        children.values().forEach(list -> list.sort(String::compareTo));

        java.util.List<String> roots = children.getOrDefault(null, java.util.List.of());
        roots.sort(String::compareTo);

        java.util.Map<String, float[]> positions = new java.util.HashMap<>();
        float currentY = 0.0f;
        for (String rootId : roots) {
            currentY = placeNode(rootId, 1, currentY, horizontalStep, verticalSpacing, children, positions);
            currentY += verticalSpacing; // space between root groups
        }

        for (var entry : original.entrySet()) {
            SkillNodePrototype prototype = entry.getValue();
            float[] pos = positions.get(prototype.id());
            if (pos != null) {
                prototype = prototype.withPosition(pos[0], pos[1]);
            } else {
                LOGGER.warn("Layout for node '{}' in tree '{}' missing, defaulting to (0,0)", prototype.id(), treeId);
            }
            adjusted.put(entry.getKey(), prototype);
        }
        return adjusted;
    }

    private float placeNode(String nodeId, int depth, float currentY,
                            float horizontalStep, float verticalSpacing,
                            Map<String, java.util.List<String>> children,
                            Map<String, float[]> positions) {
        var childList = children.getOrDefault(nodeId, java.util.List.of());
        if (childList.isEmpty()) {
            float y = currentY;
            positions.put(nodeId, new float[]{depth * horizontalStep, y});
            return currentY + verticalSpacing;
        }
        float startY = currentY;
        for (String child : childList) {
            currentY = placeNode(child, depth + 1, currentY, horizontalStep, verticalSpacing, children, positions);
        }
        float endY = currentY - verticalSpacing;
        float y = (startY + endY) / 2f;
        positions.put(nodeId, new float[]{depth * horizontalStep, y});
        return currentY;
    }
}
