package com.shiver.supremecrafting.ae2;

import com.shiver.supremecrafting.table.SupremeTableInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileSupremePatternTerminal extends TileEntity implements IInventory {
    public static final int GRID_SIZE = SupremeTableInventory.SIZE;
    public static final int BLANK_SLOT = GRID_SIZE;
    public static final int OUTPUT_SLOT = GRID_SIZE + 1;
    public static final int SIZE = GRID_SIZE + 2;

    private final SupremeTableInventory grid = new SupremeTableInventory();
    private ItemStack blankPattern = ItemStack.EMPTY;
    private ItemStack encodedPattern = ItemStack.EMPTY;
    private long modVersion;

    public SupremeTableInventory grid() {
        return grid;
    }

    public long modVersion() {
        return modVersion;
    }

    public boolean encode() {
        if (world == null || blankPattern.isEmpty() || !encodedPattern.isEmpty()) {
            return false;
        }
        ItemStack encoded = SupremePatternData.encodeCurrent(blankPattern, grid, world);
        if (encoded.isEmpty()) {
            return false;
        }
        blankPattern.shrink(1);
        if (blankPattern.isEmpty()) {
            blankPattern = ItemStack.EMPTY;
        }
        encodedPattern = encoded;
        markDirty();
        return true;
    }

    @Override
    public int getSizeInventory() {
        return SIZE;
    }

    @Override
    public boolean isEmpty() {
        return grid.isEmpty() && blankPattern.isEmpty() && encodedPattern.isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        if (index < GRID_SIZE) {
            return grid.get(index);
        }
        if (index == BLANK_SLOT) {
            return blankPattern;
        }
        if (index == OUTPUT_SLOT) {
            return encodedPattern;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack existing = getStackInSlot(index);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack taken = existing.splitStack(count);
        if (existing.isEmpty()) {
            setInventorySlotContents(index, ItemStack.EMPTY);
        } else {
            markDirty();
        }
        return taken;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = getStackInSlot(index);
        setInventorySlotContents(index, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index < GRID_SIZE) {
            ItemStack ghost = stack == null ? ItemStack.EMPTY : stack.copy();
            if (!ghost.isEmpty()) {
                ghost.setCount(1);
            }
            grid.set(index, ghost);
        } else if (index == BLANK_SLOT) {
            blankPattern = stack == null ? ItemStack.EMPTY : stack;
        } else if (index == OUTPUT_SLOT) {
            encodedPattern = stack == null ? ItemStack.EMPTY : stack;
        }
        markDirty();
    }

    @Override
    public String getName() {
        return "container.supreme_crafting.supreme_pattern_terminal";
    }

    @Override public boolean hasCustomName() { return false; }
    @Override public int getInventoryStackLimit() { return 64; }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return world != null && world.getTileEntity(pos) == this
                && player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override public void openInventory(EntityPlayer player) {}
    @Override public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index != OUTPUT_SLOT;
    }

    @Override public int getField(int id) { return 0; }
    @Override public void setField(int id, int value) {}
    @Override public int getFieldCount() { return 0; }

    @Override
    public void clear() {
        grid.clear();
        blankPattern = ItemStack.EMPTY;
        encodedPattern = ItemStack.EMPTY;
        markDirty();
    }

    @Override
    public void markDirty() {
        modVersion++;
        super.markDirty();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("Grid", grid.save());
        compound.setTag("BlankPattern", blankPattern.writeToNBT(new NBTTagCompound()));
        compound.setTag("EncodedPattern", encodedPattern.writeToNBT(new NBTTagCompound()));
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        grid.load(compound.getTagList("Grid", 10));
        blankPattern = new ItemStack(compound.getCompoundTag("BlankPattern"));
        encodedPattern = new ItemStack(compound.getCompoundTag("EncodedPattern"));
    }

    public void dropContents() {
        if (world == null) {
            return;
        }
        if (!blankPattern.isEmpty()) {
            InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), blankPattern);
        }
        if (!encodedPattern.isEmpty()) {
            InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), encodedPattern);
        }
    }
}
