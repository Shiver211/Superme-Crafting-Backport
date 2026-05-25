package com.shiver.supermecrafting.furnace;

import com.shiver.supermecrafting.block.BlockSupremeFurnaceCasing;
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
        for (BlockPos pos : seen) {
            world.setBlockToAir(pos);
        }
    }
}
