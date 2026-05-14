package com.shiver.supermecrafting.client;

import com.shiver.supermecrafting.net.MultiblockLitPacket;
import com.shiver.supermecrafting.net.MultiblockSyncPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Client-side mirror of MultiblockRegions.
 * Populated via S2C packets from the server.
 */
public class ClientMultiblockRegions {

    private static final Map<UUID, ClientRegion> regions = new HashMap<>();

    public static synchronized void apply(MultiblockSyncPacket packet) {
        switch (packet.getOp()) {
            case MultiblockSyncPacket.OP_RESET:
                regions.clear();
                // Fall through to add
            case MultiblockSyncPacket.OP_ADD:
                regions.put(packet.getRegionId(), new ClientRegion(
                        packet.getMin(), packet.getMax(), packet.getFront(), packet.isLit()));
                break;
            case MultiblockSyncPacket.OP_REMOVE:
                regions.remove(packet.getRegionId());
                break;
        }
    }

    public static synchronized void applyLit(MultiblockLitPacket packet) {
        ClientRegion region = regions.get(packet.getRegionId());
        if (region != null) {
            region.lit = packet.isLit();
        }
    }

    public static synchronized ClientRegion get(UUID id) {
        return regions.get(id);
    }

    public static synchronized Map<UUID, ClientRegion> all() {
        return new HashMap<>(regions);
    }

    public static class ClientRegion {
        public final net.minecraft.util.math.BlockPos min, max;
        public final net.minecraft.util.EnumFacing front;
        public boolean lit;

        public ClientRegion(net.minecraft.util.math.BlockPos min, net.minecraft.util.math.BlockPos max,
                            net.minecraft.util.EnumFacing front, boolean lit) {
            this.min = min;
            this.max = max;
            this.front = front;
            this.lit = lit;
        }
    }
}
