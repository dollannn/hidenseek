package se.playpark.dhs.game.listener;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import se.playpark.dhs.Main;
import se.playpark.dhs.game.util.Status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static se.playpark.dhs.configuration.Config.*;
import static se.playpark.dhs.configuration.Localization.message;

public class ElectionHandler implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || !Main.getInstance().getBoard().contains(player)) {
            return;
        }

        if (Main.getInstance().getGame().getStatus() != Status.STANDBY) {
            return;
        }

        if (!electionEnabled || !player.hasPermission(electionPermission)) {
            return;
        }

        String itemName = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        if (itemName.equals("Randomize Seekers")) {
            event.setCancelled(true);
            randomizeSeekers(player);
        } else if (itemName.equals("Start Game")) {
            event.setCancelled(true);
            startGameWithSelectedSeekers(player);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || !Main.getInstance().getBoard().contains(player)) {
            return;
        }

        if (Main.getInstance().getGame().getStatus() != Status.STANDBY) {
            return;
        }

        if (!electionEnabled || !player.hasPermission(electionPermission)) {
            return;
        }

        if (!(event.getRightClicked() instanceof Player)) {
            return;
        }

        Player target = (Player) event.getRightClicked();
        if (!Main.getInstance().getBoard().contains(target)) {
            return;
        }

        String itemName = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        if (itemName.equals("Select Seeker")) {
            event.setCancelled(true);
            selectSeeker(player, target);
        } else if (itemName.equals("Deselect Seeker")) {
            event.setCancelled(true);
            deselectSeeker(player, target);
        }
    }

    private void selectSeeker(Player admin, Player target) {
        if (Main.getInstance().getBoard().isPotentialSeeker(target)) {
            admin.sendMessage(messagePrefix + target.getName() + " is already selected as a seeker!");
            return;
        }

        Main.getInstance().getBoard().addPotentialSeeker(target);
        admin.sendMessage(messagePrefix + "Selected " + target.getName() + " as a potential seeker!");
        target.sendMessage(messagePrefix + "You have been selected as a potential seeker by " + admin.getName() + "!");

        // Update lobby boards to show the selection
        Main.getInstance().getBoard().reloadLobbyBoards();
    }

    private void deselectSeeker(Player admin, Player target) {
        if (!Main.getInstance().getBoard().isPotentialSeeker(target)) {
            admin.sendMessage(messagePrefix + target.getName() + " is not selected as a seeker!");
            return;
        }

        Main.getInstance().getBoard().removePotentialSeeker(target);
        admin.sendMessage(messagePrefix + "Deselected " + target.getName() + " as a potential seeker!");
        target.sendMessage(
                messagePrefix + "You have been deselected as a potential seeker by " + admin.getName() + "!");

        // Update lobby boards to show the deselection
        Main.getInstance().getBoard().reloadLobbyBoards();
    }

    private void randomizeSeekers(Player admin) {
        Main.getInstance().getBoard().clearPotentialSeekers();

        List<Player> players = new ArrayList<>(Main.getInstance().getBoard().getPlayers());
        Collections.shuffle(players);

        int seekersToSelect = Math.min(startingSeekerCount, players.size());
        for (int i = 0; i < seekersToSelect; i++) {
            Main.getInstance().getBoard().addPotentialSeeker(players.get(i));
        }

        admin.sendMessage(messagePrefix + "Randomized " + seekersToSelect + " seekers!");
        Main.getInstance().getGame().broadcastMessage(messagePrefix + admin.getName() + " randomized the seekers!");

        // Update lobby boards to show the randomization
        Main.getInstance().getBoard().reloadLobbyBoards();
    }

    private void startGameWithSelectedSeekers(Player admin) {
        List<Player> selectedSeekers = Main.getInstance().getBoard().getPotentialSeekers();

        if (selectedSeekers.isEmpty()) {
            admin.sendMessage(
                    errorPrefix + "No seekers have been selected! Use the selection items or randomize first.");
            return;
        }

        if (Main.getInstance().getBoard().size() < minPlayers) {
            admin.sendMessage(errorPrefix + message("START_MIN_PLAYERS").addAmount(minPlayers));
            return;
        }

        // Clear potential seekers since we're starting the game
        Main.getInstance().getBoard().clearPotentialSeekers();

        // Start the game with the selected seekers
        Main.getInstance().getGame().start(selectedSeekers);
    }
}