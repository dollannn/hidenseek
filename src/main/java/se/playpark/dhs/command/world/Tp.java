package se.playpark.dhs.command.world;

import se.playpark.dhs.Main;
import se.playpark.dhs.command.util.ICommand;
import se.playpark.dhs.configuration.Config;
import se.playpark.dhs.configuration.Localization;
import se.playpark.dhs.util.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Tp implements ICommand {
    public void execute(Player sender, String[] args) {
        Location test = new Location(args[0], 0, 0, 0);
        if (!test.exists()) {
            sender.sendMessage(Config.errorPrefix + Localization.message("WORLD_DOESNT_EXIT"));
            return;
        }
        World world = test.load();
        if (world == null) {
            sender.sendMessage(Config.errorPrefix + Localization.message("WORLD_LOAD_FAILED"));
            return;
        }
        Location loc = new Location(world.getName(), world.getSpawnLocation());
        loc.teleport(sender);
    }

    public String getLabel() {
        return "tp";
    }

    public String getUsage() {
        return "<world>";
    }

    public String getDescription() {
        return "Teleport to another world";
    }

    public List<String> autoComplete(@NotNull String parameter, @NotNull String typed) {
        if (parameter.equals("world")) {
            return Main.getInstance().getWorlds();
        }
        return null;
    }
}
