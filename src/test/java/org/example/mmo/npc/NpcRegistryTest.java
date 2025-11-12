package org.example.mmo.npc;

import net.kyori.adventure.text.Component;
import net.minestom.server.coordinate.Pos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class NpcRegistryTest {

    @AfterEach
    void clearRegistry() throws Exception {
        Map<String, NPC> map = getRegistryMap();
        map.clear();
    }

    @Test
    void registerStoresNpcById() throws Exception {
        NPC npc = new NPC("test_npc", Component.text("Test NPC"), Pos.ZERO, List.of());

        NpcRegistry.register(npc);

        Assertions.assertEquals(npc, NpcRegistry.byId("test_npc"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, NPC> getRegistryMap() throws Exception {
        var field = NpcRegistry.class.getDeclaredField("MAP");
        field.setAccessible(true);
        return (Map<String, NPC>) field.get(null);
    }
}
