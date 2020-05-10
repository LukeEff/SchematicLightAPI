package io.github.lukeeff.schematiclightapi.util;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Set;

import static org.bukkit.Bukkit.getConsoleSender;

/**
 * Utility class designed for block placing a various speeds
 * depending on the stability that is needed.
 *
 * * Where I got these ideas:
 * @See https://www.spigotmc.org/threads/methods-for-changing-massive-amount-of-blocks-up-to-14m-blocks-s.395868/
 */
public class Paste {
    public static int test = 10;

    //chunk.a(enumskyblock, blockposition, int) looks like a setlight method

    /**
     * Method of placing blocks that is roughly 150% the speed of
     * the APIs setBlock method.
     *
     * @param world the target world.
     * @param x location via x axis.
     * @param y location via y axis.
     * @param z location via z axis.
     * @param blockData the block data to be set.
     * @param applyPhysics true to apply physics.
     */
    public static void setBlock(net.minecraft.server.v1_8_R3.World world, IBlockData blockData, int x, int y, int z, boolean applyPhysics) {
        final BlockPosition blockPosition = getBlockPosition(x,y,z);
        world.setTypeAndData(blockPosition, blockData, getApplyPhysicsId(applyPhysics));
    }

    /**
     * Method of placing blocks that is roughly 3650% the speed of
     * the APIs setBlock method.
     * Packet needs to be sent after and chunk needs to be reloaded to show
     * the client the changes.
     *
     * @param world the target world.
     * @param x location via x axis.
     * @param y location via y axis.
     * @param z location via z axis.
     * @param blockData the block data to be set.
     */
    public static void rapidSetBlock(net.minecraft.server.v1_8_R3.World world, IBlockData blockData, int x, int y, int z)  {
        final Chunk chunk = getChunkAt(world, x, z);
        final BlockPosition blockPosition = getBlockPosition(x,y,z);
        chunk.a(blockPosition, blockData);
        updateChange(world, blockPosition);
    }

    public static Chunk currentChunk;
    public static Chunk oldChunk;
    private static char[] testing = getArray();


    private static char[] getArray() {
        char[] test = new char[4096];
        Arrays.fill(test, '9');
        return test;
    }
    /**
     * Fastest but more unstable way to set blocks. Very fast.
     * block setting speed (Roughly 6-10 million a second)
     * Packet needs to be sent after and chunk needs to be reloaded to show
     * the client the changes.
     *
     * @param bukkitworld the target world.
     * @param x the x coordinate.
     * @param y the y coordinate.
     * @param z the z coordinate.
     * @param blockData the block data to be set.
     */
    public static void unstableSetBlock(World bukkitworld, IBlockData blockData, int x, int y, int z) {
        net.minecraft.server.v1_8_R3.World world = getWorldHandle(bukkitworld);
            final Chunk chunk = getChunkAt(world, x, z);

            final BlockPosition blockPosition = getBlockPosition(x, y, z);
            ChunkSection chunkSection = getChunkSection(chunk, y); //Sometimes returns null
            //chunkSection.b(x & 15, y & 15, z & 15, getApplyPhysicsId(true));
            if (chunkSection == null) {
                chunkSection = new ChunkSection(y >> 4 << 4, !chunk.world.worldProvider.o()); //Sky box light related. Creates an array when true.
                chunk.getSections()[y >> 4] = chunkSection;
            }
            //world.methodProfiler.a("getBrightness");
            //int i = enumskyblock == EnumSkyBlock.SKY ? 0 : block.r();
            chunkSection.setType(x & 15, y & 15, z & 15, blockData); //All non matching binary become 0.
            //chunkSection.b(x & 15, y & 15, z & 15, PRIMINT); //This could be it, but I couldn't figure out what should be in the int field.
            //chunk.a(EnumSkyBlock.BLOCK, blockPosition, getApplyPhysicsId(true)); //I think this partially fixes lighting...
            chunkSection.recalcBlockCounts();
            updateChange(world, blockPosition);
            //chunk.initLighting();
            //updateChange(world, blockPosition);
            //world.update(blockPosition, blockData.getBlock());
            //world.setTypeUpdate(blockPosition, blockData);
            //chunk.initLighting();
            //updateChange(world, blockPosition);

            if (oldChunk == null) {
                oldChunk = chunk;
                currentChunk = chunk;
            } else if (!(currentChunk.locX == chunk.locX && currentChunk.locZ == chunk.locZ)) {
                currentChunk = chunk;
                oldChunk.initLighting();
                oldChunk = chunk;
                chunk.initLighting();
                updateChange(world, blockPosition);
            }
    }

