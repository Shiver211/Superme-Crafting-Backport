package com.shiver.supermecrafting.client;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

/**
 * Minimal IInventory used on the client side for the Supreme Furnace GUI.
 * Stores 4 sync'd fields (litTime, litDuration, cookTime, cookTimeTotal)
 * that are pushed from the server via ContainerFurnace's field sync.
 */
public class ClientFurnaceData implements IInventory {
    private final int[] fields = new int[4];

    @Override
    public int getSizeInventory() { return 3; }

    @Override
    public boolean isEmpty() { return true; }

    @Override
    public ItemStack getStackInSlot(int index) { return ItemStack.EMPTY; }

    @Override
    public ItemStack decrStackSize(int index, int count) { return ItemStack.EMPTY; }

    @Override
    public ItemStack removeStackFromSlot(int index) { return ItemStack.EMPTY; }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {}

    @Override
    public int getInventoryStackLimit() { return 64; }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) { return true; }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) { return false; }

    @Override
    public int getField(int id) { return id >= 0 && id < fields.length ? fields[id] : 0; }

    @Override
    public void setField(int id, int value) { if (id >= 0 && id < fields.length) fields[id] = value; }

    @Override
    public int getFieldCount() { return 4; }

    @Override
    public void markDirty() {}

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public void clear() {}

    @Override
    public String getName() { return "container.supreme_crafting.supreme_furnace"; }

    @Override
    public boolean hasCustomName() { return false; }

    @Override
    public ITextComponent getDisplayName() { return new TextComponentString(getName()); }
}
