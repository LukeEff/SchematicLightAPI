package io.github.lukeeff.schematiclightapi.temp;

import io.github.lukeeff.schematiclightapi.util.Paste;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunk;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import static java.lang.System.currentTimeMillis;

public abstract class AbstractBreak implements Listener {

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if(e.getPlayer() != null) {
            playerBreak(e, e.getPlayer());
        }
    }

    abstract void playerBreak(BlockBreakEvent e, Player player);


    void setBlock(int num, Material blockType) {
        Location loc = getLocation();
        long time = currentTimeMillis();
        @SuppressWarnings("deprecation") final int newId = blockType.getId();
        final byte data = 0;
        final int locX = loc.getBlockX();
        final int locY = loc.getBlockY();
        final int locZ = loc.getBlockZ();
        World world = loc.getWorld();
        for(int z = 0; z < num; z++) {
            for(int x = 0; x < num; x++) {
                for (int y = 0; y < num ; y++) {

                    setBlock(world, locX + x, locY + y, locZ + z, newId, data, false, blockType);
                }
            }
        }

        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Time elapsed: " + (currentTimeMillis() - time));
    }

    abstract Location getLocation();

    abstract void setBlock(World world, int x, int y, int z, int newId, byte data, boolean applyPhysics, Material block);

}
