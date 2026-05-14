package com.shiver.supermecrafting.furnace;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

/**
 * 纯数学：将（方块位置，世界方向）对映射到原版熔炉纹理的像素。
 *
 * <p>约定：
 * <ul>
 *   <li>原版纹理是 16x16；多方面是 {@code size} 个方块宽。
 *       除数是 {@code size/16}（32 → 2，64 → 4，128 → 8）。一个原版像素
 *       映射到 {@code (size/16)²} 个方块的集群。</li>
 *   <li>水平面：{@code v=0} 在顶部（{@code y=maxY}）；{@code u}
 *       沿 {@code worldDir.getClockWise()} 增加——即从外面看从左到右。</li>
 *   <li>顶/底面：{@code u} 沿 {@code x}，{@code v} 沿 {@code z}。
 *       原版 {@code furnace_top} 是旋转对称的，所以我们不根据 {@code front} 定向。</li>
 * </ul>
 */
public final class FaceMath {
    private FaceMath() {}

    public static class FaceTexel {
        private final MultiblockFace face;
        private final int u;
        private final int v;

        public FaceTexel(MultiblockFace face, int u, int v) {
            this.face = face;
            this.u = u;
            this.v = v;
        }

        public MultiblockFace getFace() { return face; }
        public int getU() { return u; }
        public int getV() { return v; }
    }

    /**
     * 对于每个世界 {@link EnumFacing}，返回该方块面显示的 {@link FaceTexel}
     * （当该面朝外时）或 {@code null}（当它朝向空心内部时）。
     * 按 {@code EnumFacing.getIndex()} 索引——数组长度 6。
     */
    public static FaceTexel[] perDirection(BlockPos pos, Region region) {
        FaceTexel[] out = new FaceTexel[6];
        int[] b = region.getBounds();
        EnumFacing front = region.getFront();
        int divisor = region.size() / 16;
        for (EnumFacing d : EnumFacing.values()) {
            if (!isOutward(pos, b, d)) continue;
            MultiblockFace mbf = multiblockFaceFor(d, front);
            int[] uv = texelOnFace(pos, b, d, divisor);
            out[d.getIndex()] = new FaceTexel(mbf, uv[0], uv[1]);
        }
        return out;
    }

    /** 如果 {@code d} 从 {@code pos} 指向边界框外部则返回 {@code true}。 */
    public static boolean isOutward(BlockPos pos, int[] b, EnumFacing d) {
        switch (d) {
            case UP:    return pos.getY() == b[4]; // maxY
            case DOWN:  return pos.getY() == b[1]; // minY
            case NORTH: return pos.getZ() == b[2]; // minZ
            case SOUTH: return pos.getZ() == b[5]; // maxZ
            case EAST:  return pos.getX() == b[3]; // maxX
            case WEST:  return pos.getX() == b[0]; // minX
            default:    return false;
        }
    }

    public static MultiblockFace multiblockFaceFor(EnumFacing worldDir, EnumFacing front) {
        if (worldDir == EnumFacing.UP) return MultiblockFace.TOP;
        if (worldDir == EnumFacing.DOWN) return MultiblockFace.BOTTOM;
        if (worldDir == front) return MultiblockFace.FRONT;
        if (worldDir == front.getOpposite()) return MultiblockFace.BACK;
        if (worldDir == front.rotateYCCW()) return MultiblockFace.LEFT;
        return MultiblockFace.RIGHT; // == front.rotateY()
    }

    private static int[] texelOnFace(BlockPos pos, int[] b, EnumFacing worldDir, int divisor) {
        if (worldDir.getAxis() == EnumFacing.Axis.Y) {
            int u = (pos.getX() - b[0]) / divisor;
            int v = (pos.getZ() - b[2]) / divisor;
            return new int[]{u, v};
        }
        int v = 15 - (pos.getY() - b[1]) / divisor;
        EnumFacing right = worldDir.rotateY();
        int u;
        switch (right) {
            case EAST:  u = (pos.getX() - b[0]) / divisor; break;
            case WEST:  u = (b[3] - pos.getX()) / divisor; break;
            case SOUTH: u = (pos.getZ() - b[2]) / divisor; break;
            case NORTH: u = (b[5] - pos.getZ()) / divisor; break;
            default:    throw new IllegalStateException("non-horizontal right? " + right);
        }
        return new int[]{u, v};
    }

    /**
     * 查找单个方向的纹素。为只需要一个面的调用者提供便利——
     * 避免分配完整的 {@code FaceTexel[6]} 数组。
     */
    @Nullable
    public static FaceTexel texelFor(BlockPos pos, Region region, EnumFacing worldDir) {
        if (!isOutward(pos, region.getBounds(), worldDir)) return null;
        MultiblockFace mbf = multiblockFaceFor(worldDir, region.getFront());
        int[] uv = texelOnFace(pos, region.getBounds(), worldDir, region.size() / 16);
        return new FaceTexel(mbf, uv[0], uv[1]);
    }
}
