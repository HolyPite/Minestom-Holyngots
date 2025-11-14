package org.example.mmo.dev.advancementui;

import org.example.mmo.dev.advancementui.definition.SkillTreeDefinition;
import org.example.mmo.dev.advancementui.model.SkillTreePrototype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintient les prototypes chargés depuis les définitions de skill tree.
 */
public final class AdvancementUiRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdvancementUiRegistry.class);

    private final SkillTreeDefinitionLoader loader;
    private final AdvancementGraphBuilder graphBuilder;
    private final Map<String, SkillTreePrototype> prototypes = new ConcurrentHashMap<>();

    public AdvancementUiRegistry(SkillTreeDefinitionLoader loader, AdvancementGraphBuilder graphBuilder) {
        this.loader = loader;
        this.graphBuilder = graphBuilder;
    }

    public void reload() {
        prototypes.clear();
        for (SkillTreeDefinition definition : loader.loadAll()) {
            try {
                SkillTreePrototype prototype = graphBuilder.build(definition);
                prototypes.put(prototype.id(), prototype);
            } catch (Exception e) {
                LOGGER.error("Failed to build advancement prototype for {}", definition.id(), e);
            }
        }
        LOGGER.info("Registered {} skill tree prototypes", prototypes.size());
    }

    public SkillTreePrototype get(String id) {
        return prototypes.get(id);
    }

    public Collection<String> ids() {
        return prototypes.keySet();
    }

    public int size() {
        return prototypes.size();
    }
}
