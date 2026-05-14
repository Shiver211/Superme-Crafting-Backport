package com.shiver.supermecrafting.table;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;

public class SupremeTableTileEntity extends TileEntity implements IInventory, ITickable {

    private final SupremeTableInventory inventory = new SupremeTableInventory();
    private long modVersion = 0;

    public SupremeTableInventory getInventory() {
        return inventory;
    }

    public long getModVersion() {
        return modVersion;
    }

    private void bumpModVersion() {
        modVersion++;
        markDirty();
    }

    // --- NBT Persistence ---

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("Items", inventory.save());
        compound.setLong("ModVersion", modVersion);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("Items", 9)) {
            inventory.load(compound.getTagList("Items", 10));
        }
        modVersion = compound.getLong("ModVersion");
    }

    // --- Client-Server Sync ---

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        readFromNBT(tag);
    }

    // --- IInventory ---

    @Override
    public int getSizeInventory() {
        return SupremeTableInventory.SIZE;
    }

    @Override
    public boolean isEmpty() {
        return inventory.isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventory.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack stack = inventory.get(index);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack result = stack.splitStack(count);
        if (stack.isEmpty()) {
            inventory.set(index, ItemStack.EMPTY);
        }
        bumpModVersion();
        return result;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = inventory.get(index);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        inventory.set(index, ItemStack.EMPTY);
        bumpModVersion();
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventory.set(index, stack);
        bumpModVersion();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return world.getTileEntity(pos) == this
                && player.getDistanceSq(pos) < 64;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        inventory.clear();
        bumpModVersion();
    }

    @Override
    public String getName() {
        return "Supreme Table";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    // --- ITickable ---

    @Override
    public void update() {
        // Recipe matching is done in the Container's detectAndSendChanges
    }
}
