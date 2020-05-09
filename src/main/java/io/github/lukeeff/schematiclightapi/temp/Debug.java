package io.github.lukeeff.schematiclightapi.temp;

import io.github.lukeeff.schematiclightapi.SchematicLightAPI;
import io.github.lukeeff.schematiclightapi.schematic.Clipboard;
import io.github.lukeeff.schematiclightapi.util.ClipboardTools;
import io.github.lukeeff.schematiclightapi.util.Paste;
import io.github.lukeeff.schematiclightapi.util.SchematicClipboard;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunk;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunkBulk;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.bukkit.Bukkit.broadcastMessage;
import static org.bukkit.Bukkit.getConsoleSender;
import static java.lang.System.currentTimeMillis;

/**
 * Purely for debugging. Entire class to be thrown away
 * on completion.
 */
public class Debug extends AbstractBreak implements Listener {

    @Getter @Setter private static String schematicFileName = "test.schematic";

    Map<Material, Runnable> axePower = new HashMap<>();
    @Getter @Setter Location loc;
    @Getter SchematicLightAPI plugin;
    @Getter @Setter private static Set<Chunk> chunks = new HashSet<>();

    public Debug(SchematicLightAPI plugin) {
        this.plugin = plugin;
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
        } else if(player.getItemInHand().getType().equals(Material.DIAMOND_PICKAXE)) {
            long time = currentTimeMillis();
            setLoc(e.getBlock().getLocation());
            int count = attemptSchematicPaste(e);
            broadcastMessage(ChatColor.GREEN + "Completed. Time taken: " + (currentTimeMillis() - time) + " (ms) for " + count + " blocks!");
        }
    }

    private int attemptSchematicPaste(BlockBreakEvent e) {
        int blockCount = 0;
        final Location loc = e.getBlock().getLocation();
        File schematic = new File(getPlugin().getDataFolder(), getSchematicFileName());

        try {
            SchematicClipboard clipboard = new SchematicClipboard(schematic);
            final byte[] blockId = clipboard.getBlockIds();
            final byte[] data = clipboard.getBlockData();
            final int length = clipboard.getLength();
            final int width = clipboard.getWidth();
            final int height = clipboard.getHeight();
            blockCount = length * width * height;

            ClipboardTools.getBukkitChunksRelative(clipboard, loc).forEach(affectedChunk -> getChunks().add(affectedChunk));
            ClipboardTools.binaryPaste(loc, blockId, data, length, width, height);//.forEach(c -> loc.getWorld().refreshChunk(c.locX, c.locZ)); //So apparently this works perfectly...
            getChunks().clear();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return blockCount;
    }

    @Override
    Location getLocation() {
        return getLoc();
    }

    @Override
    void setBlock(World world, int x, int y, int z, IBlockData blockData, Material block) {
        net.minecraft.server.v1_8_R3.World nmsWorld = Paste.getWorldHandle(world);
        switch(block) {
            case DIAMOND_BLOCK:
                Paste.unstableSetBlock(nmsWorld, blockData, x, y, z);
                break;
            case GOLD_BLOCK:
                Paste.rapidSetBlock(nmsWorld, blockData, x, y, z);
                break;
            case IRON_BLOCK:
                Paste.setBlock(nmsWorld, blockData, x, y, z, false);
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
