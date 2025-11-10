package org.example.mmo.npc;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NpcBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(NpcBootstrap.class);

    private NpcBootstrap() {
    }

    public static void init() {

        try (ScanResult scan = new ClassGraph()
                .enableClassInfo()
                .ignoreClassVisibility()
                .acceptPackages("org.example.mmo.npc.npcs")
                .scan()) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            List<ClassInfo> classes = scan.getAllStandardClasses();

            for (ClassInfo info : classes) {
                try {
                    Class.forName(info.getName(), true, cl);
                } catch (Throwable err) {
                    LOGGER.error("Failed to initialise NPC class {}", info.getName(), err);
                }
            }
        }
    }
}
