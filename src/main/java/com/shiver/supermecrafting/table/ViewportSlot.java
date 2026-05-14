package com.shiver.supermecrafting.table;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * Slot subclass for the 81x81 Supreme Table grid.
 * Disables vanilla's hover highlight (the GUI draws its own at variable cell size).
 */
public class ViewportSlot extends Slot {

    public ViewportSlot(IInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean isEnabled() {
        // Always enabled; the GUI controls visibility by setting xPos/yPos
        return true;
    }
}
