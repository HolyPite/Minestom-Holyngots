package org.example.mmo.dev.advancementui;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.advancements.Advancement;
import net.minestom.server.advancements.AdvancementManager;
import net.minestom.server.advancements.AdvancementRoot;
import net.minestom.server.advancements.AdvancementTab;
import net.minestom.server.item.ItemStack;
import org.example.mmo.dev.advancementui.model.SkillNodePrototype;
import org.example.mmo.dev.advancementui.model.SkillTreeDisplayPrototype;
import org.example.mmo.dev.advancementui.model.SkillTreePrototype;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Instancie un {@link AdvancementTab} à partir d'un {@link SkillTreePrototype}.
 */
public final class AdvancementTabFactory {

    private final AdvancementManager advancementManager = MinecraftServer.getAdvancementManager();

    public PlayerAdvancementTab create(UUID playerId, SkillTreePrototype prototype) {
        String identifier = buildIdentifier(playerId, prototype.id());
        AdvancementRoot root = createRoot(prototype.display());
        AdvancementTab tab = advancementManager.createTab(identifier, root);
        Map<String, Advancement> nodes = attachNodes(tab, prototype);
        return new PlayerAdvancementTab(identifier, tab, new java.util.concurrent.ConcurrentHashMap<>(nodes), prototype);
    }

    private AdvancementRoot createRoot(SkillTreeDisplayPrototype display) {
        return new AdvancementRoot(
                display.title(),
                display.description(),
                ItemStack.of(display.icon()),
                display.frameType(),
                display.x(),
                display.y(),
                display.background()
        );
    }

    private Map<String, Advancement> attachNodes(AdvancementTab tab, SkillTreePrototype prototype) {
        Map<String, Advancement> resolved = new HashMap<>();
        Set<String> remaining = new HashSet<>(prototype.nodes().keySet());
        Advancement root = tab.getRoot();

        while (!remaining.isEmpty()) {
            int addedThisPass = 0;
            for (String nodeId : Set.copyOf(remaining)) {
                SkillNodePrototype nodePrototype = prototype.nodes().get(nodeId);
                if (nodePrototype.secret()) {
                    remaining.remove(nodeId);
                    continue;
                }
                Advancement parent = nodePrototype.parentId() == null ? root : resolved.get(nodePrototype.parentId());
                if (parent == null) {
                    continue;
                }
                Advancement advancement = createAdvancement(nodePrototype);
                tab.createAdvancement(nodeIdentifier(prototype.id(), nodePrototype.id()), advancement, parent);
                resolved.put(nodeId, advancement);
                remaining.remove(nodeId);
                addedThisPass++;
            }
            if (addedThisPass == 0) {
                break;
            }
        }
        return resolved;
    }

    private Advancement createAdvancement(SkillNodePrototype prototype) {
        Advancement advancement = new Advancement(
                prototype.title(),
                prototype.description(),
                ItemStack.of(prototype.icon()),
                prototype.frameType(),
                prototype.x(),
                prototype.y()
        );
        advancement.showToast(prototype.toast());
        advancement.setHidden(prototype.hidden());
        return advancement;
    }

    private String buildIdentifier(UUID playerId, String treeId) {
        return ("skills/" + treeId + "/" + playerId).toLowerCase(Locale.ROOT);
    }

    private String nodeIdentifier(String treeId, String nodeId) {
        return ("skills/" + treeId + "/" + nodeId).toLowerCase(Locale.ROOT);
    }

    public boolean revealNode(PlayerAdvancementTab playerTab, SkillNodePrototype prototype) {
        Advancement parent = prototype.parentId() == null
                ? playerTab.tab().getRoot()
                : playerTab.nodes().get(prototype.parentId());
        if (parent == null) {
            return false;
        }
        Advancement advancement = createAdvancement(prototype);
        playerTab.tab().createAdvancement(nodeIdentifier(playerTab.prototype().id(), prototype.id()), advancement, parent);
        playerTab.nodes().put(prototype.id(), advancement);
        return true;
    }
}
