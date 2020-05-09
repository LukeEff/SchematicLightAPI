package io.github.lukeeff.schematiclightapi.util;

import net.minecraft.server.v1_8_R3.World;
import io.github.lukeeff.schematiclightapi.schematic.Clipboard;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.IBlockData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClipboardTools {

    public ClipboardTools() {

    }

    public static void pasteClipboard(Location location, SchematicClipboard clipboard) {

    }

    public static void binaryPaste(final Location loc, final byte[] blockId, final byte[] data, final int length, final int width, final int height) {

        IBlockData blockData;
        if(blockId.length == 0) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "Error... empty block id array");
            return;
        }
        final World world = Paste.getWorldHandle(loc.getWorld());
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
                    Paste.unstableSetBlock(world, blockData, worldLocX, worldLocY, worldLocZ);
                }
            }
        }
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

    /**
     * Gets all NMS chunks within a region.
     * We do not have to currently worry about directions, so it is safe to assume that
     * a schematic will never have anything in the opposite direction that is expected
     * (aka -100 relative to schematic).
     *
     * @param clipboard the clipboard to be pasted.
     * @param pasteLocation the location of the paste.
     * @return a Set containing the chunks within the bounds.
     */
    public static List<Chunk> getNMSChunksRelative(SchematicClipboard clipboard, Location pasteLocation) {
        final List<Chunk> chunkSet = new ArrayList<>();
        final net.minecraft.server.v1_8_R3.World world = Paste.getWorldHandle(pasteLocation.getWorld());
        getChunkCoordsRelative(clipboard, pasteLocation).forEach(xz -> chunkSet.add(world.getChunkAt(xz[0], xz[1])));
        return chunkSet;
    }

    /**
     * Gets all bukkit chunks within a region.
     * We do not have to currently worry about directions, so it is safe to assume that
     * a schematic will never have anything in the opposite direction that is expected
     * (aka -100 relative to schematic).
     *
     * //TODO just make a casting method.
     * @param clipboard the clipboard to be pasted.
     * @param pasteLocation the location of the paste.
     * @return a Set containing the chunks within the bounds.
     */
    public static Set<org.bukkit.Chunk> getBukkitChunksRelative(SchematicClipboard clipboard, Location pasteLocation) {
        final Set<org.bukkit.Chunk> chunkSet = new HashSet<>();
        final org.bukkit.World world = pasteLocation.getWorld();
        getChunkCoordsRelative(clipboard, pasteLocation).forEach(xz -> chunkSet.add(world.getChunkAt(xz[0], xz[1])));
        return chunkSet;
    }

    /**
     * Gets all chunks in respect to their coordinates within a region. Stored in an int array where the first
     * index is the x coordinate and the second index is the y coordinate.
     *
     * @param clipboard the clipboard to be pasted.
     * @param pasteLocation the location of the paste.
     * @return a Set of int arrays containing the x and z coordinates respectively to the chunk.
     */
    public static Set<int[]> getChunkCoordsRelative(SchematicClipboard clipboard, Location pasteLocation) {
        final Set<int[]> chunkCoordsSet = new HashSet<>();
        final net.minecraft.server.v1_8_R3.World world = Paste.getWorldHandle(pasteLocation.getWorld());
        final int originChunkZ = Paste.toChunkCoordinate(pasteLocation.getBlockZ());
        final int originChunkX = Paste.toChunkCoordinate(pasteLocation.getBlockX());
        final int highChunkZ = Paste.toChunkCoordinate(clipboard.getLength()) + originChunkZ;
        final int highChunkX = Paste.toChunkCoordinate(clipboard.getWidth()) + originChunkX;

        for(int x = originChunkX; x < highChunkX; x++) {
            for(int z = originChunkZ; z < highChunkZ; z++) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Chunk X: " +  x + "Chunk Z: " + z);
                chunkCoordsSet.add(new int[] {x,z});
            }
        }
        return chunkCoordsSet;
    }

}
