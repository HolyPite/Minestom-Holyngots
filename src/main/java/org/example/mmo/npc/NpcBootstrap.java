package org.example.mmo.npc;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

import java.util.List;

public final class NpcBootstrap {

    public static void init() {

        try (ScanResult scan = new ClassGraph()
                .enableClassInfo()
                .ignoreClassVisibility()
                .acceptPackages("org.example.mmo.npc.npcs") // Scan this package for NPC definitions
                .scan())
        {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            List<ClassInfo> classes = scan.getAllStandardClasses();

            for (ClassInfo info : classes) {
                try {
                    Class.forName(info.getName(), true, cl);   // This triggers the static block in each NPC file
                } catch (Throwable err) {
                    System.err.println("[NpcBootstrap] init FAILED: " + info.getName());
                    err.printStackTrace(System.err);
                }
            }
        }
    }

    private NpcBootstrap() {}
}
