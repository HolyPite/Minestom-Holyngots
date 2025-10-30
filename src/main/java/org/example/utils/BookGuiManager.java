package org.example.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.book.FilteredText;
import net.minestom.server.item.component.WrittenBookContent;
import net.minestom.server.network.packet.server.play.OpenBookPacket;
import org.example.NodesManagement;
import org.example.data.data_class.PlayerData;
import org.example.mmo.npc.NPC;
import org.example.mmo.quest.QuestManager;
import org.example.mmo.quest.api.IQuestObjective;
import org.example.mmo.quest.objectives.TalkObjective;
import org.example.mmo.quest.registry.QuestRegistry;
import org.example.mmo.quest.structure.Quest;
import org.example.mmo.quest.structure.QuestProgress;
import org.example.mmo.quest.structure.QuestStep;

import java.util.ArrayList;
import java.util.List;

public final class BookGuiManager {

    private static final TextColor COLOR_HEADER_START = TextColor.color(0xC9A441);
    private static final TextColor COLOR_HEADER_END = TextColor.color(0x8A6B1F);

    private static final TextColor COLOR_SECTION_START = TextColor.color(0xE8D27C);
    private static final TextColor COLOR_SECTION_END = TextColor.color(0xB8A24F);

    private static final TextColor COLOR_DIALOGUE_LINE = TextColor.color(0x4A4A4A);

    private static final TextColor COLOR_TALK_START = TextColor.color(0x4A4A4A);
    private static final TextColor COLOR_TALK_END = TextColor.color(0x8A8A8A);
    private static final TextColor COLOR_OBJECTIVE_START = TextColor.color(0x2F3E7A);
    private static final TextColor COLOR_OBJECTIVE_END = TextColor.color(0x6A74C9);
    private static final TextColor COLOR_VALIDATE_START = TextColor.color(0x2F5A2F);
    private static final TextColor COLOR_VALIDATE_END = TextColor.color(0x6FC16F);
    private static final TextColor COLOR_AVAILABLE_START = TextColor.color(0x2E5E5E);
    private static final TextColor COLOR_AVAILABLE_END = TextColor.color(0x6EDBDB);
    private static final TextColor COLOR_RETURN_START = TextColor.color(0x1F2F6A);
    private static final TextColor COLOR_RETURN_END = TextColor.color(0x4A63C9);

    private static final TextColor COLOR_QUEST_TITLE_START = TextColor.color(0xF4E3A1);
    private static final TextColor COLOR_QUEST_TITLE_END = TextColor.color(0xC99A34);
    private static final TextColor COLOR_STEP_TITLE_START = TextColor.color(0xCBD8F8);
    private static final TextColor COLOR_STEP_TITLE_END = TextColor.color(0x7E94D9);

    private BookGuiManager() {
    }

