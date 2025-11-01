package org.example.mmo.item;

import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.timer.TaskSchedule;
import org.example.bootstrap.GameContext;
import org.example.mmo.item.datas.Stats;

import java.util.Objects;

public class ItemEventsGlobal {
    public static void init(EventNode<Event> events){

        EventNode<PlayerEvent> playerNode = events.findChildren("playerNode",PlayerEvent.class).getFirst();

        // The InventoryItemChangeEvent listener was removed as it could fire too early
        // during the player configuration phase, causing crashes.
        // The initial stat refresh is now handled in the PlayerSpawnEvent in Main.java.

        // This listener is safe as it only fires when the player is in-game.
        playerNode.addListener(PlayerChangeHeldSlotEvent.class, e -> {
            Player p = e.getEntity();
            p.scheduler().buildTask(() -> {
                if (Objects.equals(GameContext.get().instances().nameOfGroup(GameContext.get().instances().groupFor(p.getInstance())), "games"))   {
                    Stats.refresh(p);
                }
            }).delay(TaskSchedule.tick(1)).schedule();
        });
    }
}
