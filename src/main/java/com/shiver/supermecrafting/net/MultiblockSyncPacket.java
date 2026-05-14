package com.shiver.supermecrafting.net;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class MultiblockSyncPacket implements IMessage {

    public static final int OP_RESET = 0;
    public static final int OP_ADD = 1;
    public static final int OP_REMOVE = 2;

    private int op;
    private UUID regionId;
    private BlockPos min, max;
    private EnumFacing front;
    private boolean lit;

    public MultiblockSyncPacket() {}

    public MultiblockSyncPacket(int op, UUID regionId, BlockPos min, BlockPos max,
                                 EnumFacing front, boolean lit) {
        this.op = op;
        this.regionId = regionId;
        this.min = min;
        this.max = max;
        this.front = front;
        this.lit = lit;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        op = buf.readByte();
        regionId = new UUID(buf.readLong(), buf.readLong());
        min = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        max = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        front = EnumFacing.byIndex(buf.readByte());
        lit = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(op);
        buf.writeLong(regionId.getMostSignificantBits());
        buf.writeLong(regionId.getLeastSignificantBits());
        buf.writeInt(min.getX()); buf.writeInt(min.getY()); buf.writeInt(min.getZ());
        buf.writeInt(max.getX()); buf.writeInt(max.getY()); buf.writeInt(max.getZ());
        buf.writeByte(front.getIndex());
        buf.writeBoolean(lit);
    }

    public int getOp() { return op; }
    public UUID getRegionId() { return regionId; }
    public BlockPos getMin() { return min; }
    public BlockPos getMax() { return max; }
    public EnumFacing getFront() { return front; }
    public boolean isLit() { return lit; }

    public static class Handler implements IMessageHandler<MultiblockSyncPacket, IMessage> {
        @Override
        public IMessage onMessage(MultiblockSyncPacket message, MessageContext ctx) {
            net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> {
                com.shiver.supermecrafting.client.ClientMultiblockRegions.apply(message);
            });
            return null;
        }
    }
}
