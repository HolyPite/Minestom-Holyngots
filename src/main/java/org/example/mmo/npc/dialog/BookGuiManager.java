package org.example.mmo.npc.dialog;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
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
import org.example.utils.TKit;
import org.example.bootstrap.GameContext;
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

    private static final TextColor COLOR_PRIMARY_START = TextColor.color(0xc69432);
    private static final TextColor COLOR_PRIMARY_END = TextColor.color(0xc8297b);
    private static final TextColor COLOR_SECONDARY_START = TextColor.color(0x7D8FDF);
    private static final TextColor COLOR_SECONDARY_END = TextColor.color(0x4555A5);
    private static final TextColor COLOR_HIGHLIGHT_START = TextColor.color(0x7ACFA5);
    private static final TextColor COLOR_HIGHLIGHT_END = TextColor.color(0x3D8F6C);
    private static final TextColor COLOR_MUTED = TextColor.color(0x000000);

    private BookGuiManager() {
    }

    public static void openNpcBook(Player player, NPC npc) {
        if (npc == null) {
            return;
        }
        PlayerData data = GameContext.get().playerDataService().get(player);
        if (data == null) {
            return;
        }

        Component page = Component.empty();

        Component header = TKit.createGradientText(
                        TKit.extractPlainText(npc.name()),
                        COLOR_PRIMARY_START,
                        COLOR_PRIMARY_END
                )
                .decorate(TextDecoration.BOLD);

        page = page.append(header).append(Component.newline());

        if (!npc.randomDialogues().isEmpty()) {
            String cmdTalk = "/npc_interact talk " + npc.id();
            Component talkComponent = TKit.createGradientText("[Parler]", COLOR_SECONDARY_START, COLOR_SECONDARY_END)
                    .decorate(TextDecoration.UNDERLINED)
                    .clickEvent(ClickEvent.runCommand(cmdTalk))
                    .hoverEvent(HoverEvent.showText(
                            Component.text("Discuter avec ")
                                    .append(npc.name().color(COLOR_PRIMARY_END))
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
                        Component.text("Continuer la quête : ").append(quest.name.color(COLOR_PRIMARY_END)),
                        QuestEntryStyle.OBJECTIVE
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
                        Component.text("Valider la quête : ").append(quest.name.color(COLOR_PRIMARY_END)),
                        QuestEntryStyle.TURN_IN
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
                        Component.text("Commencer la quête : ").append(quest.name.color(COLOR_PRIMARY_END)),
                        QuestEntryStyle.AVAILABLE
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
                Component npcHeader = TKit.createGradientText(npcName, COLOR_PRIMARY_START, COLOR_PRIMARY_END)
                        .decorate(TextDecoration.BOLD);
                page = page.append(npcHeader).append(Component.newline());
                headerAdded = true;
            }
        }

        String questTitle = quest != null && quest.name != null ? TKit.extractPlainText(quest.name) : null;
        if (questTitle != null && !questTitle.isBlank()) {
            Component questHeader = TKit.createGradientText(questTitle, COLOR_PRIMARY_START, COLOR_PRIMARY_END)
                    .decorate(TextDecoration.BOLD);
            page = page.append(questHeader).append(Component.newline());
            headerAdded = true;
        }

        String stepTitle = null;
        if (step != null && step.name != null) {
            stepTitle = TKit.extractPlainText(step.name);
        }

        if (stepTitle != null && !stepTitle.isBlank() && (questTitle == null || !stepTitle.equals(questTitle))) {
            Component stepHeader = TKit.createGradientText(stepTitle, COLOR_SECONDARY_START, COLOR_SECONDARY_END)
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
                    .append(line.color(COLOR_MUTED))
                    .append(Component.newline());
        }

        page = page.append(Component.newline());

        if (npc != null) {
            String returnCommand = "/npc_interact book " + npc.id();
            Component backBtn = TKit.createGradientText("[Retour]", COLOR_SECONDARY_START, COLOR_SECONDARY_END)
                    .decorate(TextDecoration.UNDERLINED)
                    .clickEvent(ClickEvent.runCommand(returnCommand))
                    .hoverEvent(HoverEvent.showText(
                            Component.text("Retourner au menu principal")
                                    .color(COLOR_MUTED)
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
        return TKit.createGradientText(raw, COLOR_PRIMARY_START, COLOR_PRIMARY_END)
                .decorate(TextDecoration.BOLD);
    }

    private enum QuestEntryStyle {
        OBJECTIVE(COLOR_SECONDARY_START, COLOR_SECONDARY_END, false, false),
        TURN_IN(COLOR_HIGHLIGHT_START, COLOR_HIGHLIGHT_END, true, false),
        AVAILABLE(COLOR_SECONDARY_START, COLOR_SECONDARY_END, false, true);

        final TextColor startColor;
        final TextColor endColor;
        final boolean bold;
        final boolean italic;

        QuestEntryStyle(TextColor startColor, TextColor endColor, boolean bold, boolean italic) {
            this.startColor = startColor;
            this.endColor = endColor;
            this.bold = bold;
            this.italic = italic;
        }
    }

    private static Component questEntryLine(String questNameWrapped,
                                            String clickCommand,
                                            Component hoverText,
                                            QuestEntryStyle style) {

        Component bullet = Component.text(" · ", COLOR_MUTED)
                .decoration(TextDecoration.UNDERLINED, false)
                .decoration(TextDecoration.ITALIC, false);

        Component clickable = TKit.createGradientText(questNameWrapped, style.startColor, style.endColor)
                .decorate(TextDecoration.UNDERLINED)
                .decoration(TextDecoration.BOLD, style.bold)
                .decoration(TextDecoration.ITALIC, style.italic)
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


