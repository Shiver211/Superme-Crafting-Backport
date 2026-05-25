package com.shiver.supermecrafting.table;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ViewportSlot extends Slot {
    public ViewportSlot(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean isHere(IInventory inv, int slotIn) {
        return inv == inventory && slotIn == getSlotIndex();
    }

    @Override
    public int getSlotStackLimit() {
        return 64;
    }
}
