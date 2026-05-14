package com.shiver.supermecrafting.table;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;

public final class SupremeTableInventory {
    public static final int WIDTH = 81;
    public static final int HEIGHT = 81;
    public static final int SIZE = WIDTH * HEIGHT; // 6561

    private static final String NBT_SLOT = "Slot";
    private static final String NBT_ITEMS = "Items";

    private final NonNullList<ItemStack> stacks = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    public int size() {
        return SIZE;
    }

    public ItemStack get(int index) {
        checkIndex(index);
        return stacks.get(index);
    }

    public void set(int index, ItemStack stack) {
        checkIndex(index);
        stacks.set(index, stack.isEmpty() ? ItemStack.EMPTY : stack);
    }

    public ItemStack get(int x, int y) {
        return get(indexOf(x, y));
    }

    public void set(int x, int y, ItemStack stack) {
        set(indexOf(x, y), stack);
    }

    public boolean isEmpty() {
        for (ItemStack s : stacks) {
            if (!s.isEmpty()) return false;
        }
        return true;
    }

    public void clear() {
        for (int i = 0; i < SIZE; i++) {
            stacks.set(i, ItemStack.EMPTY);
        }
    }

    public static int indexOf(int x, int y) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) {
            throw new IndexOutOfBoundsException("coords out of range: x=" + x + " y=" + y);
        }
        return x + y * WIDTH;
    }

    public static int xOf(int index) {
        checkIndex(index);
        return index % WIDTH;
    }

    public static int yOf(int index) {
        checkIndex(index);
        return index / WIDTH;
    }

    private static void checkIndex(int index) {
        if (index < 0 || index >= SIZE) {
            throw new IndexOutOfBoundsException("slot index out of range: " + index);
        }
    }

    /**
     * Sparse save: only non-empty stacks are written with int slot indices.
     */
    public NBTTagList save() {
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < SIZE; i++) {
            ItemStack stack = stacks.get(i);
            if (stack.isEmpty()) continue;
            NBTTagCompound entry = new NBTTagCompound();
            entry.setInteger(NBT_SLOT, i);
            stack.writeToNBT(entry);
            list.appendTag(entry);
        }
        return list;
    }

    /**
     * Loads from NBT. Clears existing contents first. Out-of-range slots are skipped.
     */
    public void load(NBTTagList list) {
        clear();
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            int slot = entry.getInteger(NBT_SLOT);
            if (slot < 0 || slot >= SIZE) continue;
            stacks.set(slot, new ItemStack(entry));
        }
    }

    public NonNullList<ItemStack> items() {
        return stacks;
    }
}
