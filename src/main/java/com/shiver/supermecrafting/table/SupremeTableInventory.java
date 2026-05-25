package com.shiver.supermecrafting.table;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;

public class SupremeTableInventory {
    public static final int WIDTH = 81;
    public static final int HEIGHT = 81;
    public static final int SIZE = WIDTH * HEIGHT;

    private final NonNullList<ItemStack> stacks = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    public ItemStack get(int index) {
        return stacks.get(index);
    }

    public void set(int index, ItemStack stack) {
        stacks.set(index, stack == null ? ItemStack.EMPTY : stack);
    }

    public NonNullList<ItemStack> items() {
        return stacks;
    }

    public boolean isEmpty() {
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void clear() {
        for (int i = 0; i < SIZE; i++) {
            stacks.set(i, ItemStack.EMPTY);
        }
    }

    public NBTTagList save() {
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < SIZE; i++) {
            ItemStack stack = stacks.get(i);
            if (!stack.isEmpty()) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setInteger("Slot", i);
                tag.setTag("Item", stack.writeToNBT(new NBTTagCompound()));
                list.appendTag(tag);
            }
        }
        return list;
    }

    public void load(NBTTagList list) {
        clear();
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int slot = tag.getInteger("Slot");
            if (slot >= 0 && slot < SIZE) {
                stacks.set(slot, new ItemStack(tag.getCompoundTag("Item")));
            }
        }
    }

    public static int indexOf(int x, int y) {
        return x + y * WIDTH;
    }

    public static int xOf(int index) {
        return index % WIDTH;
    }

    public static int yOf(int index) {
        return index / WIDTH;
    }
}
