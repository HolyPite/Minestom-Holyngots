package org.example.mmo.npc.dialog;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
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
import org.example.NodesManagement;
import org.example.data.PlayerDataService;
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

    private static final Component MENU_DESCRIPTION =
            Component.text("Choisissez une interaction.", NamedTextColor.GRAY);

    private static final Style PRIMARY_STYLE = Style.style(NamedTextColor.GOLD);
    private static final Style SECONDARY_STYLE = Style.style(NamedTextColor.AQUA);
    private static final Style MUTED_STYLE = Style.style(NamedTextColor.GRAY);

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

        List<DialogBody> body = new ArrayList<>();
        body.add(new DialogBody.PlainMessage(npc.name().style(PRIMARY_STYLE), DialogBody.PlainMessage.DEFAULT_WIDTH));
        body.add(new DialogBody.PlainMessage(MENU_DESCRIPTION, DialogBody.PlainMessage.DEFAULT_WIDTH));

        List<DialogActionButton> entries = new ArrayList<>();
        if (!npc.randomDialogues().isEmpty()) {
            entries.add(createButton(context,
                    Component.text("Parler", PRIMARY_STYLE),
                    Component.text("Discuter avec le PNJ.", MUTED_STYLE),
                    NpcDialogService::showRandomGreeting));
        }

        List<TalkObjectiveEntry> talks = collectTalkObjectives(data, npc.id());
        if (!talks.isEmpty()) {
            entries.add(createButton(context,
                    Component.text("Dialogue de quêtes", SECONDARY_STYLE),
                    Component.text("Progresser sur vos objectifs de dialogue.", MUTED_STYLE),
                    ctx -> showTalkObjectives(ctx, talks)));
        }

        List<QuestProgressEntry> toValidate = collectQuestsToAdvance(data, npc.id());
        if (!toValidate.isEmpty()) {
            entries.add(createButton(context,
                    Component.text("Quêtes à valider", SECONDARY_STYLE),
                    Component.text("Rendre vos quêtes terminées.", MUTED_STYLE),
                    ctx -> showQuestTurnIns(ctx, toValidate)));
        }

        List<Quest> available = collectAvailableQuests(data, npc.id());
        if (!available.isEmpty()) {
            entries.add(createButton(context,
                    Component.text("Quêtes disponibles", SECONDARY_STYLE),
                    Component.text("Examiner les nouvelles quêtes.", MUTED_STYLE),
                    ctx -> showAvailableQuests(ctx, available)));
        }

        if (entries.isEmpty()) {
            entries.add(createButton(context,
                    Component.text("Rien à faire", MUTED_STYLE),
                    Component.text("Ce PNJ n'a aucune interaction pour le moment.", MUTED_STYLE),
                    NpcDialogService::closeSession));
        }

        DialogActionButton closeButton = createButton(context,
                Component.text("Fermer", MUTED_STYLE),
                Component.text("Fermer le dialogue.", MUTED_STYLE),
                NpcDialogService::closeSession);

        DialogMetadata metadata = new DialogMetadata(
                Component.text("Interaction", PRIMARY_STYLE),
                Component.empty(),
                true,
                false,
                DialogAfterAction.NONE,
                body,
                Collections.emptyList()
        );

        Dialog dialog = new Dialog.MultiAction(metadata, entries, closeButton, Math.min(entries.size(), 2));
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

    private static void showRandomGreeting(SessionContext context) {
        List<Component> dialogues = context.npc.randomDialogues();
        Component line = dialogues.get(ThreadLocalRandom.current().nextInt(dialogues.size()));
        showNotice(context, Component.text("Discussion", PRIMARY_STYLE), List.of(line), ctx -> openMainDialog(ctx.player, ctx.npc));
    }

    private static void showTalkObjectives(SessionContext context, List<TalkObjectiveEntry> entries) {
        context.session.reset();

        List<DialogBody> body = List.of(new DialogBody.PlainMessage(
                Component.text("Objectifs de dialogue", PRIMARY_STYLE),
                DialogBody.PlainMessage.DEFAULT_WIDTH));

        List<DialogActionButton> buttons = new ArrayList<>();
        for (TalkObjectiveEntry entry : entries) {
            Component label = entry.quest().name.style(SECONDARY_STYLE);
            Component tooltip = Component.text("Continuer cette quête.", MUTED_STYLE);
            buttons.add(createButton(context, label, tooltip, ctx -> resolveTalkObjective(ctx, entry)));
        }

        DialogActionButton back = createButton(context,
                Component.text("Retour", MUTED_STYLE),
                Component.text("Retour au menu principal.", MUTED_STYLE),
                ctx -> openMainDialog(ctx.player, ctx.npc));

        DialogMetadata metadata = new DialogMetadata(
                Component.text("Dialogue", PRIMARY_STYLE),
                Component.empty(),
                true,
                false,
                DialogAfterAction.NONE,
                body,
                Collections.emptyList()
        );

        Dialog dialog = new Dialog.MultiAction(metadata, buttons, back, Math.min(buttons.size(), 1));
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
        showNotice(context,
                entry.quest().name,
                lines,
                ctx -> openMainDialog(ctx.player, ctx.npc));
    }

    private static void showQuestTurnIns(SessionContext context, List<QuestProgressEntry> entries) {
        context.session.reset();

        List<DialogBody> body = List.of(new DialogBody.PlainMessage(
                Component.text("Quêtes à valider", PRIMARY_STYLE),
                DialogBody.PlainMessage.DEFAULT_WIDTH));

        List<DialogActionButton> buttons = new ArrayList<>();
        for (QuestProgressEntry entry : entries) {
            Component label = entry.quest().name.style(SECONDARY_STYLE);
            Component tooltip = Component.text("Rendre cette quête.", MUTED_STYLE);
            buttons.add(createButton(context, label, tooltip, ctx -> {
                QuestManager.tryAdvanceQuestByNpc(ctx.player, ctx.data, entry.quest(), entry.progress(), ctx.npc.id());
                openMainDialog(ctx.player, ctx.npc);
            }));
        }

        DialogActionButton back = createButton(context,
                Component.text("Retour", MUTED_STYLE),
                Component.text("Retour au menu principal.", MUTED_STYLE),
                ctx -> openMainDialog(ctx.player, ctx.npc));

        DialogMetadata metadata = new DialogMetadata(
                Component.text("Validation", PRIMARY_STYLE),
                Component.empty(),
                true,
                false,
                DialogAfterAction.NONE,
                body,
                Collections.emptyList()
        );

        Dialog dialog = new Dialog.MultiAction(metadata, buttons, back, Math.min(buttons.size(), 1));
        context.player.showDialog(dialog);
    }

    private static void showAvailableQuests(SessionContext context, List<Quest> quests) {
        context.session.reset();

        List<DialogBody> body = List.of(new DialogBody.PlainMessage(
                Component.text("Quêtes disponibles", PRIMARY_STYLE),
                DialogBody.PlainMessage.DEFAULT_WIDTH));

        List<DialogActionButton> buttons = new ArrayList<>();
        for (Quest quest : quests) {
            Component label = quest.name.style(SECONDARY_STYLE);
            Component tooltip = Component.text("Accepter cette quête.", MUTED_STYLE);
            buttons.add(createButton(context, label, tooltip, ctx -> {
                QuestManager.tryStartQuest(ctx.player, ctx.data, quest, ctx.npc.id());
                openMainDialog(ctx.player, ctx.npc);
            }));
        }

        DialogActionButton back = createButton(context,
                Component.text("Retour", MUTED_STYLE),
                Component.text("Retour au menu principal.", MUTED_STYLE),
                ctx -> openMainDialog(ctx.player, ctx.npc));

        DialogMetadata metadata = new DialogMetadata(
                Component.text("Quêtes", PRIMARY_STYLE),
                Component.empty(),
                true,
                false,
                DialogAfterAction.NONE,
                body,
                Collections.emptyList()
        );

        Dialog dialog = new Dialog.MultiAction(metadata, buttons, back, Math.min(buttons.size(), 1));
        context.player.showDialog(dialog);
    }

    private static void showNotice(SessionContext context,
                                   Component title,
                                   List<Component> lines,
                                   Consumer<SessionContext> onClose) {
        context.session.reset();

        List<DialogBody> body = new ArrayList<>();
        if (lines.isEmpty()) {
            body.add(EMPTY_MESSAGE);
        } else {
            for (Component line : lines) {
                body.add(new DialogBody.PlainMessage(line, DialogBody.PlainMessage.DEFAULT_WIDTH));
            }
        }

        DialogActionButton button = createButton(context,
                Component.text("Continuer", PRIMARY_STYLE),
                Component.text("Retour au menu principal.", MUTED_STYLE),
                onClose);

        DialogMetadata metadata = new DialogMetadata(
                title,
                Component.empty(),
                true,
                false,
                DialogAfterAction.NONE,
                body,
                Collections.emptyList()
        );

        context.player.showDialog(new Dialog.Notice(metadata, button));
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
                if (QuestManager.checkPrerequisites(data, firstStep)) {
                    result.add(quest);
                }
            }
        }
        return result;
    }

    // endregion

    private static PlayerDataService dataService() {
        return NodesManagement.getDataService();
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
