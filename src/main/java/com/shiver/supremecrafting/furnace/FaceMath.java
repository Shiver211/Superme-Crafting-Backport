package com.shiver.supremecrafting.furnace;

import com.shiver.supremecrafting.block.FurnaceRenderRegion;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public final class FaceMath {
    private FaceMath() {
    }

    public static FaceTexel texelFor(BlockPos pos, FurnaceRenderRegion region, EnumFacing worldDir) {
        if (!isOutward(pos, region, worldDir)) return null;
        MultiblockFace face = multiblockFaceFor(worldDir, region.front());
        int[] uv = texelOnFace(pos, region, worldDir, region.size() / 16);
        return new FaceTexel(face, uv[0], uv[1]);
    }

    private static boolean isOutward(BlockPos pos, FurnaceRenderRegion region, EnumFacing direction) {
        switch (direction) {
            case UP: return pos.getY() == region.max().getY();
            case DOWN: return pos.getY() == region.min().getY();
            case NORTH: return pos.getZ() == region.min().getZ();
            case SOUTH: return pos.getZ() == region.max().getZ();
            case EAST: return pos.getX() == region.max().getX();
            case WEST: return pos.getX() == region.min().getX();
            default: return false;
        }
    }

    private static MultiblockFace multiblockFaceFor(EnumFacing worldDir, EnumFacing front) {
        if (worldDir == EnumFacing.UP) return MultiblockFace.TOP;
        if (worldDir == EnumFacing.DOWN) return MultiblockFace.BOTTOM;
        if (worldDir == front) return MultiblockFace.FRONT;
        if (worldDir == front.getOpposite()) return MultiblockFace.BACK;
        if (worldDir == front.rotateYCCW()) return MultiblockFace.LEFT;
        return MultiblockFace.RIGHT;
    }

    private static int[] texelOnFace(BlockPos pos, FurnaceRenderRegion region, EnumFacing worldDir, int divisor) {
        if (worldDir.getAxis() == EnumFacing.Axis.Y) {
            int u = (pos.getX() - region.min().getX()) / divisor;
            int v = (pos.getZ() - region.min().getZ()) / divisor;
            return new int[]{u, v};
        }
        int v = 15 - (pos.getY() - region.min().getY()) / divisor;
        EnumFacing right = worldDir.rotateY();
        int u;
        switch (right) {
            case EAST:
                u = (pos.getX() - region.min().getX()) / divisor;
                break;
            case WEST:
                u = (region.max().getX() - pos.getX()) / divisor;
                break;
            case SOUTH:
                u = (pos.getZ() - region.min().getZ()) / divisor;
                break;
            case NORTH:
                u = (region.max().getZ() - pos.getZ()) / divisor;
                break;
            default:
                u = 0;
                break;
        }
        return new int[]{u, v};
    }

    public static final class FaceTexel {
        private final MultiblockFace face;
        private final int u;
        private final int v;

        private FaceTexel(MultiblockFace face, int u, int v) {
            this.face = face;
            this.u = u;
            this.v = v;
        }

        public MultiblockFace face() {
            return face;
        }

        public int u() {
            return u;
        }

        public int v() {
            return v;
        }
    }
}
