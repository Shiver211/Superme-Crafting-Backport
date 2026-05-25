package com.shiver.supermecrafting.net;

import com.shiver.supermecrafting.table.ContainerSupremeTable;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashMap;
import java.util.Map;

public class PacketTransferRecipe implements IMessage {
    private final Map<Integer, String> targets = new HashMap<>();

    public PacketTransferRecipe() {
    }

    public PacketTransferRecipe(Map<Integer, String> targets) {
        this.targets.putAll(targets);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int count = buf.readInt();
        for (int i = 0; i < count; i++) {
            targets.put(buf.readInt(), ByteBufUtils.readUTF8String(buf));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(targets.size());
        for (Map.Entry<Integer, String> entry : targets.entrySet()) {
            buf.writeInt(entry.getKey());
            ByteBufUtils.writeUTF8String(buf, entry.getValue());
        }
    }

    public static class Handler implements IMessageHandler<PacketTransferRecipe, IMessage> {
        @Override
        public IMessage onMessage(PacketTransferRecipe message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> transfer(player, message.targets));
            return null;
        }

        private static void transfer(EntityPlayerMP player, Map<Integer, String> targets) {
            if (!(player.openContainer instanceof ContainerSupremeTable)) return;
            ContainerSupremeTable container = (ContainerSupremeTable) player.openContainer;
            for (Map.Entry<Integer, String> entry : targets.entrySet()) {
                ItemStack found = takeOne(player, entry.getValue());
                if (!found.isEmpty()) {
                    container.table().setInventorySlotContents(entry.getKey(), found);
                }
            }
        }

        private static ItemStack takeOne(EntityPlayerMP player, String key) {
            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                ItemStack stack = player.inventory.getStackInSlot(i);
                if (!stack.isEmpty() && stackKey(stack).equals(key)) {
                    ItemStack out = stack.copy();
                    out.setCount(1);
                    stack.shrink(1);
                    return out;
                }
            }
            return ItemStack.EMPTY;
        }

        private static String stackKey(ItemStack stack) {
            String id = stack.getItem().getRegistryName().toString();
            return id + "@" + stack.getMetadata();
        }
    }
}
