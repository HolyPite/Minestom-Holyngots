package org.example.mmo.item.power;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.example.mmo.item.skill.Power;
import org.example.mmo.item.skill.PowerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PowerBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(PowerBootstrap.class);
    private static final String POWER_PACKAGE = "org.example.mmo.item.power";

    private PowerBootstrap() {}

    public static void init() {
        int registered = 0;
        try (ScanResult scan = new ClassGraph()
                .enableClassInfo()
                .acceptPackages(POWER_PACKAGE)
                .scan()) {
            for (ClassInfo classInfo : scan.getClassesImplementing(Power.class)) {
                if (loadPower(classInfo)) {
                    registered++;
                }
            }
        }
        LOGGER.info("Registered {} item powers", registered);
    }

    private static boolean loadPower(ClassInfo classInfo) {
        try {
            Class<?> loaded = classInfo.loadClass();
            if (!Power.class.isAssignableFrom(loaded)) {
                return false;
            }
            PowerId annotation = loaded.getAnnotation(PowerId.class);
            if (annotation == null) {
                LOGGER.warn("Power {} has no @PowerId annotation, skipping", loaded.getName());
                return false;
            }
            Power power = (Power) loaded.getDeclaredConstructor().newInstance();
            PowerRegistry.register(annotation.value(), power);
            LOGGER.debug("Registered power {} ({})", annotation.value(), loaded.getSimpleName());
            return true;
        } catch (ReflectiveOperationException e) {
            LOGGER.error("Failed to register power {}", classInfo.getName(), e);
            return false;
        }
    }
}
