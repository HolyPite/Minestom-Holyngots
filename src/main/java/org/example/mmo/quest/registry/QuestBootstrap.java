package org.example.mmo.quest.registry;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class QuestBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuestBootstrap.class);

    private QuestBootstrap() {
    }

    public static void init() {

        try (ScanResult scan = new ClassGraph()
                .enableClassInfo()
                .ignoreClassVisibility()
                .acceptPackages("org.example.mmo.quest.quests")
                .scan()) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            List<ClassInfo> classes = scan.getAllStandardClasses();

            for (ClassInfo info : classes) {
                try {
                    Class.forName(info.getName(), true, cl);
                } catch (Throwable err) {
                    LOGGER.error("Failed to initialise quest class {}", info.getName(), err);
                }
            }
        }
    }
}
