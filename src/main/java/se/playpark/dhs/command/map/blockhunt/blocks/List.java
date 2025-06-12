package se.playpark.dhs.command.map.blockhunt.blocks;

import se.playpark.dhs.Main;
import se.playpark.dhs.command.util.ICommand;
import se.playpark.dhs.configuration.Config;
import se.playpark.dhs.configuration.Localization;
import se.playpark.dhs.configuration.Map;
import se.playpark.dhs.configuration.Maps;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public class List implements ICommand {

    public void execute(Player sender, String[] args) {
        if (!Main.getInstance().supports(9)) {
            sender.sendMessage(Config.errorPrefix + Localization.message("BLOCKHUNT_UNSUPPORTED"));
            return;
        }
        Map map = Maps.getMap(args[0]);
        if (map == null) {
            sender.sendMessage(Config.errorPrefix + Localization.message("INVALID_MAP"));
            return;
        }
        java.util.List<Material> blocks = map.getBlockHunt();
        if (blocks.isEmpty()) {
            sender.sendMessage(Config.errorPrefix + Localization.message("NO_BLOCKS"));
            return;
        }
        StringBuilder response = new StringBuilder(
                Config.messagePrefix + Localization.message("BLOCKHUNT_LIST_BLOCKS"));
        for (int i = 0; i < blocks.size(); i++) {
            response.append(String.format("\n%s. %s", i, blocks.get(i).toString()));
        }
        sender.sendMessage(response.toString());
    }

    public String getLabel() {
        return "list";
    }

    public String getUsage() {
        return "<map>";
    }

    public String getDescription() {
        return "List all blockhunt blocks in a map";
    }

    public java.util.List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
        if (parameter.equals("map")) {
            return Maps.getAllMaps().stream().map(Map::getName).collect(Collectors.toList());
        }
        return null;
    }
}
