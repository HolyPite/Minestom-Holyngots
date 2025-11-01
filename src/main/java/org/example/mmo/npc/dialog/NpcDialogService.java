package org.example.mmo.npc.dialog;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.dialog.Dialog;
import net.minestom.server.dialog.DialogAction;
import net.minestom.server.dialog.DialogActionButton;
import net.minestom.server.dialog.DialogAfterAction;
import net.minestom.server.dialog.DialogBody;
import net.minestom.server.dialog.DialogMetadata;
import org.example.bootstrap.GameContext;
import org.example.mmo.player.data.PlayerDataService;
import org.example.data.data_class.PlayerData;
import org.example.mmo.npc.NPC;
import org.example.mmo.npc.NpcRegistry;
import org.example.mmo.quest.QuestManager;
import org.example.mmo.quest.api.IQuestObjective;
import org.example.mmo.quest.event.QuestObjectiveCompleteEvent;
import org.example.mmo.quest.objectives.TalkObjective;
import org.example.mmo.quest.registry.QuestRegistry;
import org.example.mmo.quest.structure.Quest;
import org.example.mmo.quest.structure.QuestProgress;
import org.example.mmo.quest.structure.QuestStep;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Centralizes NPC interaction dialogs using Minestom's native dialog API.
 * <p>
 * The service exposes a small session-based API so caller code can build menus with buttons that
 * trigger strongly-typed callbacks instead of ad-hoc commands.
 */
public final class NpcDialogService {

    private static final DialogBody.PlainMessage EMPTY_MESSAGE =
            new DialogBody.PlainMessage(Component.text("Aucun contenu disponible.", NamedTextColor.GRAY), DialogBody.PlainMessage.DEFAULT_WIDTH);

    private static final Style PRIMARY_STYLE = Style.style(NamedTextColor.GOLD);
    private static final Style SECONDARY_STYLE = Style.style(NamedTextColor.AQUA);
    private static final Style MUTED_STYLE = Style.style(NamedTextColor.WHITE);

    private static final class UiText {
        private static final Component MENU_DESCRIPTION =
                Component.text("Choisissez une interaction.", NamedTextColor.WHITE);

        private static final Component SUMMARY_TITLE =
                Component.text("Résumé de l'aventurier", PRIMARY_STYLE).decorate(TextDecoration.BOLD, TextDecoration.UNDERLINED);

        private static final Component TO_MENU_TOOLTIP =
                Component.text("Retour au menu principal.", MUTED_STYLE);
        private static final Component CLOSE_DIALOG_TOOLTIP =
                Component.text("Fermer le dialogue.", MUTED_STYLE);

        private static final Component BUTTON_TALK =
                Component.text("Parler", NamedTextColor.GREEN);
        private static final Component BUTTON_TALK_TOOLTIP =
                Component.text("Discuter avec le PNJ.", MUTED_STYLE);

        private static final Component BUTTON_QUEST_DIALOG =
                Component.text("Dialogues de quête", SECONDARY_STYLE);
        private static final Component BUTTON_QUEST_DIALOG_TOOLTIP =
                Component.text("Progresser sur vos objectifs de dialogue.", MUTED_STYLE);

        private static final Component BUTTON_TURN_IN =
                Component.text("Quêtes à valider", SECONDARY_STYLE);
        private static final Component BUTTON_TURN_IN_TOOLTIP =
                Component.text("Rendre vos quêtes terminées.", MUTED_STYLE);

        private static final Component BUTTON_AVAILABLE =
                Component.text("Quêtes disponibles", SECONDARY_STYLE);
        private static final Component BUTTON_AVAILABLE_TOOLTIP =
                Component.text("Examiner les nouvelles quêtes.", MUTED_STYLE);

        private static final Component BUTTON_CLOSE =
                Component.text("Fermer", MUTED_STYLE);

        private static final Component BUTTON_BACK =
                Component.text("Retour", MUTED_STYLE);

        private static final Component BUTTON_ACCEPT =
                Component.text("Accepter", PRIMARY_STYLE);
        private static final Component BUTTON_ACCEPT_TOOLTIP =
                Component.text("Accepter cette quête.", MUTED_STYLE);
        private static final Component BUTTON_VIEW_QUEST_TOOLTIP =
                Component.text("Voir les détails de cette quête.", MUTED_STYLE);

