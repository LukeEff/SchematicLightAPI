package io.github.lukeeff.schematiclightapi.temp;

import io.github.lukeeff.schematiclightapi.SchematicLightAPI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

public class CommandDebug implements CommandExecutor {

    SchematicLightAPI plugin;

    public CommandDebug(SchematicLightAPI plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player p = (Player) commandSender;
        if(strings.length > 0 && new File(plugin.getDataFolder(), strings[0] + ".schematic").exists()) {
            Debug.setSchematicFileName(strings[0] + ".schematic");
            p.sendMessage(ChatColor.AQUA + "Success. Changed schematic to: " + strings[0] + ".schematic");
        } else {
            p.sendMessage(ChatColor.DARK_RED + "Failure. No schematic by the name of " + strings[0] + ".schematic in the data folder!");
        }


        return true;
    }
}
