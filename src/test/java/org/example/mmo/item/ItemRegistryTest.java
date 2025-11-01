package org.example.mmo.item;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class ItemRegistryTest {

    @AfterEach
    void clearRegistry() throws Exception {
        getRegistryMap().clear();
    }

    @Test
    void registerStoresItemById() throws Exception {
        GameItem item = new GameItem.Builder("test_item", Component.text("Test Item"))
                .material(Material.DIAMOND)
                .build();

        ItemRegistry.register(item);

        Assertions.assertEquals(item, ItemRegistry.byId("test_item"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, GameItem> getRegistryMap() throws Exception {
        var field = ItemRegistry.class.getDeclaredField("MAP");
        field.setAccessible(true);
        return (Map<String, GameItem>) field.get(null);
    }
}
