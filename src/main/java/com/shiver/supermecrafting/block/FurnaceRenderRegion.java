package com.shiver.supermecrafting.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class FurnaceRenderRegion {
    private static final int[] VALID_SIZES = {32, 64, 128};

    private final BlockPos min;
    private final BlockPos max;
    private final BlockPos pos;
    private final EnumFacing front;
    private final boolean lit;

    private FurnaceRenderRegion(BlockPos min, BlockPos max, BlockPos pos, EnumFacing front, boolean lit) {
        this.min = min;
        this.max = max;
        this.pos = pos;
        this.front = front;
        this.lit = lit;
    }

    public static FurnaceRenderRegion synced(BlockPos min, BlockPos max, EnumFacing front, boolean lit) {
        return new FurnaceRenderRegion(min, max, BlockPos.ORIGIN, front, lit);
    }

    public FurnaceRenderRegion at(BlockPos pos) {
        return new FurnaceRenderRegion(min, max, pos, front, lit);
    }

    public static FurnaceRenderRegion fromWorld(IBlockAccess world, BlockPos pos, EnumFacing front) {
        BlockPos min = new BlockPos(scan(world, pos, EnumFacing.WEST), scan(world, pos, EnumFacing.DOWN),
                scan(world, pos, EnumFacing.NORTH));
        BlockPos max = new BlockPos(scan(world, pos, EnumFacing.EAST), scan(world, pos, EnumFacing.UP),
                scan(world, pos, EnumFacing.SOUTH));
        if (!valid(max.getX() - min.getX() + 1)
                || !valid(max.getY() - min.getY() + 1)
                || !valid(max.getZ() - min.getZ() + 1)) {
            return null;
        }
        return new FurnaceRenderRegion(min, max, pos, front, false);
    }

    private static int scan(IBlockAccess world, BlockPos start, EnumFacing direction) {
        BlockPos pos = start;
        int last = coordinate(pos, direction.getAxis());
        for (int i = 0; i < 128; i++) {
            BlockPos next = pos.offset(direction);
            IBlockState state = world.getBlockState(next);
            if (!(state.getBlock() instanceof BlockSupremeFurnaceCasing)
                    || !state.getValue(BlockSupremeFurnaceCasing.FORMED)) {
                return last;
            }
            pos = next;
            last = coordinate(pos, direction.getAxis());
        }
        return last;
    }

    private static int coordinate(BlockPos pos, EnumFacing.Axis axis) {
        if (axis == EnumFacing.Axis.X) return pos.getX();
        if (axis == EnumFacing.Axis.Y) return pos.getY();
        return pos.getZ();
    }

    private static boolean valid(int size) {
        for (int valid : VALID_SIZES) {
            if (valid == size) return true;
        }
        return false;
    }

    public BlockPos min() {
        return min;
    }

    public BlockPos max() {
        return max;
    }

    public BlockPos pos() {
        return pos;
    }

    public EnumFacing front() {
        return front;
    }

    public boolean lit() {
        return lit;
    }

    public int size() {
        return max.getX() - min.getX() + 1;
    }
}
