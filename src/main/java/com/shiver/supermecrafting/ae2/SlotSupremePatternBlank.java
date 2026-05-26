package com.shiver.supermecrafting.ae2;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotSupremePatternBlank extends Slot {
    public SlotSupremePatternBlank(IInventory inventoryIn, int index, int xPosition, int yPosition) {
        super(inventoryIn, index, xPosition, yPosition);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == AE2Module.SUPREME_PATTERN && !SupremePatternData.isEncoded(stack);
    }
}