        private static final Component BUTTON_DECLINE =
                Component.text("Refuser", MUTED_STYLE);
        private static final Component BUTTON_DECLINE_TOOLTIP =
                Component.text("Revenir à la liste des quêtes.", MUTED_STYLE);

        private static final Component BUTTON_NEXT =
                Component.text("Suivant", PRIMARY_STYLE);
        private static final Component BUTTON_NEXT_TOOLTIP =
                Component.text("Voir le message suivant.", MUTED_STYLE);

        private static final Component BUTTON_PREVIOUS =
                Component.text("Précédent", MUTED_STYLE);
        private static final Component BUTTON_PREVIOUS_TOOLTIP =
                Component.text("Revenir au message précédent.", MUTED_STYLE);

        private static final Component BUTTON_FINISH =
                Component.text("Terminer", PRIMARY_STYLE);
        private static final Component BUTTON_FINISH_TOOLTIP =
                Component.text("Retourner au menu principal.", MUTED_STYLE);

        private static final Component TITLE_INTERACTION =
                Component.text("Interaction", PRIMARY_STYLE);
        private static final Component TITLE_DIALOG =
                Component.text("Dialogue", PRIMARY_STYLE);
        private static final Component TITLE_DISCUSSION =
                Component.text("Discussion", PRIMARY_STYLE);
        private static final Component TITLE_VALIDATION =
                Component.text("Validation", PRIMARY_STYLE);
        private static final Component TITLE_AVAILABLE =
                Component.text("Quêtes", PRIMARY_STYLE);
        private static final Component TITLE_TALK_OBJECTIVES =
                Component.text("Objectifs de dialogue", PRIMARY_STYLE);
        private static final Component TITLE_TURN_INS =
                Component.text("Quêtes à valider", PRIMARY_STYLE);
        private static final Component TITLE_AVAILABLE_LIST =
                Component.text("Quêtes disponibles", PRIMARY_STYLE);
        private static final Component SUBTITLE_QUEST_PREVIEW =
                Component.text("Prévisualisation de quête", MUTED_STYLE);

        private static final Component TOOLTIP_CONTINUE_QUEST =
                Component.text("Continuer cette quête.", MUTED_STYLE);
        private static final Component TOOLTIP_SUBMIT_QUEST =
                Component.text("Rendre cette quête.", MUTED_STYLE);

        private static final Component TALK_UNAVAILABLE =
                Component.text("Parler (aucune discussion libre).", MUTED_STYLE);

        private static final Component CATEGORY_EMPTY_TALKS =
                Component.text("Aucun dialogue de quête disponible.", MUTED_STYLE);
        private static final Component CATEGORY_EMPTY_TURN_INS =
                Component.text("Aucune quête à valider.", MUTED_STYLE);
        private static final Component CATEGORY_EMPTY_AVAILABLE =
                Component.text("Aucune quête disponible.", MUTED_STYLE);
    }



    private static final AtomicLong SESSION_SEQUENCE = new AtomicLong();
    private static final Map<Long, Session> SESSIONS = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> PLAYER_ACTIVE_SESSION = new ConcurrentHashMap<>();

    private NpcDialogService() {
    }

    public static void init(EventNode<PlayerEvent> playerEvents) {
        playerEvents.addListener(PlayerDisconnectEvent.class, event -> {
            UUID uuid = event.getPlayer().getUuid();
            Long sessionId = PLAYER_ACTIVE_SESSION.remove(uuid);
            if (sessionId != null) {
                SESSIONS.remove(sessionId);
            }
        });
    }

