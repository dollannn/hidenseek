package se.playpark.dhs.game.listener;

import se.playpark.dhs.Main;
import se.playpark.dhs.configuration.Items;
import se.playpark.dhs.game.util.Status;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import static se.playpark.dhs.configuration.Config.dropItems;
import static se.playpark.dhs.configuration.Config.regenHealth;

public class PlayerHandler implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            if (!Main.getInstance().getBoard().contains((Player) event.getEntity()))
                return;
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRegainHealth(EntityRegainHealthEvent event) {
        if (regenHealth)
            return;
        if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED
                || event.getRegainReason() == EntityRegainHealthEvent.RegainReason.REGEN) {
            if (event.getEntity() instanceof Player) {
                if (!Main.getInstance().getBoard().contains((Player) event.getEntity()))
                    return;
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemDrop(PlayerDropItemEvent event) {
        if (!dropItems && Main.getInstance().getBoard().contains(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemSpawn(ItemSpawnEvent event) {
        if (Main.getInstance().getGame().getStatus() == Status.STANDBY)
            return;
        ItemStack item = event.getEntity().getItemStack();
        if (!Items.matchItem(item))
            return;
        if (dropItems)
            return;
        event.setCancelled(true);
    }

}