    public static void openNpcBook(Player player, NPC npc) {
        if (npc == null) {
            return;
        }
        PlayerData data = NodesManagement.getDataService().get(player);
        if (data == null) {
            return;
        }

        Component page = Component.empty();

        Component header = npc.name().decorate(TextDecoration.BOLD);

        page = page.append(header).append(Component.newline());

        if (!npc.randomDialogues().isEmpty()) {
            String cmdTalk = "/npc_interact talk " + npc.id();
            Component talkComponent = TKit.createGradientText("[Parler]", COLOR_TALK_START, COLOR_TALK_END)
                    .decorate(TextDecoration.UNDERLINED)
                    .clickEvent(ClickEvent.runCommand(cmdTalk))
                    .hoverEvent(HoverEvent.showText(
                            Component.text("Discuter avec ")
                                    .append(npc.name().color(NamedTextColor.GOLD))
                    ));
            page = page.append(talkComponent).append(Component.newline());
        }

        List<QuestProgress> talkObjectives = getTalkObjectives(data, npc.id());
        if (!talkObjectives.isEmpty()) {
            page = page
                    .append(Component.newline())
                    .append(gradientSectionTitle("Dialogue de quêtes"))
                    .append(Component.newline());

            for (QuestProgress progress : talkObjectives) {
                Quest quest = QuestRegistry.byId(progress.questId);
                if (quest == null) {
                    continue;
                }

                String questNameWrapped = wrapQuestTitleForBook(
                        TKit.extractPlainText(quest.name),
                        24
                );

                String cmdObj = "/npc_interact talk_objective " + npc.id() + " " + quest.id;
                Component questLine = questEntryLine(
                        questNameWrapped,
                        cmdObj,
                        Component.text("Continuer la quête : ").append(quest.name.color(NamedTextColor.GOLD)),
                        COLOR_OBJECTIVE_START,
                        COLOR_OBJECTIVE_END
                );

                page = page.append(questLine).append(Component.newline());
            }
        }

        List<Quest> questsToAdvance = getQuestsToAdvance(data, npc.id());
        if (!questsToAdvance.isEmpty()) {
            page = page
                    .append(Component.newline())
                    .append(gradientSectionTitle("Quêtes à valider"))
                    .append(Component.newline());

            for (Quest quest : questsToAdvance) {

                String questNameWrapped = wrapQuestTitleForBook(
                        TKit.extractPlainText(quest.name),
                        24
                );

                String cmdValidate = "/npc_interact advance_quest " + npc.id() + " " + quest.id;
                Component questLine = questEntryLine(
                        questNameWrapped,
                        cmdValidate,
                        Component.text("Valider la quête : ").append(quest.name.color(NamedTextColor.GOLD)),
                        COLOR_VALIDATE_START,
                        COLOR_VALIDATE_END
                );

                page = page.append(questLine).append(Component.newline());
            }
        }

        List<Quest> availableQuests = getAvailableQuests(data, npc.id());
        if (!availableQuests.isEmpty()) {
            page = page
                    .append(Component.newline())
                    .append(gradientSectionTitle("Quêtes disponibles"))
                    .append(Component.newline());

            for (Quest quest : availableQuests) {

                String questNameWrapped = wrapQuestTitleForBook(
                        TKit.extractPlainText(quest.name),
                        24
                );

                String cmdStart = "/npc_interact start_quest " + npc.id() + " " + quest.id;
                Component questLine = questEntryLine(
                        questNameWrapped,
                        cmdStart,
                        Component.text("Commencer la quête : ").append(quest.name.color(NamedTextColor.GOLD)),
                        COLOR_AVAILABLE_START,
                        COLOR_AVAILABLE_END
                );

                page = page.append(questLine).append(Component.newline());
            }
        }

        String bookTitle = TKit.extractPlainText(npc.name());
        ItemStack book = createBook(bookTitle.isBlank() ? "Dialogue" : bookTitle, List.of(page));
        openBookUI(player, book);
    }

    public static void showDialogueBook(Player player, NPC npc, List<Component> dialogue) {
        showDialogueBook(player, npc, null, null, dialogue);
    }

    public static void showDialogueBook(Player player, NPC npc, Quest quest, QuestStep step, List<Component> dialogue) {
        if (dialogue == null || dialogue.isEmpty()) {
            return;
        }

        Component page = Component.empty();

        boolean headerAdded = false;

        if (npc != null) {
            String npcName = TKit.extractPlainText(npc.name());
            if (!npcName.isBlank()) {
                Component npcHeader = TKit.createGradientText(npcName, COLOR_HEADER_START, COLOR_HEADER_END)
                        .decorate(TextDecoration.BOLD);
                page = page.append(npcHeader).append(Component.newline());
                headerAdded = true;
            }
        }

        String questTitle = quest != null && quest.name != null ? TKit.extractPlainText(quest.name) : null;
        if (questTitle != null && !questTitle.isBlank()) {
            Component questHeader = TKit.createGradientText(questTitle, COLOR_QUEST_TITLE_START, COLOR_QUEST_TITLE_END)
                    .decorate(TextDecoration.BOLD);
            page = page.append(questHeader).append(Component.newline());
            headerAdded = true;
        }

        String stepTitle = null;
        if (step != null && step.name != null) {
            stepTitle = TKit.extractPlainText(step.name);
        }

        if (stepTitle != null && !stepTitle.isBlank() && (questTitle == null || !stepTitle.equals(questTitle))) {
            Component stepHeader = TKit.createGradientText(stepTitle, COLOR_STEP_TITLE_START, COLOR_STEP_TITLE_END)
                    .decorate(TextDecoration.BOLD)
                    .decorate(TextDecoration.ITALIC);
            page = page.append(stepHeader).append(Component.newline());
            headerAdded = true;
        }

        if (headerAdded) {
            page = page.append(Component.newline());
        }

        for (Component line : dialogue) {
            page = page
                    .append(line.color(COLOR_DIALOGUE_LINE))
                    .append(Component.newline());
        }

        page = page.append(Component.newline());

        if (npc != null) {
            String returnCommand = "/npc_interact book " + npc.id();
            Component backBtn = TKit.createGradientText("[Retour]", COLOR_RETURN_START, COLOR_RETURN_END)
                    .decorate(TextDecoration.UNDERLINED)
                    .clickEvent(ClickEvent.runCommand(returnCommand))
                    .hoverEvent(HoverEvent.showText(
                            Component.text("Retourner au menu principal")
                                    .color(NamedTextColor.GRAY)
                    ));

            page = page.append(backBtn);
        }

        String bookTitle;
        if (questTitle != null && !questTitle.isBlank()) {
            bookTitle = questTitle;
        } else if (npc != null) {
            String npcName = TKit.extractPlainText(npc.name());
            bookTitle = npcName.isBlank() ? "Dialogue" : npcName;
        } else {
            bookTitle = "Dialogue";
        }

        ItemStack book = createBook(bookTitle, List.of(page));
        openBookUI(player, book);
    }