    /**
     * Opens (or refreshes) the root menu dialog for the provided NPC.
     */
    public static void openMainDialog(Player player, NPC npc) {
        PlayerData data = dataService().get(player);
        if (data == null) {
            return;
        }
        Session session = prepareSession(player, npc);
        session.reset();
        SessionContext context = new SessionContext(session, player, npc, data);

        boolean hasRandomDialogues = !npc.randomDialogues().isEmpty();
        List<TalkObjectiveEntry> talks = collectTalkObjectives(data, npc.id());
        List<QuestProgressEntry> toValidate = collectQuestsToAdvance(data, npc.id());
        List<Quest> available = collectAvailableQuests(data, npc.id());

        List<DialogBody> body = new ArrayList<>();
        body.add(new DialogBody.PlainMessage(npc.name().decorate(TextDecoration.BOLD), DialogBody.PlainMessage.DEFAULT_WIDTH));
        body.add(new DialogBody.PlainMessage(UiText.MENU_DESCRIPTION, DialogBody.PlainMessage.DEFAULT_WIDTH));
        if (hasRandomDialogues) {
            body.add(new DialogBody.PlainMessage(
                    inlineAction(context, UiText.BUTTON_TALK, UiText.BUTTON_TALK_TOOLTIP, NpcDialogService::showRandomGreeting),
                    DialogBody.PlainMessage.DEFAULT_WIDTH));
        } else {
            body.add(new DialogBody.PlainMessage(UiText.TALK_UNAVAILABLE, DialogBody.PlainMessage.DEFAULT_WIDTH));
        }
        appendMainSummary(context, body, hasRandomDialogues, talks.size(), toValidate.size(), available.size());

        List<TalkObjectiveEntry> talkEntries = List.copyOf(talks);
        List<QuestProgressEntry> turnInEntries = List.copyOf(toValidate);
        List<Quest> availableQuests = List.copyOf(available);

        List<DialogActionButton> entries = new ArrayList<>();
        entries.add(createButton(context,
                categoryLabel(UiText.BUTTON_QUEST_DIALOG, talkEntries.size(), !talkEntries.isEmpty()),
                UiText.BUTTON_QUEST_DIALOG_TOOLTIP,
                ctx -> {
                    if (talkEntries.isEmpty()) {
                        showEmptyCategory(ctx, UiText.TITLE_DIALOG, UiText.CATEGORY_EMPTY_TALKS);
                    } else {
                        showTalkObjectives(ctx, talkEntries);
                    }
                }));

        entries.add(createButton(context,
                categoryLabel(UiText.BUTTON_TURN_IN, turnInEntries.size(), !turnInEntries.isEmpty()),
                UiText.BUTTON_TURN_IN_TOOLTIP,
                ctx -> {
                    if (turnInEntries.isEmpty()) {
                        showEmptyCategory(ctx, UiText.TITLE_VALIDATION, UiText.CATEGORY_EMPTY_TURN_INS);
                    } else {
                        showQuestTurnIns(ctx, turnInEntries);
                    }
                }));

        entries.add(createButton(context,
                categoryLabel(UiText.BUTTON_AVAILABLE, availableQuests.size(), !availableQuests.isEmpty()),
                UiText.BUTTON_AVAILABLE_TOOLTIP,
                ctx -> {
                    if (availableQuests.isEmpty()) {
                        showEmptyCategory(ctx, UiText.TITLE_AVAILABLE, UiText.CATEGORY_EMPTY_AVAILABLE);
                    } else {
                        showAvailableQuests(ctx, availableQuests);
                    }
                }));

        DialogActionButton closeButton = createButton(context,
                UiText.BUTTON_CLOSE,
                UiText.CLOSE_DIALOG_TOOLTIP,
                NpcDialogService::closeSession);

        DialogMetadata metadata = new DialogMetadata(
                UiText.TITLE_INTERACTION,
                Component.empty(),
                true,
                false,
                DialogAfterAction.NONE,
                body,
                Collections.emptyList()
        );

        Dialog dialog = new Dialog.MultiAction(metadata, entries, closeButton, Math.max(1, Math.min(entries.size(), 4)));
        context.player.showDialog(dialog);
    }

    /**
     * Delegated from {@code /npcdialog <session> <action>} buttons.
     */
    public static void executeAction(Player player, long sessionId, String actionKey) {
        Session session = SESSIONS.get(sessionId);
        if (session == null || !session.playerId.equals(player.getUuid())) {
            return;
        }
        NPC npc = NpcRegistry.byId(session.npcId);
        if (npc == null) {
            closeSessionInternal(session, player);
            return;
        }
        PlayerData data = dataService().get(player);
        if (data == null) {
            closeSessionInternal(session, player);
            return;
        }
        Consumer<SessionContext> action = session.actions.get(actionKey);
        if (action == null) {
            return;
        }
        SessionContext context = new SessionContext(session, player, npc, data);
        action.accept(context);
    }

