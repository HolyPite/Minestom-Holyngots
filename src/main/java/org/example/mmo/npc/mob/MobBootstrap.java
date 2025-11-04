package org.example.mmo.npc.mob;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads mob-related definitions so adding a class in the right package is enough.
 */
public final class MobBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(MobBootstrap.class);
    private static final List<String> PACKAGE_WHITELIST = List.of(
            "org.example.mmo.npc.mob.archetype.archetypes",
            "org.example.mmo.npc.mob.ai.ais",
            "org.example.mmo.npc.mob.behaviour.behaviours",
            "org.example.mmo.npc.mob.loot.loots"
    );

    private MobBootstrap() {
    }

    public static void init() {
        try (ScanResult scan = new ClassGraph()
                .enableClassInfo()
                .ignoreClassVisibility()
                .acceptPackages(PACKAGE_WHITELIST.toArray(String[]::new))
                .scan()) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            for (ClassInfo info : scan.getAllStandardClasses()) {
                try {
                    Class.forName(info.getName(), true, classLoader);
                } catch (Throwable throwable) {
                    LOGGER.error("Failed to initialise mob class {}", info.getName(), throwable);
                }
            }
        }
    }
}
