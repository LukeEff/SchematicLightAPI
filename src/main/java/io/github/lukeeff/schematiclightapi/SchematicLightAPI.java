package io.github.lukeeff.schematiclightapi;

import io.github.lukeeff.schematiclightapi.temp.Debug;
import org.bukkit.plugin.java.JavaPlugin;

public class SchematicLightAPI extends JavaPlugin {

    @Override
    public void onEnable() {

        getServer().getPluginManager().registerEvents(new Debug(this), this);
    }


    @Override
    public void onDisable() {

    }

}
