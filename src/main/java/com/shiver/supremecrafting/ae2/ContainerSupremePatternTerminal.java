package com.shiver.supremecrafting.ae2;

import com.shiver.supremecrafting.recipe.SupremeCraftingMatcher;
import com.shiver.supremecrafting.table.ContainerSupremeTable;
import com.shiver.supremecrafting.table.SupremeTableInventory;
import com.shiver.supremecrafting.table.ViewportSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerSupremePatternTerminal extends Container {
    public static final int BLANK_SLOT_X = 329;
    public static final int BLANK_SLOT_Y = 166;
    public static final int RESULT_SLOT_X = ContainerSupremeTable.RESULT_SLOT_X;
    public static final int RESULT_SLOT_Y = ContainerSupremeTable.RESULT_SLOT_Y;
    public static final int OUTPUT_SLOT_X = 329;
    public static final int OUTPUT_SLOT_Y = 212;
    public static final int BLANK_SLOT_INDEX = SupremeTableInventory.SIZE + 36;
    public static final int RESULT_SLOT_INDEX = SupremeTableInventory.SIZE + 37;
    public static final int OUTPUT_SLOT_INDEX = SupremeTableInventory.SIZE + 38;

    private final TileSupremePatternTerminal terminal;
    private final InventoryCraftResult result = new InventoryCraftResult();
    private long lastVersion = -1L;

    public ContainerSupremePatternTerminal(InventoryPlayer playerInv, TileSupremePatternTerminal terminal) {
        this.terminal = terminal;
        for (int i = 0; i < SupremeTableInventory.SIZE; i++) {
            addSlotToContainer(new ViewportSlot(terminal, i, -9999, -9999));
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInv, col + row * 9 + 9, 0, 0));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col, 0, 0));
        }
        addSlotToContainer(new SlotSupremePatternBlank(terminal, TileSupremePatternTerminal.BLANK_SLOT, BLANK_SLOT_X, BLANK_SLOT_Y));
        addSlotToContainer(new Slot(result, 0, RESULT_SLOT_X, RESULT_SLOT_Y) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }

            @Override
            public boolean canTakeStack(EntityPlayer playerIn) {
                return false;
            }
        });
        addSlotToContainer(new SlotSupremePatternOutput(terminal, TileSupremePatternTerminal.OUTPUT_SLOT, OUTPUT_SLOT_X, OUTPUT_SLOT_Y));
    }

    public TileSupremePatternTerminal terminal() {
        return terminal;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return terminal.isUsableByPlayer(playerIn);
    }

    @Override
    public void detectAndSendChanges() {
        refreshCraftingResult();
        super.detectAndSendChanges();
    }

    public void refreshCraftingResult() {
        if (lastVersion != terminal.modVersion()) {
            lastVersion = terminal.modVersion();
            result.setInventorySlotContents(0, SupremeCraftingMatcher.findResult(terminal.grid(), terminal.getWorld()));
        }
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
        if (slotId >= 0 && slotId < SupremeTableInventory.SIZE) {
            Slot slot = inventorySlots.get(slotId);
            ItemStack held = player.inventory.getItemStack();
            if (held.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                ItemStack ghost = held.copy();
                ghost.setCount(1);
                slot.putStack(ghost);
            }
            return held;
        }
        return super.slotClick(slotId, dragType, clickTypeIn, player);
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
            return ItemStack.EMPTY;
        } else if (index == OUTPUT_SLOT_INDEX) {
            if (!mergeItemStack(stack, gridEnd, playerEnd, true)) return ItemStack.EMPTY;
        } else if (index >= gridEnd && index < playerEnd) {
            if (!SupremePatternData.isEncoded(stack) && stack.getItem() == AE2Module.SUPREME_PATTERN) {
                if (!mergeItemStack(stack, BLANK_SLOT_INDEX, BLANK_SLOT_INDEX + 1, false)) return ItemStack.EMPTY;
            } else {
                return ItemStack.EMPTY;
            }
        } else if (index == BLANK_SLOT_INDEX) {
            if (!mergeItemStack(stack, gridEnd, playerEnd, true)) return ItemStack.EMPTY;
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
