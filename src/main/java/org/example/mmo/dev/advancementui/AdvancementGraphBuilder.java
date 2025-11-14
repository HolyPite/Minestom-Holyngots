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
            SkillNodePrototype prototype = toNodePrototype(definition.id(), node);
            if (nodeMap.putIfAbsent(prototype.id(), prototype) != null) {
                throw new IllegalStateException("Duplicate node id '" + prototype.id() + "' in tree " + definition.id());
            }
        }

        validateParentLinks(definition.id(), nodeMap);
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

    private SkillNodePrototype toNodePrototype(String treeId, SkillNodeDefinition node) {
        if (node.id() == null || node.id().isBlank()) {
            throw new IllegalStateException("Node without id in tree " + treeId);
        }
        Component title = Component.text(node.title() != null ? node.title() : node.id());
        Component description = Component.text(node.description() != null ? node.description() : "");
        Material icon = resolveMaterial(node.icon(), "icon for node " + node.id() + " in tree " + treeId);
        FrameType frameType = resolveFrame(node.frameType(), FrameType.TASK);
        Map<String, Object> metadata = node.metadata() == null ? Map.of() : Map.copyOf(node.metadata());
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
                node.parentId(),
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
}
