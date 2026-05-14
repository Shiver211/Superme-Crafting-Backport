package com.shiver.supermecrafting.recipe;

import com.shiver.supermecrafting.table.SupremeTableInventory;
import com.shiver.supermecrafting.table.SupremeTableTileEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;

/**
 * Server-side recipe transfer logic for the Supreme Table.
 * Implements partial-fill: places what the player has, leaves missing cells empty.
 */
public class SupremeRecipeTransfer {

    public static void executeTransfer(SupremeTableTileEntity table, EntityPlayerMP player, IRecipe recipe) {
        SupremeTableInventory inv = table.getInventory();

        // Get recipe ingredients
        NonNullList<Ingredient> ingredients;
        int width, height;

        if (recipe instanceof SupremeCraftingRecipe) {
            SupremeCraftingRecipe scRecipe = (SupremeCraftingRecipe) recipe;
            width = scRecipe.getPatternWidth();
            height = scRecipe.getPatternHeight();
            ingredients = recipe.getIngredients();
        } else {
            // Vanilla recipe - assume 3x3
            width = 3;
            height = 3;
            ingredients = recipe.getIngredients();
        }

        // Calculate offset to center the recipe in the grid
        int offsetX = (SupremeTableInventory.WIDTH - width) / 2;
        int offsetY = (SupremeTableInventory.HEIGHT - height) / 2;

        // First pass: clear items outside the recipe area
        for (int y = 0; y < SupremeTableInventory.HEIGHT; y++) {
            for (int x = 0; x < SupremeTableInventory.WIDTH; x++) {
                int gridIdx = SupremeTableInventory.indexOf(x, y);
                ItemStack cell = inv.get(gridIdx);
                if (cell.isEmpty()) continue;

                boolean inRecipeArea = x >= offsetX && x < offsetX + width
                        && y >= offsetY && y < offsetY + height;

                if (!inRecipeArea) {
                    // Return to player inventory
                    if (!player.inventory.addItemStackToInventory(cell.copy())) {
                        player.dropItem(cell.copy(), false);
                    }
                    inv.set(gridIdx, ItemStack.EMPTY);
                }
            }
        }

        // Second pass: place ingredients from player inventory
        for (int i = 0; i < ingredients.size(); i++) {
            int ix = i % width;
            int iy = i / width;
            if (iy >= height) break;

            int gridX = offsetX + ix;
            int gridY = offsetY + iy;
            int gridIdx = SupremeTableInventory.indexOf(gridX, gridY);

            Ingredient ingredient = ingredients.get(i);
            if (ingredient == Ingredient.EMPTY) continue;

            ItemStack current = inv.get(gridIdx);
            if (!current.isEmpty() && ingredient.test(current)) {
                // Already has the right item, leave it
                continue;
            }

            // Return wrong item to player
            if (!current.isEmpty()) {
                if (!player.inventory.addItemStackToInventory(current.copy())) {
                    player.dropItem(current.copy(), false);
                }
                inv.set(gridIdx, ItemStack.EMPTY);
            }

            // Find matching item in player inventory
            ItemStack[] matchingStacks = ingredient.getMatchingStacks();
            boolean found = false;
            for (ItemStack match : matchingStacks) {
                if (found) break;
                for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++) {
                    ItemStack playerStack = player.inventory.getStackInSlot(slot);
                    if (!playerStack.isEmpty() && playerStack.isItemEqual(match)
                            && ItemStack.areItemStackTagsEqual(playerStack, match)) {
                        // Take one from player inventory
                        ItemStack taken = playerStack.splitStack(1);
                        if (playerStack.isEmpty()) {
                            player.inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
                        }
                        inv.set(gridIdx, taken);
                        found = true;
                        break;
                    }
                }
            }
        }

        table.markDirty();
    }
}
