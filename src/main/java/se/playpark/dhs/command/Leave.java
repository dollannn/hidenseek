package se.playpark.dhs.command;

import se.playpark.dhs.Main;
import se.playpark.dhs.command.util.ICommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static se.playpark.dhs.configuration.Config.errorPrefix;
import static se.playpark.dhs.configuration.Localization.message;

public class Leave implements ICommand {

	public void execute(Player sender, String[] args) {
		if (Main.getInstance().getGame().checkCurrentMap()) {
			sender.sendMessage(errorPrefix + message("GAME_SETUP"));
			return;
		}
		Player player = Bukkit.getServer().getPlayer(sender.getName());
		if (player == null) {
			sender.sendMessage(errorPrefix + message("COMMAND_ERROR"));
			return;
		}
		if (!Main.getInstance().getBoard().contains(player)) {
			sender.sendMessage(errorPrefix + message("GAME_NOT_INGAME"));
			return;
		}
		Main.getInstance().getGame().leave(player);
	}

	public String getLabel() {
		return "leave";
	}

	public String getUsage() {
		return "";
	}

	public String getDescription() {
		return "Leaves the lobby if game is set to manual join/leave";
	}

	public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
		return null;
	}

}