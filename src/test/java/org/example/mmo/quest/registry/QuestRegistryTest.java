package org.example.mmo.quest.registry;

import net.kyori.adventure.text.Component;
import org.example.mmo.quest.structure.Quest;
import org.example.mmo.quest.structure.QuestStep;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class QuestRegistryTest {

    @AfterEach
    void clearRegistry() throws Exception {
        getMap("MAP").clear();
        getList("AUTO_START_QUESTS").clear();
        getMap("QUESTS_BY_START_NPC").clear();
    }

    @Test
    void registerStoresQuestById() throws Exception {
        QuestStep step = new QuestStep();
        step.name = Component.text("Step 1");
        step.startNpc = null;
        step.endNpc = null;
        step.duration = Duration.ZERO;

        Quest quest = new Quest(
                "test_quest",
                Component.text("Quest"),
                Component.text("Description"),
                List.of(step)
        );

        QuestRegistry.register(quest);

        Assertions.assertEquals(quest, QuestRegistry.byId("test_quest"));
        Assertions.assertTrue(QuestRegistry.autoStartQuests().contains(quest));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Quest> getMap(String fieldName) throws Exception {
        Field field = QuestRegistry.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (Map<String, Quest>) field.get(null);
    }

    @SuppressWarnings("unchecked")
    private List<Quest> getList(String fieldName) throws Exception {
        Field field = QuestRegistry.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (List<Quest>) field.get(null);
    }
}
