package se.playpark.dhs.command;

import se.playpark.dhs.Main;
import se.playpark.dhs.command.util.ICommand;
import se.playpark.dhs.game.util.Status;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static se.playpark.dhs.configuration.Config.abortPrefix;
import static se.playpark.dhs.configuration.Config.errorPrefix;
import static se.playpark.dhs.configuration.Localization.message;

public class Stop implements ICommand {

	public void execute(Player sender, String[] args) {
		if (Main.getInstance().getGame().checkCurrentMap()) {
			sender.sendMessage(errorPrefix + message("GAME_SETUP"));
			return;
		}
		if (Main.getInstance().getGame().getStatus() == Status.STARTING
				|| Main.getInstance().getGame().getStatus() == Status.PLAYING) {
			Main.getInstance().getGame().broadcastMessage(abortPrefix + message("STOP"));
			Main.getInstance().getGame().end();
		} else {
			sender.sendMessage(errorPrefix + message("GAME_NOT_INPROGRESS"));
		}
	}

	public String getLabel() {
		return "stop";
	}

	public String getUsage() {
		return "";
	}

	public String getDescription() {
		return "Stops the game";
	}

	public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
		return null;
	}

}
