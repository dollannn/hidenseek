package se.playpark.dhs.command.map.blockhunt.blocks;

import se.playpark.dhs.Main;
import se.playpark.dhs.command.util.ICommand;
import se.playpark.dhs.configuration.Config;
import se.playpark.dhs.configuration.Localization;
import se.playpark.dhs.configuration.Map;
import se.playpark.dhs.configuration.Maps;
import se.playpark.dhs.game.util.Status;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Remove implements ICommand {

    public void execute(Player sender, String[] args) {
        if (!Main.getInstance().supports(9)) {
            sender.sendMessage(Config.errorPrefix + Localization.message("BLOCKHUNT_UNSUPPORTED"));
            return;
        }
        if (Main.getInstance().getGame().getStatus() != Status.STANDBY) {
            sender.sendMessage(Config.errorPrefix + Localization.message("GAME_INPROGRESS"));
            return;
        }
        Map map = Maps.getMap(args[0]);
        if (map == null) {
            sender.sendMessage(Config.errorPrefix + Localization.message("INVALID_MAP"));
            return;
        }
        Material block;
        try {
            block = Material.valueOf(args[1]);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Config.errorPrefix + Localization.message("COMMAND_INVALID_ARG").addAmount(args[1]));
            return;
        }
        java.util.List<Material> blocks = map.getBlockHunt();
        if (!blocks.contains(block)) {
            sender.sendMessage(
                    Config.errorPrefix + Localization.message("BLOCKHUNT_BLOCK_DOESNT_EXIT").addAmount(args[1]));
        }
        blocks.remove(block);
        map.setBlockhunt(map.isBlockHuntEnabled(), blocks);
        Maps.setMap(map.getName(), map);
        sender.sendMessage(Config.messagePrefix + Localization.message("BLOCKHUNT_BLOCK_REMOVED").addAmount(args[1]));
    }

    public String getLabel() {
        return "remove";
    }

    public String getUsage() {
        return "<map> <block>";
    }

    public String getDescription() {
        return "Remove a blockhunt block from a map!";
    }

    public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
        if (parameter.equals("map")) {
            return Maps.getAllMaps().stream().map(Map::getName).collect(Collectors.toList());
        } else if (parameter.equals("block")) {
            return Arrays.stream(Material.values())
                    .filter(Material::isBlock)
                    .map(Material::toString)
                    .filter(s -> s.toUpperCase().startsWith(typed.toUpperCase()))
                    .collect(Collectors.toList());
        }
        return null;
    }

}
