package com.shiver.supermecrafting.net;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class MultiblockLitPacket implements IMessage {

    private UUID regionId;
    private boolean lit;

    public MultiblockLitPacket() {}

    public MultiblockLitPacket(UUID regionId, boolean lit) {
        this.regionId = regionId;
        this.lit = lit;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        regionId = new UUID(buf.readLong(), buf.readLong());
        lit = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(regionId.getMostSignificantBits());
        buf.writeLong(regionId.getLeastSignificantBits());
        buf.writeBoolean(lit);
    }

    public UUID getRegionId() { return regionId; }
    public boolean isLit() { return lit; }

    public static class Handler implements IMessageHandler<MultiblockLitPacket, IMessage> {
        @Override
        public IMessage onMessage(MultiblockLitPacket message, MessageContext ctx) {
            net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> {
                com.shiver.supermecrafting.client.ClientMultiblockRegions.applyLit(message);
            });
            return null;
        }
    }
}
