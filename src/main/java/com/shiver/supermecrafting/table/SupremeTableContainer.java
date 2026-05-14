package com.shiver.supermecrafting.table;

import com.shiver.supermecrafting.recipe.SupremeCraftingMatcher;
import com.shiver.supermecrafting.recipe.SupremeResultSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Optional;

public class SupremeTableContainer extends Container {

    public static final int RESULT_SLOT_INDEX = SupremeTableInventory.SIZE + 36;

    private final SupremeTableTileEntity tileEntity;
    private final InventoryPlayer playerInv;
    private final BlockPos tablePos;
    private final IInventory resultContainer = new net.minecraft.inventory.InventoryCraftResult();
    private long lastResultModVersion = -1L;

    public SupremeTableContainer(InventoryPlayer playerInv, SupremeTableTileEntity te) {
        this.tileEntity = te;
        this.playerInv = playerInv;
        this.tablePos = te.getPos();

        // Grid slots (81x81 = 6561)
        for (int i = 0; i < SupremeTableInventory.SIZE; i++) {
            this.addSlotToContainer(new ViewportSlot(te, i, -9999, -9999));
        }

        // Player inventory (3 rows of 9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlotToContainer(new Slot(playerInv, col + row * 9 + 9, 0, 0));
            }
        }
        // Hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlotToContainer(new Slot(playerInv, col, 0, 0));
        }

        // Result slot
        this.addSlotToContainer(new SupremeResultSlot(playerInv.player, te, resultContainer, 0, 329, 118));
    }

    public BlockPos getTablePos() {
        return tablePos;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tileEntity.getWorld().getTileEntity(tablePos) == tileEntity
                && player.getDistanceSq(tablePos) < 64;
    }

    @Override
    public void detectAndSendChanges() {
        World world = tileEntity.getWorld();
        if (!world.isRemote) {
            long current = tileEntity.getModVersion();
            if (current != lastResultModVersion) {
                lastResultModVersion = current;
                recomputeResult(world);
            }
        }
        super.detectAndSendChanges();
    }

    private void recomputeResult(World world) {
        Optional<IRecipe> match = SupremeCraftingMatcher.findRecipe(tileEntity.getInventory(), world);
        ItemStack result = match.map(r -> r.getCraftingResult(SupremeCraftingMatcher.buildCraftingInput(tileEntity.getInventory())))
                .orElse(ItemStack.EMPTY);
        resultContainer.setInventorySlotContents(0, result);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        if (index < 0 || index >= inventorySlots.size()) return ItemStack.EMPTY;
        Slot source = inventorySlots.get(index);
        if (!source.getHasStack()) return ItemStack.EMPTY;

        ItemStack sourceStack = source.getStack();
        ItemStack result = sourceStack.copy();
        int gridEnd = SupremeTableInventory.SIZE;
        int playerInvEnd = gridEnd + 36;

        if (index == RESULT_SLOT_INDEX) {
            // Result → player inv
            if (!mergeItemStack(sourceStack, gridEnd, playerInvEnd, true)) return ItemStack.EMPTY;
            source.onSlotChange(sourceStack, result);
        } else if (index < gridEnd) {
            // Grid → player inv
            if (!mergeItemStack(sourceStack, gridEnd, playerInvEnd, true)) return ItemStack.EMPTY;
        } else {
            // Player inv → grid
            if (!mergeItemStack(sourceStack, 0, gridEnd, false)) return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) {
            source.putStack(ItemStack.EMPTY);
        } else {
            source.onSlotChanged();
        }
        return result;
    }
}
