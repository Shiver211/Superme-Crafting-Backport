package com.shiver.supermecrafting.furnace;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import java.util.*;

/**
 * Per-world BFS that destroys connected Supreme Furnace shell blocks
 * (casings + hatch variants) a few at a time. Seeded by Furnace Destroyer
 * on player break.
 *
 * State is in-memory only — if the server stops mid-cascade, the remaining
 * casings persist as normal blocks.
 */
public final class CasingCascade {
    /** Blocks destroyed per server tick, per level. */
    private static final int MAX_PER_TICK = 100;
    /** Client sync + no neighbour cascades + no drops. */
    private static final int VOID_FLAGS = 2 | 16 | 4; // UPDATE_CLIENTS | UPDATE_KNOWN_SHAPE | NO_RERENDER

    private static final Map<WorldServer, Cascade> CASCADES = new WeakHashMap<>();

    private CasingCascade() {}

    public static void seed(WorldServer level, BlockPos start) {
        Cascade c = CASCADES.computeIfAbsent(level, k -> new Cascade());
        BlockPos imm = start.toImmutable();
        c.visited.add(imm);
        for (EnumFacing d : EnumFacing.VALUES) {
            BlockPos n = imm.offset(d).toImmutable();
            if (c.visited.add(n)) c.queue.add(n);
        }
    }

    public static void tick(WorldServer level) {
        Cascade c = CASCADES.get(level);
        if (c == null) return;
        if (c.queue.isEmpty()) {
            CASCADES.remove(level);
            return;
        }

        int destroyed = 0;
        while (destroyed < MAX_PER_TICK && !c.queue.isEmpty()) {
            BlockPos pos = c.queue.poll();
            IBlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof SupremeFurnaceCasingBlock)) continue;

            // Clear container contents so nothing drops
            TileEntity te = level.getTileEntity(pos);
            if (te instanceof IInventory) {
                ((IInventory) te).clear();
            }
            level.setBlockState(pos, Blocks.AIR.getDefaultState(), VOID_FLAGS);
            destroyed++;
            for (EnumFacing d : EnumFacing.VALUES) {
                BlockPos n = pos.offset(d).toImmutable();
                if (c.visited.add(n)) c.queue.add(n);
            }
        }

        if (c.queue.isEmpty()) CASCADES.remove(level);
    }

    private static final class Cascade {
        final Deque<BlockPos> queue = new ArrayDeque<>();
        final Set<BlockPos> visited = new HashSet<>();
    }
}
