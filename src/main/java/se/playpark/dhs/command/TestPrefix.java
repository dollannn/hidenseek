package se.playpark.dhs.command;

import org.bukkit.entity.Player;
import se.playpark.dhs.command.util.ICommand;
import se.playpark.dhs.game.util.PrefixManager;

import static se.playpark.dhs.configuration.Config.*;

public class TestPrefix implements ICommand {

    public void execute(Player sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(errorPrefix + "Usage: /hs testprefix <seeker|hider|remove>");
            return;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "seeker":
                PrefixManager.setSeekerPrefix(sender);
                sender.sendMessage(messagePrefix + "Set your prefix to SEEKER");
                break;
            case "hider":
                PrefixManager.setHiderPrefix(sender);
                sender.sendMessage(messagePrefix + "Set your prefix to HIDER");
                break;
            case "remove":
                PrefixManager.removeGamePrefix(sender);
                sender.sendMessage(messagePrefix + "Removed game prefix");
                break;
            case "status":
                sender.sendMessage(messagePrefix + "TAB enabled: " + PrefixManager.isTabEnabled());
                break;
            default:
                sender.sendMessage(errorPrefix + "Usage: /hs testprefix <seeker|hider|remove|status>");
                break;
        }
    }

    public String getLabel() {
        return "testprefix";
    }

    public String getDescription() {
        return "Test prefix functionality";
    }

    public String getUsage() {
        return "/hs testprefix <seeker|hider|remove|status>";
    }

    public java.util.List<String> autoComplete(String parameter, String typed) {
        if (parameter.equals("0")) {
            return java.util.Arrays.asList("seeker", "hider", "remove", "status");
        }
        return java.util.Collections.emptyList();
    }
}