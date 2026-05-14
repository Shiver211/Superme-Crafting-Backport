package com.shiver.supermecrafting.recipe;

import com.shiver.supermecrafting.table.SupremeTableInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import java.util.Optional;

public class SupremeCraftingMatcher {

    /**
     * Builds an InventoryCrafting from the Supreme Table inventory for recipe matching.
     * Creates a temporary container to hold the items.
     */
    public static InventoryCrafting buildCraftingInput(SupremeTableInventory inv) {
        // Create a temporary InventoryCrafting with a dummy container
        net.minecraft.inventory.Container dummyContainer = new net.minecraft.inventory.Container() {
            @Override
            public boolean canInteractWith(net.minecraft.entity.player.EntityPlayer player) {
                return true;
            }
        };
        InventoryCrafting crafting = new InventoryCrafting(dummyContainer, SupremeTableInventory.WIDTH, SupremeTableInventory.HEIGHT);
        for (int i = 0; i < SupremeTableInventory.SIZE; i++) {
            crafting.setInventorySlotContents(i, inv.get(i));
        }
        return crafting;
    }

    /**
     * Finds a matching recipe for the given inventory.
     * Checks Supreme Crafting recipes first, then falls back to vanilla 3x3.
     */
    public static Optional<IRecipe> findRecipe(SupremeTableInventory inv, World world) {
        InventoryCrafting crafting = buildCraftingInput(inv);

        // First try Supreme Crafting recipes
        for (IRecipe recipe : CraftingManager.REGISTRY) {
            if (recipe instanceof SupremeCraftingRecipe) {
                if (recipe.matches(crafting, world)) {
                    return Optional.of(recipe);
                }
            }
        }

        // Fallback: try vanilla 3x3 recipes if input bbox fits
        // Find the bounding box of non-empty cells
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

        if (maxX >= 0) {
            int width = maxX - minX + 1;
            int height = maxY - minY + 1;
            if (width <= 3 && height <= 3) {
                // Build a 3x3 crafting input from the bounding box
                InventoryCrafting smallCrafting = new InventoryCrafting(
                        new net.minecraft.inventory.Container() {
                            @Override
                            public boolean canInteractWith(net.minecraft.entity.player.EntityPlayer player) {
                                return true;
                            }
                        }, 3, 3);
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        smallCrafting.setInventorySlotContents(x + y * 3, inv.get(minX + x, minY + y));
                    }
                }

                for (IRecipe recipe : CraftingManager.REGISTRY) {
                    if (!(recipe instanceof SupremeCraftingRecipe) && recipe.matches(smallCrafting, world)) {
                        return Optional.of(recipe);
                    }
                }
            }
        }

        return Optional.empty();
    }
}
