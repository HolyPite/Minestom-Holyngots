package org.example.mmo.quests;

import net.kyori.adventure.text.Component;

import java.time.Duration;
import java.util.List;

/**
 * Représente une étape d'une quête.
 * Cette classe est principalement un conteneur de données
 * et ne contient pas de logique particulière.
 */
public class QuestStep {
    public String description;
    public String objective;
    public String startNpc;
    public String endNpc;
    public List<String> prerequisites;
    public Duration delay = Duration.ZERO;
    public Duration duration = Duration.ZERO;
    public int attemptLimit = 0;
    public List<Component> successDialogues = List.of();
    public List<Component> failureDialogues = List.of();
    public List<Component> waitingDialogues = List.of();
    public List<Component> delayDialogues = List.of();

    public QuestStep() {
    }
}
