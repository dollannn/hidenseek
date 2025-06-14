package se.playpark.dhs.command.map.unset;

import se.playpark.dhs.Main;
import se.playpark.dhs.command.util.ICommand;
import se.playpark.dhs.configuration.Config;
import se.playpark.dhs.configuration.Localization;
import se.playpark.dhs.configuration.Map;
import se.playpark.dhs.configuration.Maps;
import se.playpark.dhs.game.util.Status;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class Border implements ICommand {

    public void execute(Player sender, String[] args) {
        if (Main.getInstance().getGame().getStatus() != Status.STANDBY) {
            sender.sendMessage(Config.errorPrefix + Localization.message("GAME_INPROGRESS"));
            return;
        }
        Map map = Maps.getMap(args[0]);
        if (map == null) {
            sender.sendMessage(Config.errorPrefix + Localization.message("INVALID_MAP"));
            return;
        }
        if (map.getSpawn().isNotSetup()) {
            sender.sendMessage(Config.errorPrefix + Localization.message("ERROR_GAME_SPAWN"));
            return;
        }
        map.setWorldBorderData(0, 0, 0, 0, 0);
        Config.addToConfig("worldBorder.enabled", false);
        Config.saveConfig();
        sender.sendMessage(Config.messagePrefix + Localization.message("WORLDBORDER_DISABLE"));
        map.getWorldBorder().resetWorldBorder();
    }

    public String getLabel() {
        return "border";
    }

    public String getUsage() {
        return "<map>";
    }

    public String getDescription() {
        return "Removes a maps world border information";
    }

    public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
        if (parameter.equals("map")) {
            return Maps.getAllMaps().stream().map(Map::getName).collect(Collectors.toList());
        }
        return null;
    }

}