    /**
     * Displays a standalone dialog (no menu/session) – used for quest narration callbacks.
     */
    public static void showNarration(Player player, Component title, List<Component> lines) {
        List<DialogBody> body = new ArrayList<>();
        if (lines.isEmpty()) {
            body.add(EMPTY_MESSAGE);
        } else {
            for (Component line : lines) {
                body.add(new DialogBody.PlainMessage(line, DialogBody.PlainMessage.DEFAULT_WIDTH));
            }
        }
        DialogMetadata metadata = new DialogMetadata(
                title,
                Component.empty(),
                true,
                false,
                DialogAfterAction.NONE,
                body,
                Collections.emptyList()
        );
        player.showDialog(new Dialog.Notice(metadata, Dialog.Notice.DEFAULT_ACTION));
    }

    // region Session helpers

    private static Session prepareSession(Player player, NPC npc) {
        Long existingId = PLAYER_ACTIVE_SESSION.get(player.getUuid());
        if (existingId != null) {
            Session session = SESSIONS.get(existingId);
            if (session != null && session.npcId.equals(npc.id())) {
                return session;
            }
            if (session != null) {
                closeSessionInternal(session, player);
            }
        }
        Session fresh = new Session(SESSION_SEQUENCE.incrementAndGet(), player.getUuid(), npc.id());
        SESSIONS.put(fresh.id, fresh);
        PLAYER_ACTIVE_SESSION.put(player.getUuid(), fresh.id);
        return fresh;
    }

    private static DialogActionButton createButton(SessionContext context,
                                                   Component label,
                                                   Component tooltip,
                                                   Consumer<SessionContext> action) {
        String key = context.session.registerAction(action);
        String command = "/npcdialog " + context.session.id + " " + key;
        return new DialogActionButton(
                label,
                tooltip,
                DialogActionButton.DEFAULT_WIDTH,
                new DialogAction.RunCommand(command)
        );
    }

    private static void closeSession(SessionContext context) {
        closeSessionInternal(context.session, context.player);
    }

    private static void closeSessionInternal(Session session, Player player) {
        SESSIONS.remove(session.id);
        PLAYER_ACTIVE_SESSION.remove(session.playerId, session.id);
        player.closeDialog();
    }

    // endregion

    // region View logic

    private static void appendMainSummary(SessionContext context,
                                          List<DialogBody> body,
                                          boolean hasRandomDialogues,
                                          int talkCount,
                                          int turnInCount,
                                          int availableCount) {
        body.add(new DialogBody.PlainMessage(UiText.SUMMARY_TITLE, DialogBody.PlainMessage.DEFAULT_WIDTH));
        body.add(new DialogBody.PlainMessage(summaryLine("Niveau : ", context.data.level), DialogBody.PlainMessage.DEFAULT_WIDTH));
        body.add(new DialogBody.PlainMessage(summaryLine("Quêtes suivies : ", context.data.quests.size()), DialogBody.PlainMessage.DEFAULT_WIDTH));
        body.add(new DialogBody.PlainMessage(summaryLine("Dialogues en cours : ", talkCount), DialogBody.PlainMessage.DEFAULT_WIDTH));
        body.add(new DialogBody.PlainMessage(summaryLine("Validations disponibles : ", turnInCount), DialogBody.PlainMessage.DEFAULT_WIDTH));
        body.add(new DialogBody.PlainMessage(summaryLine("Quêtes proposées : ", availableCount), DialogBody.PlainMessage.DEFAULT_WIDTH));
        body.add(new DialogBody.PlainMessage(summaryStatus("Discussions libres : ", hasRandomDialogues), DialogBody.PlainMessage.DEFAULT_WIDTH));
    }

    private static Component summaryLine(String label, int value) {
        return Component.text(label, MUTED_STYLE)
                .append(Component.text(Integer.toString(value), SECONDARY_STYLE));
    }

    private static Component summaryLine(String label, Component value) {
        return Component.text(label, MUTED_STYLE)
                .append(value);
    }

    private static Component summaryStatus(String label, boolean enabled) {
        Style valueStyle = enabled ? SECONDARY_STYLE : MUTED_STYLE;
        String valueText = enabled ? "Oui" : "Non";
        return Component.text(label, MUTED_STYLE)
                .append(Component.text(valueText, valueStyle));
    }

