package org.example.mmo.mob;

import net.kyori.adventure.text.Component;
import net.minestom.server.tag.Tag;

public final class MobMetadataKeys {

    private MobMetadataKeys() {
    }

    public static final Tag<String> ARCHETYPE_ID = Tag.String("mmo:mob_archetype");
    public static final Tag<Component> DISPLAY_NAME = Tag.Component("mmo:mob_display_name");
}
