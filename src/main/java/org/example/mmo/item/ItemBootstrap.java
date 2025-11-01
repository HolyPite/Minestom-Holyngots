// org.example.mmo.items.ItemBootstrap
package org.example.mmo.item;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ItemBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemBootstrap.class);

    private ItemBootstrap() {
    }

    public static void init() {
        try (ScanResult scan = new ClassGraph()
                .enableClassInfo()
                .ignoreClassVisibility()
                .acceptPackages("org.example.mmo.item.items")
                .scan()) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            List<ClassInfo> classes = scan.getAllStandardClasses();

            for (ClassInfo info : classes) {
                try {
                    Class.forName(info.getName(), true, cl);
                } catch (Throwable err) {
                    LOGGER.error("Failed to initialise item class {}", info.getName(), err);
                }
            }
        }
    }
}
