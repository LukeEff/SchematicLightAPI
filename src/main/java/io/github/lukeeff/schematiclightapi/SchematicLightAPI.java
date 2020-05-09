package io.github.lukeeff.schematiclightapi;

import io.github.lukeeff.schematiclightapi.temp.CommandDebug;
import io.github.lukeeff.schematiclightapi.temp.Debug;
import org.bukkit.plugin.java.JavaPlugin;

public class SchematicLightAPI extends JavaPlugin {

    @Override
    public void onEnable() {
        final boolean mkdirs = this.getDataFolder().mkdirs();
        getServer().getPluginCommand("loadschematic").setExecutor(new CommandDebug(this));
        getServer().getPluginManager().registerEvents(new Debug(this), this);
    }


    @Override
    public void onDisable() {

    }

}
