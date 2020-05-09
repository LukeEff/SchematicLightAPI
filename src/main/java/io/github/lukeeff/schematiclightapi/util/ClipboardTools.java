package io.github.lukeeff.schematiclightapi.util;


import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.IBlockData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashSet;
import java.util.Set;

public class ClipboardTools {

    public ClipboardTools() {

    }

    public static void pasteClipboard(Location location, SchematicClipboard clipboard) {

    }

    public static Set<Chunk> binaryPaste(final Location loc, final byte[] blockId, final byte[] data, final int length, final int width, final int height) {
        final Set<Chunk> chunkSet = new HashSet<>(); //Temporary
        IBlockData blockData;
        if(blockId.length == 0) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "Error... empty block id array");
            return chunkSet;
        }
        final World world = loc.getWorld();
        final int locX = loc.getBlockX();
        final int locY = loc.getBlockY();
        final int locZ = loc.getBlockZ();
        for(int x = 0; x < width; x++) {
            for(int z = 0; z < length; z++) {
                for(int y = 0; y < height; y++) {
                    final int worldLocX = locX + x;
                    final int worldLocY = locY + y;
                    final int worldLocZ = locZ + z;
                    final int index = getBinaryIndex(x,y,z, length, width);
                    try {
                        blockData = Paste.getBlockData(blockId[index], data[index]);
                    } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException no) {
                        continue;
                    }

                    chunkSet.add(Paste.unstableSetBlock(world, blockData, worldLocX, worldLocY, worldLocZ));
                }
            }
        }
        return chunkSet;
    }


    /**
     * Gets the index of the byte for a block inside a schematic.
     * The math seen below is simply how to decode the index.
     * parameters are all relative to the schematic and have nothing
     * to do with the world.
     *
     * @param x the x coordinate in the schematic.
     * @param y the y coordinate in the schematic.
     * @param z the z coordinate in the schematic.
     * @param length the length of the schematic.
     * @param width the width of the schematic.
     * @return the index of the binary id array for the target block.
     */
    private static int getBinaryIndex(final int x, final int y, final int z, final int length, final int width) {
        return (y * length + z) * width + x;
    }

    public static Set<Chunk> getChunksRelative(SchematicClipboard clipboard, Location pasteLocation) {
        final Set<Chunk> chunkSet = new HashSet<>();
        final net.minecraft.server.v1_8_R3.World world = Paste.getWorldHandle(pasteLocation.getWorld());
        final int originChunkZ = Paste.toChunkCoordinate(clipboard.getLength());
        final int originChunkX = Paste.toChunkCoordinate(clipboard.getWidth());
        final int highChunkZ = Paste.toChunkCoordinate(pasteLocation.getBlockZ()) + originChunkZ;
        final int highChunkX = Paste.toChunkCoordinate(pasteLocation.getBlockX()) + originChunkX;

        for(int x = originChunkX; x < highChunkX; x++) {
            for(int z = originChunkZ; z < highChunkZ; z++) {
                chunkSet.add(world.getChunkAt(x, z));
            }
        }
        return chunkSet;
    }




}