    private static Component gradientSectionTitle(String raw) {
        return TKit.createGradientText(raw, COLOR_SECTION_START, COLOR_SECTION_END)
                .decorate(TextDecoration.BOLD);
    }

    private static Component questEntryLine(String questNameWrapped,
                                            String clickCommand,
                                            Component hoverText,
                                            TextColor startColor,
                                            TextColor endColor) {

        Component bullet = Component.text(" · ", NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.UNDERLINED, false)
                .decoration(TextDecoration.ITALIC, false);

        Component clickable = TKit.createGradientText(questNameWrapped, startColor, endColor)
                .decorate(TextDecoration.UNDERLINED)
                .decoration(TextDecoration.ITALIC, false)
                .clickEvent(ClickEvent.runCommand(clickCommand))
                .hoverEvent(HoverEvent.showText(hoverText));

        return bullet.append(clickable);
    }

    private static String wrapQuestTitleForBook(String raw, int maxLen) {
        if (raw.length() <= maxLen) {
            return raw;
        }

        int cut = raw.lastIndexOf(' ', maxLen);
        if (cut <= 0) {
            cut = maxLen;
        }

        String first = raw.substring(0, cut).trim();
        String rest = raw.substring(cut).trim();

        return first + "\n  " + rest;
    }

    private static ItemStack createBook(String title, List<Component> pages) {
        var ftTitle = new FilteredText<>(title, null);
        List<FilteredText<Component>> ftPages = pages.stream()
                .map(c -> new FilteredText<>(c, null))
                .toList();

        var content = new WrittenBookContent(
                ftTitle,
                "",
                0,
                ftPages,
                true
        );

        return ItemStack.of(Material.WRITTEN_BOOK)
                .with(DataComponents.WRITTEN_BOOK_CONTENT, content);
    }

    private static void openBookUI(Player player, ItemStack book) {
        ItemStack prev = player.getItemInOffHand();
        player.setItemInOffHand(book);
        player.sendPacket(new OpenBookPacket(PlayerHand.OFF));
        MinecraftServer.getSchedulerManager().scheduleNextTick(() -> player.setItemInOffHand(prev));
    }

    private static List<QuestProgress> getTalkObjectives(PlayerData data, String npcId) {
        List<QuestProgress> result = new ArrayList<>();
        for (QuestProgress progress : data.quests) {
            Quest quest = QuestRegistry.byId(progress.questId);
            if (quest != null && progress.stepIndex < quest.steps.size()) {
                QuestStep currentStep = quest.steps.get(progress.stepIndex);
                for (IQuestObjective objective : currentStep.objectives) {
                    if (objective instanceof TalkObjective talkObj
                            && talkObj.getNpcId().equals(npcId)
                            && !progress.isObjectiveCompleted(objective)) {
                        result.add(progress);
                        break;
                    }
                }
            }
        }
        return result;
    }

    private static List<Quest> getQuestsToAdvance(PlayerData data, String npcId) {
        List<Quest> result = new ArrayList<>();
        for (QuestProgress progress : data.quests) {
            Quest quest = QuestRegistry.byId(progress.questId);
            if (quest != null && progress.stepIndex < quest.steps.size()) {
                QuestStep currentStep = quest.steps.get(progress.stepIndex);
                if (npcId.equals(currentStep.endNpc)
                        && currentStep.objectives.stream().allMatch(progress::isObjectiveCompleted)) {
                    result.add(quest);
                }
            }
        }
        return result;
    }

    private static List<Quest> getAvailableQuests(PlayerData data, String npcId) {
        List<Quest> result = new ArrayList<>();
        for (Quest quest : QuestRegistry.byStartNpc(npcId)) {
            if (quest.steps.isEmpty()) {
                continue;
            }

            boolean hasQuest = data.quests.stream().anyMatch(p -> p.questId.equals(quest.id));
            boolean hasCompleted = data.hasCompletedQuest(quest.id);
            boolean hasFailed = data.hasFailedQuest(quest.id);

            if (!hasQuest && !hasCompleted && !hasFailed) {
                QuestStep firstStep = quest.steps.getFirst();
                if (QuestManager.checkPrerequisites(data, firstStep)) {
                    result.add(quest);
                }
            }
        }
        return result;
    }
}


