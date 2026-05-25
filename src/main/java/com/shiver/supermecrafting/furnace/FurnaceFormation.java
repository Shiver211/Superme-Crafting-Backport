package com.shiver.supermecrafting.furnace;

import com.shiver.supermecrafting.block.BlockSupremeFurnaceCasing;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

public final class FurnaceFormation {
    private static final int[] VALID_SIZES = {32, 64, 128};
    private static final int FLOOD_CAP = shellCount(128) + 1024;

    private FurnaceFormation() {
    }

    public static int shellCount(int size) {
        int inner = size - 2;
        return size * size * size - inner * inner * inner;
    }

    public static Result tryForm(World world, BlockPos start, EntityPlayer player) {
        if (MultiblockRegions.get(world).findContaining(start) != null) {
            return Result.failure("this block is already part of a formed structure");
        }
        if (!isShell(world.getBlockState(start))) {
            return Result.failure("not a shell block");
        }
        Set<BlockPos> shell = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        shell.add(start);
        queue.add(start);
        int minX = start.getX(), maxX = start.getX();
        int minY = start.getY(), maxY = start.getY();
        int minZ = start.getZ(), maxZ = start.getZ();
        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            for (EnumFacing facing : EnumFacing.values()) {
                BlockPos next = pos.offset(facing);
                if (shell.contains(next) || !isShell(world.getBlockState(next))) continue;
                shell.add(next);
                if (shell.size() > FLOOD_CAP) return Result.failure("connected casing exceeds cap");
                queue.add(next);
                minX = Math.min(minX, next.getX());
                maxX = Math.max(maxX, next.getX());
                minY = Math.min(minY, next.getY());
                maxY = Math.max(maxY, next.getY());
                minZ = Math.min(minZ, next.getZ());
                maxZ = Math.max(maxZ, next.getZ());
            }
        }
        int xs = maxX - minX + 1;
        int ys = maxY - minY + 1;
        int zs = maxZ - minZ + 1;
        if (xs != ys || ys != zs || !isValid(xs)) return Result.failure("size must be 32, 64 or 128 cube");
        if (shell.size() != shellCount(xs)) return Result.failure("shell incomplete or has extras");
        for (int x = minX + 1; x < maxX; x++) {
            for (int y = minY + 1; y < maxY; y++) {
                for (int z = minZ + 1; z < maxZ; z++) {
                    if (!world.isAirBlock(new BlockPos(x, y, z))) return Result.failure("interior must be empty");
                }
            }
        }
        BlockPos min = new BlockPos(minX, minY, minZ);
        BlockPos max = new BlockPos(maxX, maxY, maxZ);
        EnumFacing front = player == null ? EnumFacing.NORTH : player.getHorizontalFacing().getOpposite();
        Region region = MultiblockRegions.get(world).create(min, max, front);
        flip(world, min, max, true, front);
        MultiblockSync.add(world, region);
        return Result.success("Supreme Furnace formed: " + xs + "^3", region);
    }

    public static void disassemble(World world, Region region) {
        flip(world, region.min(), region.max(), false, region.front());
        MultiblockSync.remove(world, region);
        MultiblockRegions.get(world).remove(region.getId());
    }

    private static void flip(World world, BlockPos min, BlockPos max, boolean formed, EnumFacing front) {
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                flipOne(world, new BlockPos(x, min.getY(), z), formed, front);
                flipOne(world, new BlockPos(x, max.getY(), z), formed, front);
            }
        }
        for (int y = min.getY() + 1; y < max.getY(); y++) {
            for (int x = min.getX(); x <= max.getX(); x++) {
                flipOne(world, new BlockPos(x, y, min.getZ()), formed, front);
                flipOne(world, new BlockPos(x, y, max.getZ()), formed, front);
            }
            for (int z = min.getZ() + 1; z < max.getZ(); z++) {
                flipOne(world, new BlockPos(min.getX(), y, z), formed, front);
                flipOne(world, new BlockPos(max.getX(), y, z), formed, front);
            }
        }
    }

    private static void flipOne(World world, BlockPos pos, boolean formed, EnumFacing front) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof BlockSupremeFurnaceCasing) {
            world.setBlockState(pos, state.withProperty(BlockSupremeFurnaceCasing.FORMED, formed)
                    .withProperty(BlockSupremeFurnaceCasing.FRONT, front), 3);
        }
    }

    private static boolean isShell(IBlockState state) {
        return state.getBlock() instanceof BlockSupremeFurnaceCasing;
    }

    private static boolean isValid(int size) {
        for (int valid : VALID_SIZES) if (valid == size) return true;
        return false;
    }

    public static final class Result {
        private final boolean success;
        private final String message;
        private final Region region;

        private Result(boolean success, String message, Region region) {
            this.success = success;
            this.message = message;
            this.region = region;
        }

        static Result success(String message, Region region) {
            return new Result(true, message, region);
        }

        static Result failure(String message) {
            return new Result(false, "Cannot form: " + message, null);
        }

        public ITextComponent message() {
            return new TextComponentString(message);
        }
    }
}
