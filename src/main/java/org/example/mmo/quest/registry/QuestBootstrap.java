package org.example.mmo.quest.registry;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

import java.util.List;

public final class QuestBootstrap {

    public static void init() {

        try (ScanResult scan = new ClassGraph()
                .enableClassInfo()
                .ignoreClassVisibility()
                .acceptPackages("org.example.mmo.quest.quests")   // toute la hiérarchie
                .scan())
        {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            List<ClassInfo> classes = scan.getAllStandardClasses();

            for (ClassInfo info : classes) {
                try {
                    Class.forName(info.getName(), true, cl);   // initialise
                } catch (Throwable err) {
                    System.err.println("[QuestBootstrap] init FAILED: " + info.getName());
                    err.printStackTrace(System.err);
                }
            }
        }
    }

    private QuestBootstrap() {}
}
