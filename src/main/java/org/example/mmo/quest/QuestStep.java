package org.example.mmo.quest;

import net.kyori.adventure.text.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Represents one step of a quest.
 * This class is a data container defining objectives, rewards, conditions, and dialogues for a single step.
 */
public class QuestStep {

    /** The name of the quest step. */
    public Component name;

    /** Text displayed in the quest log when the player has finished the step but not yet started the next one. */
    public Component endDescription;

    /** Description of what the player needs to do, displayed in the quest log. */
    public Component description;

    /** ID of the starting NPC to talk to to get the step. */
    public String startNpc;

    /** ID of the ending NPC to talk to to complete the step. */
    public String endNpc;

    /** List of objectives the player must complete for this step. */
    public List<IQuestObjective> objectives = Collections.emptyList();

    /** List of rewards given to the player for completing the step. */
    public List<IQuestReward> rewards = Collections.emptyList();

    /** List of prerequisite IDs (e.g., "questId:stepIndex") the player must have completed to unlock this step. */
    public List<String> prerequisites = Collections.emptyList();

    /** Time the player must wait since the end of the previous step to accept this one. */
    public Duration delay = Duration.ZERO;

    /** Time limit within which the player must complete the step, otherwise they fail. */
    public Duration duration = Duration.ZERO;

    /** Maximum number of attempts the player can make to complete the step. */
    public int attemptLimit = 0;

    /** If true, on failure, the player is redirected to the quest specified in failureRedirectionQuest. If false, progress is blocked. */
    public boolean failureRedirection = false;

    /** ID of the quest to which the player is redirected on failure if failureRedirection is true. */
    public String failureRedirectionQuest;

    /** Dialogues displayed on successful completion of the step. */
    public List<Component> successDialogues = Collections.emptyList();

    /** Dialogues displayed when the player tries to complete the step but has not met the requirements. */
    public List<Component> waitingDialogues = Collections.emptyList();

    /** Dialogues displayed when the player fails the step (e.g., time runs out). */
    public List<Component> failureDialogues = Collections.emptyList();

    /** Dialogues displayed if the player cannot yet accept the step due to a delay. */
    public List<Component> delayDialogues = Collections.emptyList();

    public QuestStep() {
    }
}
