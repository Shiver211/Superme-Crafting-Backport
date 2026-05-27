package com.shiver.supremecrafting.net;

import com.shiver.supremecrafting.block.FurnaceRenderRegion;
import com.shiver.supremecrafting.furnace.Region;
import com.shiver.supremecrafting.SupremeCrafting;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class PacketMultiblockSync implements IMessage {
    public static final byte RESET = 0;
    public static final byte ADD = 1;
    public static final byte REMOVE = 2;

    private byte op;
    private UUID id;
    private FurnaceRenderRegion region;
    private final List<Entry> entries = new ArrayList<>();

    public PacketMultiblockSync() {
    }

    private PacketMultiblockSync(byte op) {
        this.op = op;
    }

    public static PacketMultiblockSync reset(Collection<Region> regions) {
        PacketMultiblockSync packet = new PacketMultiblockSync(RESET);
        for (Region region : regions) {
            packet.entries.add(Entry.of(region));
        }
        return packet;
    }

    public static PacketMultiblockSync add(Region region) {
        PacketMultiblockSync packet = new PacketMultiblockSync(ADD);
        packet.entries.add(Entry.of(region));
        return packet;
    }

    public static PacketMultiblockSync remove(Region region) {
        PacketMultiblockSync packet = new PacketMultiblockSync(REMOVE);
        packet.id = region.getId();
        packet.region = renderRegion(region);
        return packet;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        op = buf.readByte();
        if (op == REMOVE) {
            id = new UUID(buf.readLong(), buf.readLong());
            region = readRegion(buf);
            return;
        }
        int count = buf.readInt();
        for (int i = 0; i < count; i++) {
            UUID entryId = new UUID(buf.readLong(), buf.readLong());
            entries.add(new Entry(entryId, readRegion(buf)));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(op);
        if (op == REMOVE) {
            buf.writeLong(id.getMostSignificantBits());
            buf.writeLong(id.getLeastSignificantBits());
            writeRegion(buf, region);
            return;
        }
        buf.writeInt(entries.size());
        for (Entry entry : entries) {
            buf.writeLong(entry.id.getMostSignificantBits());
            buf.writeLong(entry.id.getLeastSignificantBits());
            writeRegion(buf, entry.region);
        }
    }

    public byte op() {
        return op;
    }

    public UUID id() {
        return id;
    }

    public FurnaceRenderRegion region() {
        return region;
    }

    public List<Entry> entries() {
        return entries;
    }

    private static FurnaceRenderRegion readRegion(ByteBuf buf) {
        BlockPos min = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        BlockPos max = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        EnumFacing front = EnumFacing.byHorizontalIndex(buf.readByte());
        boolean lit = buf.readBoolean();
        return FurnaceRenderRegion.synced(min, max, front, lit);
    }

    private static void writeRegion(ByteBuf buf, FurnaceRenderRegion region) {
        buf.writeInt(region.min().getX());
        buf.writeInt(region.min().getY());
        buf.writeInt(region.min().getZ());
        buf.writeInt(region.max().getX());
        buf.writeInt(region.max().getY());
        buf.writeInt(region.max().getZ());
        buf.writeByte(region.front().getHorizontalIndex());
        buf.writeBoolean(region.lit());
    }

    private static FurnaceRenderRegion renderRegion(Region region) {
        return FurnaceRenderRegion.synced(region.min(), region.max(), region.front(), region.isLit());
    }

    public static final class Entry {
        public final UUID id;
        public final FurnaceRenderRegion region;

        private Entry(UUID id, FurnaceRenderRegion region) {
            this.id = id;
            this.region = region;
        }

        private static Entry of(Region region) {
            return new Entry(region.getId(), renderRegion(region));
        }
    }

    public static class Handler implements IMessageHandler<PacketMultiblockSync, IMessage> {
        @Override
        public IMessage onMessage(PacketMultiblockSync message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(
                    () -> SupremeCrafting.proxy.applyMultiblockSync(message));
            return null;
        }
    }
}
