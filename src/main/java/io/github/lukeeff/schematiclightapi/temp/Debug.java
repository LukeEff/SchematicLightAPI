package io.github.lukeeff.schematiclightapi.temp;

import io.github.lukeeff.schematiclightapi.SchematicLightAPI;
import io.github.lukeeff.schematiclightapi.util.Paste;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunk;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.System.currentTimeMillis;

public class Debug extends AbstractBreak implements Listener {

    Map<Material, Runnable> axePower = new HashMap<>();
    @Getter @Setter Location loc;
    private static Set<net.minecraft.server.v1_8_R3.Chunk> chunks = new HashSet<>();

    public Debug(SchematicLightAPI plugin) {
        setAxePower();
    }

    private void setAxePower() {
        axePower.put(Material.STONE_AXE, () -> setBlock(100, Material.STONE)); //1Mil cuboid 2945
        axePower.put(Material.IRON_AXE, () -> setBlock(100, Material.IRON_BLOCK)); //1Mil cuboid 876
        axePower.put(Material.GOLD_AXE, () -> setBlock(100, Material.GOLD_BLOCK)); //1Mil cuboid 360
        axePower.put(Material.DIAMOND_AXE, () -> setBlock(100, Material.DIAMOND_BLOCK)); //1Mil cuboid 15
    }

    @Override
    void playerBreak(BlockBreakEvent e, Player player) {
        if(hasAxe(player)) {
            setLoc(e.getBlock().getLocation());
            axePower.get(player.getItemInHand().getType()).run();
            refreshChunk(player);
        }
    }

    private void refreshChunk(Player player) {
        World world = player.getWorld();
        //e.getBlock().getWorld().getNearbyEntities() For all players in range
        chunks.forEach(chunk -> {
            send(player, chunk);
            world.refreshChunk(chunk.locX, chunk.locZ);
        });
    }

    private void send(final Player player, net.minecraft.server.v1_8_R3.Chunk chunk) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutMapChunk(chunk, true, 20));
    }



    @Override
    Location getLocation() {
        return getLoc();
    }

    @Override
    void setBlock(World world, int x, int y, int z, int newId, byte data, boolean applyPhysics, Material block) {
        switch(block) {
            case DIAMOND_BLOCK:
                chunks.add(Paste.unstableSetBlock(world, x, y, z, newId, data));
                break;
            case GOLD_BLOCK:
                chunks.add(Paste.rapidSetBlock(world, x, y, z, newId, data));
                break;
            case IRON_BLOCK:
                Paste.setBlock(world, x, y, z, newId, data, applyPhysics);
                break;
            case STONE:
                world.getBlockAt(x,y,z).setType(block);
                break;
        }

    }

    private boolean hasAxe(Player player) {
        return axePower.containsKey(player.getItemInHand().getType());
    }



}