    private static Component inlineAction(SessionContext context,
                                          Component label,
                                          Component tooltip,
                                          Consumer<SessionContext> action) {
        String key = context.session.registerAction(action);
        String command = "/npcdialog " + context.session.id + " " + key;
        Component clickable = label
                .decoration(TextDecoration.UNDERLINED, true)
                .clickEvent(ClickEvent.runCommand(command))
                .hoverEvent(HoverEvent.showText(tooltip));
        return Component.text()
                .append(Component.text("▶ ", PRIMARY_STYLE))
                .append(clickable)
                .build();
    }

    private static Component categoryLabel(Component baseLabel, int count, boolean hasContent) {
        Style labelStyle = hasContent ? SECONDARY_STYLE : MUTED_STYLE;
        Component title = baseLabel.style(labelStyle);
        NamedTextColor countColor = hasContent ? NamedTextColor.AQUA : NamedTextColor.GRAY;
        Component countComponent = Component.text(" (" + count + ")", countColor);
        return Component.text()
                .append(title)
                .append(countComponent)
                .build();
    }

    private static Component cooldownValue(Duration duration) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            return Component.text("Aucun", MUTED_STYLE);
        }

        long totalSeconds = duration.toSeconds();
        long minutes = totalSeconds / 60;
        long hours = minutes / 60;
        minutes %= 60;
        long seconds = totalSeconds % 60;

        StringBuilder builder = new StringBuilder();
        if (hours > 0) {
            builder.append(hours).append(" h");
        }
        if (minutes > 0) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(minutes).append(" min");
        }
        if (seconds > 0 && hours == 0) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(seconds).append(" s");
        }
        if (builder.length() == 0) {
            builder.append("1 s");
        }

        return Component.text(builder.toString(), SECONDARY_STYLE);
    }

    private static void showRandomGreeting(SessionContext context) {
        List<Component> dialogues = context.npc.randomDialogues();
        Component line = dialogues.get(ThreadLocalRandom.current().nextInt(dialogues.size()));
        showNarration(context, UiText.TITLE_DISCUSSION, List.of(line), ctx -> openMainDialog(ctx.player, ctx.npc));
    }

    private static void showTalkObjectives(SessionContext context, List<TalkObjectiveEntry> entries) {
        context.session.reset();

        List<DialogBody> body = List.of(new DialogBody.PlainMessage(
                UiText.TITLE_TALK_OBJECTIVES,
                DialogBody.PlainMessage.DEFAULT_WIDTH));

        List<DialogActionButton> buttons = new ArrayList<>();
        for (TalkObjectiveEntry entry : entries) {
            Component label = entry.quest().name.style(SECONDARY_STYLE);
            Component tooltip = UiText.TOOLTIP_CONTINUE_QUEST;
            buttons.add(createButton(context, label, tooltip, ctx -> resolveTalkObjective(ctx, entry)));
        }

        if (buttons.isEmpty()) {
            showEmptyCategory(context, UiText.TITLE_DIALOG, UiText.CATEGORY_EMPTY_TALKS);
            return;
        }

        DialogActionButton back = createButton(context,
                UiText.BUTTON_BACK,
                UiText.TO_MENU_TOOLTIP,
                ctx -> openMainDialog(ctx.player, ctx.npc));

        DialogMetadata metadata = new DialogMetadata(
                UiText.TITLE_DIALOG,
                Component.empty(),
                true,
                false,
                DialogAfterAction.NONE,
                body,
                Collections.emptyList()
        );

        Dialog dialog = new Dialog.MultiAction(metadata, buttons, back, Math.max(1, Math.min(buttons.size(), 3)));
        context.player.showDialog(dialog);
    }

    private static void resolveTalkObjective(SessionContext context, TalkObjectiveEntry entry) {
        QuestProgress progress = entry.progress();
        TalkObjective objective = entry.objective();
        if (progress.isObjectiveCompleted(objective)) {
            openMainDialog(context.player, context.npc);
            return;
        }

        progress.setObjectiveCompleted(objective, true);
        QuestManager.getEventNode().call(new QuestObjectiveCompleteEvent(
                context.player,
                progress,
                entry.step(),
                objective
        ));

        List<Component> lines = objective.getDialogues();
        showNarration(context,
                entry.quest().name,
                lines,
                ctx -> openMainDialog(ctx.player, ctx.npc));
    }

    private static void showQuestTurnIns(SessionContext context, List<QuestProgressEntry> entries) {
        context.session.reset();

        List<DialogBody> body = List.of(new DialogBody.PlainMessage(
                UiText.TITLE_TURN_INS,
                DialogBody.PlainMessage.DEFAULT_WIDTH));

        List<DialogActionButton> buttons = new ArrayList<>();
        for (QuestProgressEntry entry : entries) {
            Component label = entry.quest().name.style(SECONDARY_STYLE);
            Component tooltip = UiText.TOOLTIP_SUBMIT_QUEST;
            buttons.add(createButton(context, label, tooltip, ctx -> {
                QuestManager.tryAdvanceQuestByNpc(ctx.player, ctx.data, entry.quest(), entry.progress(), ctx.npc.id());
                openMainDialog(ctx.player, ctx.npc);
            }));
        }

        if (buttons.isEmpty()) {
            showEmptyCategory(context, UiText.TITLE_VALIDATION, UiText.CATEGORY_EMPTY_TURN_INS);
            return;
        }

        DialogActionButton back = createButton(context,
                UiText.BUTTON_BACK,
                UiText.TO_MENU_TOOLTIP,
                ctx -> openMainDialog(ctx.player, ctx.npc));

        DialogMetadata metadata = new DialogMetadata(
                UiText.TITLE_VALIDATION,
                Component.empty(),
                true,
                false,
                DialogAfterAction.NONE,
                body,
                Collections.emptyList()
        );

        Dialog dialog = new Dialog.MultiAction(metadata, buttons, back, Math.max(1, Math.min(buttons.size(), 3)));
        context.player.showDialog(dialog);
    }

    private static void showAvailableQuests(SessionContext context, List<Quest> quests) {
        context.session.reset();

        List<DialogBody> body = List.of(new DialogBody.PlainMessage(
                UiText.TITLE_AVAILABLE_LIST,
                DialogBody.PlainMessage.DEFAULT_WIDTH));

        List<DialogActionButton> buttons = new ArrayList<>();
        for (Quest quest : quests) {
            Component label = quest.name.style(SECONDARY_STYLE);
            Component tooltip = UiText.BUTTON_VIEW_QUEST_TOOLTIP;
            buttons.add(createButton(context, label, tooltip, ctx -> showQuestPreview(ctx, quest)));
        }

        if (buttons.isEmpty()) {
            showEmptyCategory(context, UiText.TITLE_AVAILABLE, UiText.CATEGORY_EMPTY_AVAILABLE);
            return;
        }

        DialogActionButton back = createButton(context,
                UiText.BUTTON_BACK,
                UiText.TO_MENU_TOOLTIP,
                ctx -> openMainDialog(ctx.player, ctx.npc));

        DialogMetadata metadata = new DialogMetadata(
                UiText.TITLE_AVAILABLE,
                Component.empty(),
                true,
                false,
                DialogAfterAction.NONE,
                body,
                Collections.emptyList()
        );

        Dialog dialog = new Dialog.MultiAction(metadata, buttons, back, Math.max(1, Math.min(buttons.size(), 3)));
        context.player.showDialog(dialog);
    }

    private static void showQuestPreview(SessionContext context, Quest quest) {
        context.session.reset();

        List<DialogBody> body = new ArrayList<>();
        Component questTitle = quest.name.style(PRIMARY_STYLE);
        body.add(new DialogBody.PlainMessage(questTitle, DialogBody.PlainMessage.DEFAULT_WIDTH));
        if (quest.description != null) {
            body.add(new DialogBody.PlainMessage(quest.description, DialogBody.PlainMessage.DEFAULT_WIDTH));
        }

        body.add(new DialogBody.PlainMessage(summaryLine("Étapes : ", quest.steps.size()), DialogBody.PlainMessage.DEFAULT_WIDTH));
        int requiredLevel = quest.steps.stream().mapToInt(step -> step.requiredLevel).max().orElse(0);
        body.add(new DialogBody.PlainMessage(summaryLine("Niveau requis : ", requiredLevel), DialogBody.PlainMessage.DEFAULT_WIDTH));
        body.add(new DialogBody.PlainMessage(summaryStatus("Répétable : ", quest.repeatable), DialogBody.PlainMessage.DEFAULT_WIDTH));
        if (quest.repeatable) {
            body.add(new DialogBody.PlainMessage(summaryLine("Cooldown : ", cooldownValue(quest.cooldown)), DialogBody.PlainMessage.DEFAULT_WIDTH));
        }

        if (!quest.steps.isEmpty()) {
            QuestStep firstStep = quest.steps.getFirst();
            Component nextStep = firstStep.description != null
                    ? firstStep.description
                    : firstStep.name != null
                    ? firstStep.name
                    : Component.text("Commencez auprès de ce PNJ.", MUTED_STYLE);
            body.add(new DialogBody.PlainMessage(summaryLine("Première étape : ", nextStep), DialogBody.PlainMessage.DEFAULT_WIDTH));
        }

        List<DialogActionButton> buttons = new ArrayList<>();
        buttons.add(createButton(context,
                UiText.BUTTON_ACCEPT,
                UiText.BUTTON_ACCEPT_TOOLTIP,
                ctx -> {
                    QuestManager.tryStartQuest(ctx.player, ctx.data, quest, ctx.npc.id());
                    openMainDialog(ctx.player, ctx.npc);
                }));

        DialogActionButton back = createButton(context,
                UiText.BUTTON_DECLINE,
                UiText.BUTTON_DECLINE_TOOLTIP,
                ctx -> showAvailableQuests(ctx, collectAvailableQuests(ctx.data, ctx.npc.id())));

        DialogMetadata metadata = new DialogMetadata(
                quest.name,
                UiText.SUBTITLE_QUEST_PREVIEW,
                true,
                false,
                DialogAfterAction.NONE,
                body,
                Collections.emptyList()
        );

        Dialog dialog = new Dialog.MultiAction(metadata, buttons, back, Math.max(1, buttons.size()));
        context.player.showDialog(dialog);
    }

    private static void showNarration(SessionContext context,
                                      Component title,
                                      List<Component> lines,
                                      Consumer<SessionContext> onClose) {
        List<Component> pages = (lines == null || lines.isEmpty())
                ? List.of(Component.text("Aucun contenu disponible.", MUTED_STYLE))
                : List.copyOf(lines);
        showNarrationPage(context, title, pages, 0, onClose);
    }

    private static void showNarrationPage(SessionContext context,
                                          Component title,
                                          List<Component> pages,
                                          int index,
                                          Consumer<SessionContext> onClose) {
        context.session.reset();

        List<DialogBody> body = new ArrayList<>();
        Component content = pages.get(index);
        if (content == null) {
            content = Component.empty();
        }
        body.add(new DialogBody.PlainMessage(content, DialogBody.PlainMessage.DEFAULT_WIDTH));

        Component subtitle = Component.text((index + 1) + " / " + pages.size(), MUTED_STYLE);

        List<DialogActionButton> buttons = new ArrayList<>();
        if (index > 0) {
            buttons.add(createButton(context,
                    UiText.BUTTON_PREVIOUS,
                    UiText.BUTTON_PREVIOUS_TOOLTIP,
                    ctx -> showNarrationPage(ctx, title, pages, index - 1, onClose)));
        }

        DialogActionButton forwardButton;
        if (index < pages.size() - 1) {
            forwardButton = createButton(context,
                    UiText.BUTTON_NEXT,
                    UiText.BUTTON_NEXT_TOOLTIP,
                    ctx -> showNarrationPage(ctx, title, pages, index + 1, onClose));
        } else {
            forwardButton = createButton(context,
                    UiText.BUTTON_FINISH,
                    UiText.BUTTON_FINISH_TOOLTIP,
                    onClose);
        }
        buttons.add(forwardButton);

        DialogActionButton exitButton = createButton(context,
                UiText.BUTTON_BACK,
                UiText.TO_MENU_TOOLTIP,
                onClose);

        DialogMetadata metadata = new DialogMetadata(
                title,
                subtitle,
                true,
                false,
                DialogAfterAction.NONE,
                body,
                Collections.emptyList()
        );

        Dialog dialog = new Dialog.MultiAction(metadata, buttons, exitButton, Math.max(1, Math.min(buttons.size(), 2)));
        context.player.showDialog(dialog);
    }

    private static void showEmptyCategory(SessionContext context,
                                          Component title,
                                          Component message) {
        showNarration(context, title, List.of(message), ctx -> openMainDialog(ctx.player, ctx.npc));
    }

    private static void showNotice(SessionContext context,
                                   Component title,
                                   List<Component> lines,
                                   Consumer<SessionContext> onClose) {
        showNarration(context, title, lines, onClose);
    }

    // endregion

    // region Data collection

    private static List<TalkObjectiveEntry> collectTalkObjectives(PlayerData data, String npcId) {
        List<TalkObjectiveEntry> result = new ArrayList<>();
        for (QuestProgress progress : data.quests) {
            Quest quest = QuestRegistry.byId(progress.questId);
            if (quest == null || progress.stepIndex >= quest.steps.size()) continue;

            QuestStep currentStep = quest.steps.get(progress.stepIndex);
            for (IQuestObjective objective : currentStep.objectives) {
                if (objective instanceof TalkObjective talk &&
                        talk.getNpcId().equals(npcId) &&
                        !progress.isObjectiveCompleted(objective)) {
                    result.add(new TalkObjectiveEntry(quest, progress, currentStep, talk));
                }
            }
        }
        return result;
    }

    private static List<QuestProgressEntry> collectQuestsToAdvance(PlayerData data, String npcId) {
        List<QuestProgressEntry> result = new ArrayList<>();
        for (QuestProgress progress : data.quests) {
            Quest quest = QuestRegistry.byId(progress.questId);
            if (quest == null || progress.stepIndex >= quest.steps.size()) continue;
            QuestStep currentStep = quest.steps.get(progress.stepIndex);
            boolean allObjectivesCompleted = currentStep.objectives.stream().allMatch(progress::isObjectiveCompleted);
            if (allObjectivesCompleted && npcId.equals(currentStep.endNpc)) {
                result.add(new QuestProgressEntry(quest, progress));
            }
        }
        return result;
    }

    private static List<Quest> collectAvailableQuests(PlayerData data, String npcId) {
        List<Quest> result = new ArrayList<>();
        for (Quest quest : QuestRegistry.byStartNpc(npcId)) {
            if (quest.steps.isEmpty()) continue;
            boolean hasQuest = data.quests.stream().anyMatch(p -> p.questId.equals(quest.id));
            boolean completed = data.hasCompletedQuest(quest.id);
            boolean failed = data.hasFailedQuest(quest.id);
            if (!hasQuest && !completed && !failed) {
                QuestStep firstStep = quest.steps.getFirst();
                if (data.level >= firstStep.requiredLevel && QuestManager.checkPrerequisites(data, firstStep)) {
                    result.add(quest);
                }
            }
        }
        return result;
    }

    // endregion

    private static PlayerDataService dataService() {
        return GameContext.get().playerDataService();
    }

    // region Internal types

    private static final class Session {
        final long id;
        final UUID playerId;
        final String npcId;

        private final AtomicInteger sequence = new AtomicInteger();
        private final Map<String, Consumer<SessionContext>> actions = new ConcurrentHashMap<>();

        Session(long id, UUID playerId, String npcId) {
            this.id = id;
            this.playerId = playerId;
            this.npcId = npcId;
        }

        void reset() {
            actions.clear();
            sequence.set(0);
        }

        String registerAction(Consumer<SessionContext> action) {
            String key = Integer.toString(sequence.incrementAndGet(), 36);
            actions.put(key, action);
            return key;
        }
    }

    private static final class SessionContext {
        final Session session;
        final Player player;
        final NPC npc;
        final PlayerData data;

        SessionContext(Session session, Player player, NPC npc, PlayerData data) {
            this.session = session;
            this.player = player;
            this.npc = npc;
            this.data = data;
        }
    }

    private record TalkObjectiveEntry(Quest quest,
                                      QuestProgress progress,
                                      QuestStep step,
                                      TalkObjective objective) {
    }

    private record QuestProgressEntry(Quest quest,
                                      QuestProgress progress) {
    }

    // endregion
}
