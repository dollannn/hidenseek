package se.playpark.dhs.command;

import se.playpark.dhs.command.location.LocationUtils;
import se.playpark.dhs.command.location.Locations;
import se.playpark.dhs.command.util.ICommand;
import se.playpark.dhs.configuration.Config;
import se.playpark.dhs.util.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SetExitLocation implements ICommand {

	public void execute(Player sender, String[] args) {
		LocationUtils.setLocation(sender, Locations.EXIT, null, map -> {
			Config.addToConfig("exit.x", sender.getLocation().getBlockX());
			Config.addToConfig("exit.y", sender.getLocation().getBlockY());
			Config.addToConfig("exit.z", sender.getLocation().getBlockZ());
			Config.addToConfig("exit.world", sender.getLocation().getWorld().getName());
			Config.exitPosition = Location.from(sender);
			Config.saveConfig();
		});
	}

	public String getLabel() {
		return "setexit";
	}

	public String getUsage() {
		return "";
	}

	public String getDescription() {
		return "Sets the plugins exit location";
	}

	public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
		return null;
	}

}
