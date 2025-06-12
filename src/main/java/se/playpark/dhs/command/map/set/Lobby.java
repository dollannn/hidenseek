package se.playpark.dhs.command.map.set;

import se.playpark.dhs.command.location.LocationUtils;
import se.playpark.dhs.command.location.Locations;
import se.playpark.dhs.command.util.ICommand;
import se.playpark.dhs.configuration.Map;
import se.playpark.dhs.configuration.Maps;
import se.playpark.dhs.util.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class Lobby implements ICommand {

	public void execute(Player sender, String[] args) {
		LocationUtils.setLocation(sender, Locations.LOBBY, args[0], map -> {
			map.setLobby(Location.from(sender));
		});
	}

	public String getLabel() {
		return "lobby";
	}

	public String getUsage() {
		return "<map>";
	}

	public String getDescription() {
		return "Sets the maps lobby location";
	}

	public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
		if (parameter.equals("map")) {
			return Maps.getAllMaps().stream().map(Map::getName).collect(Collectors.toList());
		}
		return null;
	}

}
