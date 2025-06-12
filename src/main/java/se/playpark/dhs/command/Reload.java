package se.playpark.dhs.command;

import se.playpark.dhs.Main;
import se.playpark.dhs.command.util.ICommand;
import se.playpark.dhs.configuration.*;
import se.playpark.dhs.game.util.Status;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static se.playpark.dhs.configuration.Config.errorPrefix;
import static se.playpark.dhs.configuration.Config.messagePrefix;
import static se.playpark.dhs.configuration.Localization.message;

public class Reload implements ICommand {

	public void execute(Player sender, String[] args) {

		if (Main.getInstance().getGame().getStatus() != Status.STANDBY) {
			sender.sendMessage(errorPrefix + message("GAME_INPROGRESS"));
			return;
		}

		try {
			Config.loadConfig();
			Maps.loadMaps();
			Localization.loadLocalization();
			Items.loadItems();
			Leaderboard.loadLeaderboard();
		} catch (Exception e) {
			sender.sendMessage(errorPrefix + message("CONFIG_ERROR"));
			return;
		}

		sender.sendMessage(messagePrefix + message("CONFIG_RELOAD"));
	}

	public String getLabel() {
		return "reload";
	}

	public String getUsage() {
		return "";
	}

	public String getDescription() {
		return "Reloads the config";
	}

	public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
		return null;
	}

}
