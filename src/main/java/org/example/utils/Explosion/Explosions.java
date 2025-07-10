package org.example.utils.Explosion;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.instance.Instance;

public final class Explosions {

    private Explosions() {}                // util-class

    public static void explode(Instance inst,
                               double x, double y, double z,
                               float strength,
                               float fireChance,
                               float kb) {

        CompoundBinaryTag nbt = CompoundBinaryTag.builder()
                .putFloat("fireChance", fireChance)
                .putFloat("kb",          kb)
                .build();

        inst.explode((float) x,(float) y,(float) z, strength, nbt);
    }
}