    /*
    NEW PLAN:
    Get data from schematic in chunks and pass into method
    get new instance of chunk that is overriding data and
    set world chunk equal to it then save.
     */

    //public static void setData()

    /**
     * Notifies a block update so that player can see.
     *
     * @param world the world.
     * @param blockPosition the block.
     */
    private static void updateChange(net.minecraft.server.v1_8_R3.World world, BlockPosition blockPosition) {
        //world.c(EnumSkyBlock.BLOCK, blockPosition); //Fixes light but laggy.
        world.notify(blockPosition);
    }

    /**
     * Significant overhead. Will update lighting throughout chunk. Should not be looped with block placement
     * for the sake of efficiency.
     *
     * @param chunk the chunk to have lighting updated.
     */
    private static void updateLighting(net.minecraft.server.v1_8_R3.Chunk chunk) {
        chunk.initLighting();
    }

    /**
     * Not certain what this does exactly. Just a theory mainly.
     * Loops through chunk sections of a given chunk and finds the
     * first section that is not null and also valid for placing blocks.
     * TODO figure out how this really works in the NMS code
     * @param chunk the chunk.
     * @return the valid section if found otherwise null.
     */
    @Deprecated //Not necessary.
    private static ChunkSection getValidSection(Chunk chunk) {
        ChunkSection[] chunkSections = chunk.getSections();
        final int index = chunkSections.length -1;

        for(int i = index; i >= 0; i--) {
            ChunkSection section = chunkSections[i];
            if(isValidSection(section)) { //Not a null section and has no empty blocks.
                return section;
            }
        }
        return null; //return null if a valid section was not found.
    }

    /**
     * Checks if a section in a chunk at a real y position is empty (containing no useful data)
     *
     * @param section the target chunk section
     * @param yPos the y pos
     * @return true if it is not an empty section.
     */
    private static boolean isIncludedSection(ChunkSection section, int yPos) {
        if(section != null && ((section.getYPosition()) & (1 << yPos)) != 0) {
            return true;
        }
        return false;
    }
    /**
     * Not certain this is correct. Just a theory mainly.
     * Checks if null or if nonEmptyBlockCount is 0.
     * TODO figure out how this method really works
     *
     * @param chunkSection the chunk section
     * @return true if can not place
     */
    private static boolean isValidSection(@Nullable ChunkSection chunkSection) {
        return !(chunkSection == null || hasEmptyBlockCount(chunkSection)); //nonEmptyBlockCount == 0
    }

    /**
     * Checks if a chunk section has an empty block count.
     * This method exists because this is obfuscated in NMS.
     *
     * @param chunkSection the target chunk section.
     * @return true if the nonEmptyBlockCount field is equal to 0.
     */
    private static boolean hasEmptyBlockCount(ChunkSection chunkSection) {
        return chunkSection.a();
    }

    /**
     * Gets chunk section.
     *
     * @param chunk the target chunk.
     * @param y y position of block. (I think this is the height?)
     * @return the chunk section.
     */
    private static ChunkSection getChunkSection(Chunk chunk, int y) {
        return chunk.getSections()[y >> 4];
    }

    //TODO my own set block
    private static void setChunkType(Chunk chunk, BlockPosition blockPosition, IBlockData data, int applyPhysics) {
        int x = blockPosition.getX() & 15;
        int y = blockPosition.getY();
        int z = blockPosition.getZ() & 15;
        ChunkSection chunkSection = new ChunkSection(y >> 4 << 4, false); //doLight

    }

