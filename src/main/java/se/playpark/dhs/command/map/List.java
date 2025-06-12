package se.playpark.dhs.command.map;

import se.playpark.dhs.command.util.ICommand;
import se.playpark.dhs.configuration.Config;
import se.playpark.dhs.configuration.Localization;
import se.playpark.dhs.configuration.Map;
import se.playpark.dhs.configuration.Maps;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class List implements ICommand {

    public void execute(Player sender, String[] args) {
        Collection<Map> maps = Maps.getAllMaps();
        if (maps.size() < 1) {
            sender.sendMessage(Config.errorPrefix + Localization.message("NO_MAPS"));
            return;
        }
        StringBuilder response = new StringBuilder(Config.messagePrefix + Localization.message("LIST_MAPS"));
        for (Map map : maps) {
            response.append("\n    ").append(map.getName()).append(": ")
                    .append(map.isNotSetup() ? ChatColor.RED + "NOT SETUP" : ChatColor.GREEN + "SETUP")
                    .append(ChatColor.WHITE);
        }
        sender.sendMessage(response.toString());
    }

    public String getLabel() {
        return "list";
    }

    public String getUsage() {
        return "";
    }

    public String getDescription() {
        return "List all maps in the plugin";
    }

    public java.util.List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
        return null;
    }

}