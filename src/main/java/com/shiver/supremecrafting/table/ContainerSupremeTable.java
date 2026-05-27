package com.shiver.supremecrafting.table;

import com.shiver.supremecrafting.recipe.SupremeCraftingMatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerSupremeTable extends Container {
    public static final int SLOT_PX = 18;
    public static final int RESULT_SLOT_X = 329;
    public static final int RESULT_SLOT_Y = 118;
    public static final int RESULT_SLOT_INDEX = SupremeTableInventory.SIZE + 36;

    private final TileSupremeTable table;
    private final InventoryCraftResult result = new InventoryCraftResult();
    private long lastVersion = -1L;

    public ContainerSupremeTable(InventoryPlayer playerInv, TileSupremeTable table) {
        this.table = table;
        for (int i = 0; i < SupremeTableInventory.SIZE; i++) {
            addSlotToContainer(new ViewportSlot(table, i, -9999, -9999));
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInv, col + row * 9 + 9, 0, 0));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col, 0, 0));
        }
        addSlotToContainer(new SupremeResultSlot(playerInv.player, table, result, 0, RESULT_SLOT_X, RESULT_SLOT_Y));
    }

    public TileSupremeTable table() {
        return table;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return table.isUsableByPlayer(playerIn);
    }

    @Override
    public void detectAndSendChanges() {
        refreshCraftingResult();
        super.detectAndSendChanges();
    }

    public void refreshCraftingResult() {
        if (lastVersion != table.modVersion()) {
            lastVersion = table.modVersion();
            result.setInventorySlotContents(0, SupremeCraftingMatcher.findResult(table.supremeInventory(), table.getWorld()));
        }
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
        int gridEnd = SupremeTableInventory.SIZE;
        int playerEnd = gridEnd + 36;
        if (index == RESULT_SLOT_INDEX) {
            if (!mergeItemStack(stack, gridEnd, playerEnd, true)) return ItemStack.EMPTY;
            slot.onTake(playerIn, stack);
        } else if (index < gridEnd) {
            if (!mergeItemStack(stack, gridEnd, playerEnd, true)) return ItemStack.EMPTY;
        } else {
            if (!mergeItemStack(stack, 0, gridEnd, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }
        return copy;
    }
}
