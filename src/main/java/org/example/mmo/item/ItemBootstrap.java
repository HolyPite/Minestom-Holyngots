// org.example.mmo.items.ItemBootstrap
package org.example.mmo.item;

import io.github.classgraph.*;
import java.util.List;

public final class ItemBootstrap {

    public static void init() {

        try (ScanResult scan = new ClassGraph()
                .enableClassInfo()
                .ignoreClassVisibility()
                .acceptPackages("org.example.mmo.items.itemsList")   // toute la hiérarchie
                .scan())
        {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            List<ClassInfo> classes = scan.getAllStandardClasses();
            //int ok = 0, ko = 0;

            for (ClassInfo info : classes) {
                try {
                    Class.forName(info.getName(), true, cl);   // initialise
                    //ok++;
                } catch (Throwable err) {                      // <- Saisit *tout*
                    //ko++;
                    System.err.println("[ItemBootstrap] init FAILED: " + info.getName());
                    err.printStackTrace(System.err);
                }
            }
            //System.out.printf("[ItemBootstrap] items OK=%d  ERR=%d%n", ok, ko);
        }
    }

    private ItemBootstrap() {}
}
