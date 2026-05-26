package com.shiver.supermecrafting.net;

import com.shiver.supermecrafting.ae2.SupremeTableAe2Bridge;
import com.shiver.supermecrafting.table.ContainerSupremeTable;
import com.shiver.supermecrafting.table.SupremeTableInventory;
import com.shiver.supermecrafting.table.TileSupremeTable;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class PacketReturnTableItems implements IMessage {
    private static final String AE2_MOD_ID = "appliedenergistics2";
    private static final EnumFacing[] STORAGE_FACES = {
            EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST
    };

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public static class Handler implements IMessageHandler<PacketReturnTableItems, IMessage> {
        @Override
        public IMessage onMessage(PacketReturnTableItems message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> returnItems(player));
            return null;
        }

        private static void returnItems(EntityPlayerMP player) {
            if (!(player.openContainer instanceof ContainerSupremeTable)) {
                return;
            }
            ContainerSupremeTable container = (ContainerSupremeTable) player.openContainer;
            TileSupremeTable table = container.table();
            for (int slot = 0; slot < SupremeTableInventory.SIZE; slot++) {
                ItemStack stack = table.getStackInSlot(slot);
                if (stack.isEmpty()) {
                    continue;
                }
                ItemStack remaining = insertIntoAe(player, table, stack.copy());
                remaining = insertIntoAdjacentContainers(table, remaining);
                remaining = insertIntoPlayer(player, remaining);
                table.setInventorySlotContents(slot, remaining);
            }
            table.markDirty();
            container.detectAndSendChanges();
        }

        private static ItemStack insertIntoAe(EntityPlayerMP player, TileSupremeTable table, ItemStack stack) {
            if (stack.isEmpty() || !Loader.isModLoaded(AE2_MOD_ID)) {
                return stack;
            }
            return SupremeTableAe2Bridge.insert(player, table, stack);
        }

        private static ItemStack insertIntoAdjacentContainers(TileSupremeTable table, ItemStack stack) {
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            for (EnumFacing face : STORAGE_FACES) {
                TileEntity tile = table.getWorld().getTileEntity(table.getPos().offset(face));
                if (tile == null) {
                    continue;
                }
                EnumFacing accessFace = face.getOpposite();
                stack = insertIntoItemHandler(tile, accessFace, stack);
                if (stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                stack = insertIntoInventory(tile, accessFace, stack);
                if (stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
            return stack;
        }

        private static ItemStack insertIntoItemHandler(TileEntity tile, EnumFacing side, ItemStack stack) {
            IItemHandler handler = itemHandler(tile, side);
            if (handler == null) {
                return stack;
            }
            return ItemHandlerHelper.insertItemStacked(handler, stack, false);
        }

        private static IItemHandler itemHandler(TileEntity tile, EnumFacing side) {
            if (tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)) {
                return tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
            }
            if (tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                return tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            }
            return null;
        }

        private static ItemStack insertIntoInventory(TileEntity tile, EnumFacing side, ItemStack stack) {
            if (!(tile instanceof IInventory)) {
                return stack;
            }
            IInventory inventory = (IInventory) tile;
            if (inventory instanceof ISidedInventory) {
                ISidedInventory sided = (ISidedInventory) inventory;
                for (int slot : sided.getSlotsForFace(side)) {
                    stack = insertIntoInventorySlot(inventory, slot, stack, sided.canInsertItem(slot, stack, side));
                    if (stack.isEmpty()) {
                        return ItemStack.EMPTY;
                    }
                }
                return stack;
            }
            for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
                stack = insertIntoInventorySlot(inventory, slot, stack, inventory.isItemValidForSlot(slot, stack));
                if (stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
            return stack;
        }

        private static ItemStack insertIntoInventorySlot(IInventory inventory, int slot, ItemStack stack, boolean canInsert) {
            if (!canInsert || stack.isEmpty()) {
                return stack;
            }
            ItemStack existing = inventory.getStackInSlot(slot);
            int limit = Math.min(inventory.getInventoryStackLimit(), stack.getMaxStackSize());
            if (existing.isEmpty()) {
                ItemStack inserted = stack.copy();
                inserted.setCount(Math.min(stack.getCount(), limit));
                inventory.setInventorySlotContents(slot, inserted);
                stack.shrink(inserted.getCount());
                inventory.markDirty();
                return stack.isEmpty() ? ItemStack.EMPTY : stack;
            }
            if (!ItemHandlerHelper.canItemStacksStack(existing, stack) || existing.getCount() >= limit) {
                return stack;
            }
            int inserted = Math.min(stack.getCount(), limit - existing.getCount());
            existing.grow(inserted);
            stack.shrink(inserted);
            inventory.markDirty();
            return stack.isEmpty() ? ItemStack.EMPTY : stack;
        }

        private static ItemStack insertIntoPlayer(EntityPlayerMP player, ItemStack stack) {
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack remaining = stack.copy();
            if (player.inventory.addItemStackToInventory(remaining)) {
                player.inventory.markDirty();
                return ItemStack.EMPTY;
            }
            player.inventory.markDirty();
            return remaining;
        }
    }
}
