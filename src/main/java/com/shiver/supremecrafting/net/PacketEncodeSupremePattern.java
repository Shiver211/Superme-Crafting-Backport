package com.shiver.supremecrafting.net;

import com.shiver.supremecrafting.ae2.ContainerSupremePatternTerminal;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketEncodeSupremePattern implements IMessage {
    private boolean clear;

    public PacketEncodeSupremePattern() {
    }

    public PacketEncodeSupremePattern(boolean clear) {
        this.clear = clear;
    }

    @Override public void fromBytes(ByteBuf buf) { clear = buf.readBoolean(); }
    @Override public void toBytes(ByteBuf buf) { buf.writeBoolean(clear); }

    public static class Handler implements IMessageHandler<PacketEncodeSupremePattern, IMessage> {
        @Override
        public IMessage onMessage(PacketEncodeSupremePattern message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
                if (player.openContainer instanceof ContainerSupremePatternTerminal) {
                    ContainerSupremePatternTerminal container = (ContainerSupremePatternTerminal) player.openContainer;
                    if (message.clear) {
                        container.terminal().grid().clear();
                        container.terminal().markDirty();
                    } else {
                        container.terminal().encode();
                    }
                    container.detectAndSendChanges();
                }
            });
            return null;
        }
    }
}
