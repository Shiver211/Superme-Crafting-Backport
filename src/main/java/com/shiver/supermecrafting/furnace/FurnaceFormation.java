package com.shiver.supermecrafting.furnace;

import com.shiver.supermecrafting.net.SCNetwork;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

/**
 * Forms / disassembles hollow Supreme Furnace structures.
 * Three valid cube sizes: 32, 64, 128.
 *
 * Form algorithm (wrench right-click on any casing block):
 * 1. Flood-fill connected casing component (6-axis adjacency), capped to FLOOD_CAP.
 * 2. Reject unless bounding box is a cube of valid size and shell count matches.
 * 3. Reject unless every interior position is air.
 * 4. Flip FORMED on every shell cell; register new Region; broadcast to clients.
 */
public final class FurnaceFormation {
    public static final int[] VALID_SIZES = {32, 64, 128};
    public static final int MAX_SIZE = 128;
    private static final int FLOOD_CAP = shellCount(MAX_SIZE) + 1024;

    // Block flags: UPDATE_CLIENTS (2) + UPDATE_KNOWN_SHAPE (16)
    private static final int BLOCK_FLIP_FLAGS = 2 | 16;

    private FurnaceFormation() {}

    public interface Result {
        class Success implements Result {
            private final Region region;
            public Success(Region region) { this.region = region; }
            public Region region() { return region; }
        }
        class Failure implements Result {
            private final String reason;
            public Failure(String reason) { this.reason = reason; }
            public String reason() { return reason; }
        }
    }

    public static int shellCount(int size) {
        int inner = size - 2;
        return size * size * size - inner * inner * inner;
    }

    private static boolean isValidSize(int size) {
        for (int s : VALID_SIZES) if (s == size) return true;
        return false;
    }

    public static Result tryForm(WorldServer level, BlockPos start, EntityPlayer formingPlayer) {
        MultiblockRegions regions = MultiblockRegions.get(level);
        if (regions.findContaining(start) != null) {
            return new Result.Failure("this block is already part of a formed structure");
        }
        if (!isShell(level.getBlockState(start))) {
            return new Result.Failure("not a shell block");
        }

        Set<BlockPos> casingSet = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        BlockPos startImm = start.toImmutable();
        queue.add(startImm);
        casingSet.add(startImm);
        int minX = start.getX(), maxX = minX;
        int minY = start.getY(), maxY = minY;
        int minZ = start.getZ(), maxZ = minZ;

        while (!queue.isEmpty()) {
            BlockPos p = queue.poll();
            for (EnumFacing d : EnumFacing.VALUES) {
                BlockPos n = p.offset(d).toImmutable();
                if (casingSet.contains(n)) continue;
                if (!isShell(level.getBlockState(n))) continue;
                casingSet.add(n);
                if (casingSet.size() > FLOOD_CAP) {
                    return new Result.Failure("connected casing exceeds " + FLOOD_CAP + " blocks");
                }
                queue.add(n);
                if (n.getX() < minX) minX = n.getX();
                if (n.getX() > maxX) maxX = n.getX();
                if (n.getY() < minY) minY = n.getY();
                if (n.getY() > maxY) maxY = n.getY();
                if (n.getZ() < minZ) minZ = n.getZ();
                if (n.getZ() > maxZ) maxZ = n.getZ();
            }
        }

        int xs = maxX - minX + 1;
        int ys = maxY - minY + 1;
        int zs = maxZ - minZ + 1;
        if (xs != ys || ys != zs) {
            return new Result.Failure("bounding box is " + xs + "x" + ys + "x" + zs + ", must be a cube");
        }
        int size = xs;
        if (!isValidSize(size)) {
            return new Result.Failure("size " + size + " not one of 32, 64, 128");
        }
        int expectedShell = shellCount(size);
        if (casingSet.size() != expectedShell) {
            return new Result.Failure("found " + casingSet.size() + " casing blocks, expected "
                    + expectedShell + " (shell incomplete or has extras)");
        }

        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int x = minX + 1; x < maxX; x++) {
            for (int y = minY + 1; y < maxY; y++) {
                for (int z = minZ + 1; z < maxZ; z++) {
                    m.setPos(x, y, z);
                    if (!level.isAirBlock(m)) {
                        return new Result.Failure("interior must be empty at " + x + "," + y + "," + z);
                    }
                }
            }
        }

        EnumFacing front = (formingPlayer != null)
                ? formingPlayer.getHorizontalFacing().getOpposite()
                : EnumFacing.NORTH;

        int[] bounds = {minX, minY, minZ, maxX, maxY, maxZ};
        Region region = regions.create(bounds, front);

        flipShell(level, bounds, true);
        broadcastAdd(level, region);
        return new Result.Success(region);
    }

    public static void disassemble(WorldServer level, Region region) {
        flipShell(level, region.getBounds(), false);
        MultiblockRegions.get(level).remove(region.getId());
        broadcastRemove(level, region.getId());
    }

    /**
     * Flips FORMED on every shell cell of the bounds.
     */
    private static void flipShell(WorldServer level, int[] b, boolean formed) {
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        int minX = b[0], maxX = b[3];
        int minY = b[1], maxY = b[4];
        int minZ = b[2], maxZ = b[5];

        // Bottom + top faces
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                flipCell(level, m.setPos(x, minY, z), formed);
                flipCell(level, m.setPos(x, maxY, z), formed);
            }
        }
        // Vertical sides
        for (int y = minY + 1; y < maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                flipCell(level, m.setPos(x, y, minZ), formed);
                flipCell(level, m.setPos(x, y, maxZ), formed);
            }
            for (int z = minZ + 1; z < maxZ; z++) {
                flipCell(level, m.setPos(minX, y, z), formed);
                flipCell(level, m.setPos(maxX, y, z), formed);
            }
        }
    }

    private static void flipCell(WorldServer level, BlockPos pos, boolean formed) {
        IBlockState s = level.getBlockState(pos);
        if (!isShell(s)) return;
        IBlockState target = s.withProperty(SupremeFurnaceCasingBlock.FORMED, formed);
        if (s != target) {
            level.setBlockState(pos, target, BLOCK_FLIP_FLAGS);
        }
    }

    private static boolean isShell(IBlockState s) {
        return s.getBlock() instanceof SupremeFurnaceCasingBlock;
    }

    private static void broadcastAdd(WorldServer level, Region region) {
        int[] b = region.getBounds();
        SCNetwork.INSTANCE.sendToAll(new com.shiver.supermecrafting.net.MultiblockSyncPacket(
                com.shiver.supermecrafting.net.MultiblockSyncPacket.OP_ADD,
                region.getId(),
                new BlockPos(b[0], b[1], b[2]),
                new BlockPos(b[3], b[4], b[5]),
                region.getFront(),
                region.isLit()));
    }

    private static void broadcastRemove(WorldServer level, java.util.UUID id) {
        SCNetwork.INSTANCE.sendToAll(new com.shiver.supermecrafting.net.MultiblockSyncPacket(
                com.shiver.supermecrafting.net.MultiblockSyncPacket.OP_REMOVE,
                id,
                BlockPos.ORIGIN,
                BlockPos.ORIGIN,
                EnumFacing.NORTH,
                false));
    }
}
