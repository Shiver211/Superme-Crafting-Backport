package com.shiver.supermecrafting.client;

import com.shiver.supermecrafting.block.FurnaceRenderRegion;
import com.shiver.supermecrafting.net.PacketMultiblockSync;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SideOnly(Side.CLIENT)
public final class ClientMultiblockRegions {
    private static final Map<UUID, FurnaceRenderRegion> REGIONS = new HashMap<>();

    private ClientMultiblockRegions() {
    }

    public static FurnaceRenderRegion findContaining(BlockPos pos) {
        synchronized (REGIONS) {
            for (FurnaceRenderRegion region : REGIONS.values()) {
                if (contains(region, pos)) return region.at(pos);
            }
        }
        return null;
    }

    public static FurnaceRenderRegion findOrCreate(IBlockAccess world, BlockPos pos, EnumFacing front) {
        FurnaceRenderRegion region = findContaining(pos);
        return region == null ? FurnaceRenderRegion.fromWorld(world, pos, front) : region;
    }

    public static void apply(PacketMultiblockSync packet) {
        synchronized (REGIONS) {
            if (packet.op() == PacketMultiblockSync.RESET) {
                REGIONS.clear();
            } else if (packet.op() == PacketMultiblockSync.REMOVE) {
                REGIONS.remove(packet.id());
                markDirty(packet.region());
                return;
            }
            for (PacketMultiblockSync.Entry entry : packet.entries()) {
                REGIONS.put(entry.id, entry.region);
                markDirty(entry.region);
            }
        }
    }

    private static boolean contains(FurnaceRenderRegion region, BlockPos pos) {
        return pos.getX() >= region.min().getX() && pos.getX() <= region.max().getX()
                && pos.getY() >= region.min().getY() && pos.getY() <= region.max().getY()
                && pos.getZ() >= region.min().getZ() && pos.getZ() <= region.max().getZ();
    }

    private static void markDirty(FurnaceRenderRegion region) {
        if (region == null || Minecraft.getMinecraft().renderGlobal == null) return;
        Minecraft.getMinecraft().renderGlobal.markBlockRangeForRenderUpdate(
                region.min().getX(), region.min().getY(), region.min().getZ(),
                region.max().getX(), region.max().getY(), region.max().getZ());
    }
}
