package com.shiver.supremecrafting.furnace;

import com.shiver.supremecrafting.block.BlockSupremeFurnaceCasing;
import com.shiver.supremecrafting.registry.SCRegistry;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public final class CasingCascade {
    private static final int LIMIT = FurnaceFormation.shellCount(128) + 1024;

    private CasingCascade() {
    }

    public static void destroyConnected(World world, BlockPos start) {
        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> seen = new HashSet<>();
        queue.add(start);
        seen.add(start);
        while (!queue.isEmpty() && seen.size() <= LIMIT) {
            BlockPos pos = queue.poll();
            for (EnumFacing facing : EnumFacing.values()) {
                BlockPos next = pos.offset(facing);
                if (!seen.contains(next) && world.getBlockState(next).getBlock() instanceof BlockSupremeFurnaceCasing) {
                    seen.add(next);
                    queue.add(next);
                }
            }
        }
        dropBomb(world, start, seen.size());
        for (BlockPos pos : seen) {
            world.setBlockToAir(pos);
        }
    }

    private static void dropBomb(World world, BlockPos pos, int blocks) {
        Item bomb = bombFor(blocks);
        if (bomb != null) {
            InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(bomb));
        }
    }

    private static Item bombFor(int blocks) {
        if (blocks == FurnaceFormation.shellCount(32)) {
            return SCRegistry.SUPREME_FURNACE_BOMB_T1;
        }
        if (blocks == FurnaceFormation.shellCount(64)) {
            return SCRegistry.SUPREME_FURNACE_BOMB_T2;
        }
        if (blocks == FurnaceFormation.shellCount(128)) {
            return SCRegistry.SUPREME_FURNACE_BOMB_T3;
        }
        return null;
    }
}