    /**
     * Loads chunks that are not already loaded in a given set.
     *
     * @param chunks the chunks to be loaded.
     */
    public static void loadChunks(Set<org.bukkit.Chunk> chunks) {
        chunks.stream().filter(c -> !c.isLoaded()).forEach(org.bukkit.Chunk::load);
    }

    /**
     * Unloads chunks that are not already unloaded in a given set.
     *
     * @param chunks the chunks to be unloaded.
     */
    public static void unloadChunks(Set<org.bukkit.Chunk> chunks) {
        chunks.stream().filter(org.bukkit.Chunk::isLoaded).forEach(c -> c.unload(true));
    }

    /**
     * Reloads all chunks within a given set.
     *
     * @param chunks the chunks to be reloaded.
     */
    public static void reloadChunks(Set<org.bukkit.Chunk> chunks) {
        unloadChunks(chunks);
        loadChunks(chunks);
    }


    /**
     * Gets the chunk at a block's location.
     *
     * @param nmsWorld the handle of the target Bukkit world.
     * @param blockX the x location of the block (NOT chunk coordinates).
     * @param blockZ the z location of the block (NOT chunk coordinates).
     * @return the nms chunk.
     */
    public static Chunk getChunkAt(net.minecraft.server.v1_8_R3.World nmsWorld, int blockX, int blockZ) {
        return nmsWorld.getChunkAt(toChunkCoordinate(blockX), toChunkCoordinate(blockZ));
    }

    /**
     * Returns the chunk coordinate value of a block coordinate.
     *
     * @param blockCoordinate the block coordinate.
     * @return the chunk coordinate in respect to that block.
     */
    public static int toChunkCoordinate(final int blockCoordinate) {
        return blockCoordinate >> 4;
    }

    /**
     * Returns the block coordinate value that matches a chunk coordinate value.
     *
     * @param chunkCoordinate the chunk coordinate.
     * @return the respective block coordinate.
     */
    public static int toBlockCoordinate(final int chunkCoordinate) {
        return chunkCoordinate << 4;
    }

    /**
     * Gets the int required for knowing whether to apply physics or not.
     * @param applyPhysics true to apply physics.
     * @return an int that will enable or disable block physics when placed.
     */
    private static int getApplyPhysicsId(boolean applyPhysics) {
        return applyPhysics ? 3 : 2;
    }

    /**
     * Short hand way to get the block data.
     *
     * @param blockId the block id.
     * @param data the block data.
     * @return an IBlockData object relative to the passed parameters.
     */
    public static IBlockData getBlockData(int blockId, byte data) {
        return net.minecraft.server.v1_8_R3.Block.getByCombinedId(blockId + (data << 12));
    }

    /**
     * Short hand way to get the handle of the Bukkit world.
     *
     * @param world the target Bukkit world.
     * @return an NMS world.
     */
    public static net.minecraft.server.v1_8_R3.World getWorldHandle(World world) {
        return ((CraftWorld) world).getHandle();
    }

    /**
     * Gets a BlockPosition object at the specific block coordinates.
     *
     * @param x the x position.
     * @param y the y position.
     * @param z the z position.
     * @return a new BlockPosition object reference with the target coords.
     */
    public static BlockPosition getBlockPosition(int x, int y, int z) {
        return new BlockPosition(x,y,z);
    }

    /**
     * Gets the lower chunk coordinate based on two different block coordinates in the same plane.
     *
     * @param blockXorZ one block x or z coordinate.
     * @param otherBlockXorZ another block x or z coordinate.
     * @return the lower chunk coordinate value.
     */
    public static int getLowerAsChunkCoord(int blockXorZ, int otherBlockXorZ) {
        return Math.min(toChunkCoordinate(blockXorZ), toChunkCoordinate(otherBlockXorZ));
    }

    /**
     * Gets the higher chunk coordinate based on two different block coordinates in the same plane.
     *
     * @param blockXorZ one block x or z coordinate.
     * @param otherBlockXorZ another block x or z coordinate.
     * @return the higher chunk coordinate value.
     */
    public static int getHigherAsChunkCoord(int blockXorZ, int otherBlockXorZ) {
        return Math.max(toChunkCoordinate(blockXorZ), toChunkCoordinate(otherBlockXorZ));
    }

}
