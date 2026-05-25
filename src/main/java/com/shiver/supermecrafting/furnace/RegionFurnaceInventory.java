package com.shiver.supermecrafting.furnace;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.UUID;

public class RegionFurnaceInventory implements IInventory {
    private final World world;
    private final UUID regionId;

    public RegionFurnaceInventory(World world, UUID regionId) {
        this.world = world;
        this.regionId = regionId;
    }

    private Region region() {
        return MultiblockRegions.get(world).byId(regionId);
    }

    @Override public int getSizeInventory() { return Region.SLOT_COUNT; }

    @Override
    public boolean isEmpty() {
        Region r = region();
        if (r == null) return true;
        for (ItemStack stack : r.items()) if (!stack.isEmpty()) return false;
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        Region r = region();
        return r == null ? ItemStack.EMPTY : r.items().get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        Region r = region();
        if (r == null) return ItemStack.EMPTY;
        ItemStack stack = net.minecraft.inventory.ItemStackHelper.getAndSplit(r.items(), index, count);
        markDirty();
        return stack;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        Region r = region();
        if (r == null) return ItemStack.EMPTY;
        ItemStack stack = net.minecraft.inventory.ItemStackHelper.getAndRemove(r.items(), index);
        markDirty();
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        Region r = region();
        if (r == null) return;
        r.items().set(index, stack);
        if (!stack.isEmpty() && stack.getCount() > getInventoryStackLimit()) stack.setCount(getInventoryStackLimit());
        markDirty();
    }

    @Override public String getName() { return "container.supreme_crafting.supreme_furnace"; }
    @Override public boolean hasCustomName() { return false; }
    @Override public ITextComponent getDisplayName() { return new TextComponentString(getName()); }
    @Override public int getInventoryStackLimit() { return 64; }
    @Override public void markDirty() { MultiblockRegions.get(world).markDirty(); }
    @Override public boolean isUsableByPlayer(EntityPlayer player) { return region() != null; }
    @Override public void openInventory(EntityPlayer player) {}
    @Override public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index == Region.SLOT_OUTPUT) return false;
        if (index == Region.SLOT_INPUT) return true;
        return TileEntityFurnace.isItemFuel(stack);
    }

    @Override public int getField(int id) { return 0; }
    @Override public void setField(int id, int value) {}
    @Override public int getFieldCount() { return 0; }
    @Override public void clear() { Region r = region(); if (r != null) r.items().clear(); markDirty(); }
}
