package io.github.lukeeff.schematiclightapi.util;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.ChunkSection;
import net.minecraft.server.v1_8_R3.IBlockData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;

import javax.annotation.Nullable;
import java.util.Arrays;
import static org.bukkit.Bukkit.getConsoleSender;

public class Paste {

    //chunk.a(enumskyblock, blockposition, int) looks like a setlight method

    /**
     * Method of placing blocks that is roughly 150% the speed of
     * the APIs setBlock method.
     *
     * @param world the target world.
     * @param x location via x axis.
     * @param y location via y axis.
     * @param z location via z axis.
     * @param blockId the id of the block.
     * @param data the data of the block.
     * @param applyPhysics true to apply physics.
     */
    public static void setBlock(World world, int x, int y, int z, int blockId, byte data, boolean applyPhysics) {
        final net.minecraft.server.v1_8_R3.World nmsWorld = getWorldHandle(world);
        final BlockPosition blockPosition = getBlockPosition(x,y,z);
        final IBlockData blockData = getBlockData(blockId, data);
        nmsWorld.setTypeAndData(blockPosition, blockData, getApplyPhysicsId(applyPhysics));
    }

    /**
     * Method of placing blocks that is roughly 3650% the speed of
     * the APIs setBlock method.
     *
     * Client side rendering failure
     *
     * @param world the target world.
     * @param x location via x axis.
     * @param y location via y axis.
     * @param z location via z axis.
     * @param blockId the id of the block.
     * @param data the data of the block.
     */
    public static Chunk rapidSetBlock(World world, int x, int y, int z, int blockId, byte data)  {
        final Chunk chunk = getChunkAt(getWorldHandle(world), x, z);
        final BlockPosition blockPosition = getBlockPosition(x,y,z);
        final IBlockData blockData = getBlockData(blockId, data);
        chunk.a(blockPosition, blockData); //I think this is setType in later NMS versions without the flag.
        return chunk;
    }


    //TODO implement for 1.8 compatibility
    //Not finished.

    /**
     *
     * Client side rendering failure
     *
     * @param world
     * @param x
     * @param y
     * @param z
     * @param blockId
     * @param data
     * @return
     */
    public static Chunk unstableSetBlock(World world, int x, int y, int z, int blockId, byte data) {
        final Chunk chunk = getChunkAt(getWorldHandle(world), x, z);
        final IBlockData blockData = getBlockData(blockId, data);
        ChunkSection chunkSection = getChunkSection(chunk, y); //Sometimes returns null

        //if(chunkSection == getValidSection(chunk)) {
        //   chunkSection = new ChunkSection(y >> 4 << 4, true); //Flag is for sky light
        //    chunk.getSections()[y >> 4] = chunkSection;
        //
        // }
        if(chunkSection == null) {
            chunkSection = new ChunkSection(y >> 4 << 4, true);
            chunk.getSections()[y >> 4] = chunkSection;
        }

        //getConsoleSender().sendMessage(ChatColor.GREEN + chunkSection.toString()); //NPE if null

        chunkSection.setType(x & 15, y & 15, z & 15, blockData); //All non matching binary become 0.
        return chunk;
    }

    public static void prototypeSetBlock(World world, int x, int y, int z, int blockId, byte data) {
        final Chunk chunk = getChunkAt(getWorldHandle(world), x, z);
        final IBlockData blockData = getBlockData(blockId, data);
        ChunkSection section = getChunkSection(chunk, y);



    }


    /**
     * Not certain what this does exactly. Just a theory mainly.
     * Loops through chunk sections of a given chunk and finds the
     * first section that is not null and also valid for placing blocks.
     * TODO figure out how this really works in the NMS code
     * @param chunk the chunk.
     * @return the valid section if found otherwise null.
     */
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
     * Gets the chunk at a block's location.
     *
     * @param nmsWorld the handle of the target Bukkit world.
     * @param blockX the x location of the block (NOT chunk coordinates).
     * @param blockZ the z location of the block (NOT chunk coordinates).
     * @return the nms chunk.
     */
    private static Chunk getChunkAt(net.minecraft.server.v1_8_R3.World nmsWorld, int blockX, int blockZ) {
        return nmsWorld.getChunkAt(blockX >> 4, blockZ >> 4);
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
    private static IBlockData getBlockData(int blockId, byte data) {
        return net.minecraft.server.v1_8_R3.Block.getByCombinedId(blockId + (data << 12));
    }

    /**
     * Short hand way to get the handle of the Bukkit world.
     *
     * @param world the target Bukkit world.
     * @return an NMS world.
     */
    private static net.minecraft.server.v1_8_R3.World getWorldHandle(World world) {
        return ((CraftWorld) world).getHandle();
    }

    /**
     * Gets a BlockPosition object at the specific coordinates.
     *
     * @param x the x position.
     * @param y the y position.
     * @param z the z position.
     * @return a new BlockPosition object reference with the target coords.
     */
    private static BlockPosition getBlockPosition(int x, int y, int z) {
        return new BlockPosition(x,y,z);
    }

}
