package com.shiver.supremecrafting.ae2;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerSupremeInterface extends Container {
    private static final int TOP_CAP = 7;
    private static final int CROP_TOP = 79;
    private static final int PATTERN_ROWS = 4;
    private static final int PATTERN_COLUMNS = 9;
    private static final int PATTERN_SLOTS = TileSupremeInterface.PATTERN_SLOTS;

    private final TileSupremeInterface tile;

    public ContainerSupremeInterface(InventoryPlayer playerInv, TileSupremeInterface tile) {
        this.tile = tile;
        for (int row = 0; row < PATTERN_ROWS; row++) {
            for (int col = 0; col < PATTERN_COLUMNS; col++) {
                addSlotToContainer(new Slot(tile, col + row * PATTERN_COLUMNS,
                        8 + col * 18, 97 - CROP_TOP + TOP_CAP + row * 18) {
                    @Override
                    public boolean isItemValid(ItemStack stack) {
                        return SupremePatternData.isEncoded(stack);
                    }
                });
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInv, col + row * 9 + 9,
                        8 + col * 18, 174 - CROP_TOP + TOP_CAP + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, 232 - CROP_TOP + TOP_CAP));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return tile.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        if (index < 0 || index >= inventorySlots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = inventorySlots.get(index);
        if (!slot.getHasStack()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getStack();
        ItemStack copy = stack.copy();
        if (index < PATTERN_SLOTS) {
            if (!mergeItemStack(stack, PATTERN_SLOTS, inventorySlots.size(), true)) return ItemStack.EMPTY;
        } else if (SupremePatternData.isEncoded(stack)) {
            if (!mergeItemStack(stack, 0, PATTERN_SLOTS, false)) return ItemStack.EMPTY;
        } else {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }
        return copy;
    }
}
