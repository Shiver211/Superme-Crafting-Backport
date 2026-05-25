package com.shiver.supermecrafting.recipe;

import com.shiver.supermecrafting.table.SupremeTableInventory;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class SupremeCraftingMatcher {
    private SupremeCraftingMatcher() {
    }

    public static ItemStack findResult(SupremeTableInventory inventory, World world) {
        IRecipe recipe = findRecipe(inventory, world);
        return recipe == null ? ItemStack.EMPTY : recipe.getRecipeOutput().copy();
    }

    public static IRecipe findRecipe(SupremeTableInventory inventory, World world) {
        if (inventory.isEmpty()) {
            return null;
        }
        for (IRecipe recipe : ForgeRegistries.RECIPES) {
            if (recipe instanceof SupremeRecipe && ((SupremeRecipe) recipe).matches(inventory, world)) {
                return recipe;
            }
        }
        Bounds b = bounds(inventory);
        if (b != null && b.width() <= 3 && b.height() <= 3) {
            InventoryCrafting crafting = new InventoryCrafting(new Container() {
                @Override
                public boolean canInteractWith(net.minecraft.entity.player.EntityPlayer playerIn) {
                    return false;
                }
            }, b.width(), b.height());
            for (int y = 0; y < b.height(); y++) {
                for (int x = 0; x < b.width(); x++) {
                    crafting.setInventorySlotContents(x + y * b.width(),
                            inventory.get(SupremeTableInventory.indexOf(b.minX + x, b.minY + y)));
                }
            }
            return CraftingManager.findMatchingRecipe(crafting, world);
        }
        return null;
    }

    public static void consume(SupremeTableInventory inventory, World world) {
        if (findRecipe(inventory, world) == null) {
            return;
        }
        for (int i = 0; i < SupremeTableInventory.SIZE; i++) {
            ItemStack stack = inventory.get(i);
            if (!stack.isEmpty()) {
                ItemStack before = stack.copy();
                stack.shrink(1);
                if (stack.isEmpty() && before.getItem().hasContainerItem(before)) {
                    inventory.set(i, before.getItem().getContainerItem(before));
                }
            }
        }
    }

    public static Bounds bounds(SupremeTableInventory inventory) {
        int minX = SupremeTableInventory.WIDTH;
        int minY = SupremeTableInventory.HEIGHT;
        int maxX = -1;
        int maxY = -1;
        for (int i = 0; i < SupremeTableInventory.SIZE; i++) {
            if (!inventory.get(i).isEmpty()) {
                int x = SupremeTableInventory.xOf(i);
                int y = SupremeTableInventory.yOf(i);
                if (x < minX) minX = x;
                if (y < minY) minY = y;
                if (x > maxX) maxX = x;
                if (y > maxY) maxY = y;
            }
        }
        return maxX < 0 ? null : new Bounds(minX, minY, maxX, maxY);
    }

    public static final class Bounds {
        public final int minX;
        public final int minY;
        public final int maxX;
        public final int maxY;

        Bounds(int minX, int minY, int maxX, int maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }

        public int width() {
            return maxX - minX + 1;
        }

        public int height() {
            return maxY - minY + 1;
        }
    }
}
