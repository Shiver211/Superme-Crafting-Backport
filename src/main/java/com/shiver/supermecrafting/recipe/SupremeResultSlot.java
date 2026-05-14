package com.shiver.supermecrafting.recipe;

import com.shiver.supermecrafting.table.SupremeTableInventory;
import com.shiver.supermecrafting.table.SupremeTableTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;

import java.util.Optional;

public class SupremeResultSlot extends Slot {

    private final EntityPlayer player;
    private final SupremeTableTileEntity table;
    private final IInventory resultContainer;
    private int removeCount;

    public SupremeResultSlot(EntityPlayer player, SupremeTableTileEntity table,
                             IInventory resultContainer, int slotIndex, int x, int y) {
        super(resultContainer, slotIndex, x, y);
        this.player = player;
        this.table = table;
        this.resultContainer = resultContainer;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack onTake(EntityPlayer player, ItemStack craftedStack) {
        if (player.world.isRemote) return craftedStack;

        checkTakeAchievements(craftedStack);

        SupremeTableInventory inv = table.getInventory();

        // Find the recipe that matched
        Optional<IRecipe> match = SupremeCraftingMatcher.findRecipe(inv, player.world);
        if (!match.isPresent()) return craftedStack;

        IRecipe recipe = match.get();

        // Find bounding box of non-empty cells
        int minX = SupremeTableInventory.WIDTH, minY = SupremeTableInventory.HEIGHT;
        int maxX = -1, maxY = -1;
        for (int y = 0; y < SupremeTableInventory.HEIGHT; y++) {
            for (int x = 0; x < SupremeTableInventory.WIDTH; x++) {
                if (!inv.get(x, y).isEmpty()) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        if (maxX < 0) return craftedStack;

        int width = maxX - minX + 1;
        int height = maxY - minY + 1;

        // 仅使用边界框构建合成输入（与原版使用定位输入的行为一致）
        net.minecraft.inventory.Container dummyContainer = new net.minecraft.inventory.Container() {
            @Override
            public boolean canInteractWith(net.minecraft.entity.player.EntityPlayer p) { return true; }
        };
        net.minecraft.inventory.InventoryCrafting bboxInput =
                new net.minecraft.inventory.InventoryCrafting(dummyContainer, width, height);
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                bboxInput.setInventorySlotContents(col + row * width, inv.get(minX + col, minY + row));
            }
        }
        NonNullList<ItemStack> remaining = recipe.getRemainingItems(bboxInput);

        // Consume ingredients and deposit remainders
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int gridIdx = SupremeTableInventory.indexOf(minX + col, minY + row);
                int remainIdx = col + row * width;

                ItemStack cell = inv.get(gridIdx);
                if (!cell.isEmpty()) {
                    cell.shrink(1);
                    if (cell.isEmpty()) {
                        inv.set(gridIdx, ItemStack.EMPTY);
                    }
                }

                if (remainIdx < remaining.size()) {
                    ItemStack remainder = remaining.get(remainIdx);
                    if (!remainder.isEmpty()) {
                        ItemStack cellAfter = inv.get(gridIdx);
                        if (cellAfter.isEmpty()) {
                            inv.set(gridIdx, remainder.copy());
                        } else if (cellAfter.isItemEqual(remainder) && ItemStack.areItemStackTagsEqual(cellAfter, remainder)) {
                            cellAfter.grow(remainder.getCount());
                        } else if (!player.inventory.addItemStackToInventory(remainder.copy())) {
                            player.dropItem(remainder.copy(), false);
                        }
                    }
                }
            }
        }

        table.markDirty();
        return craftedStack;
    }

    @Override
    public ItemStack decrStackSize(int amount) {
        if (this.getHasStack()) {
            this.removeCount += Math.min(amount, this.getStack().getCount());
        }
        return super.decrStackSize(amount);
    }

    protected void checkTakeAchievements(ItemStack stack) {
        if (removeCount > 0) {
            stack.onCrafting(player.world, player, removeCount);
        }
        removeCount = 0;
    }
}
