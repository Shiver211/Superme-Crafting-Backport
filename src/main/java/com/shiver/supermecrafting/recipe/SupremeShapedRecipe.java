package com.shiver.supermecrafting.recipe;

import com.shiver.supermecrafting.table.SupremeTableInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

public class SupremeShapedRecipe extends SupremeRecipe {
    private final int width;
    private final int height;
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack result;

    public SupremeShapedRecipe(int width, int height, NonNullList<Ingredient> ingredients, ItemStack result) {
        super("supreme_shaped");
        this.width = width;
        this.height = height;
        this.ingredients = ingredients;
        this.result = result;
    }

    @Override
    public boolean matches(SupremeTableInventory inventory, World world) {
        SupremeCraftingMatcher.Bounds b = SupremeCraftingMatcher.bounds(inventory);
        if (b == null || b.width() != width || b.height() != height) {
            return false;
        }
        return matchesAt(inventory, b.minX, b.minY, false) || matchesAt(inventory, b.minX, b.minY, true);
    }

    private boolean matchesAt(SupremeTableInventory inventory, int startX, int startY, boolean mirror) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int ingredientX = mirror ? width - x - 1 : x;
                Ingredient ingredient = ingredients.get(ingredientX + y * width);
                ItemStack stack = inventory.get(SupremeTableInventory.indexOf(startX + x, startY + y));
                if (!ingredient.apply(stack)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public NonNullList<Ingredient> getSupremeIngredients() {
        return ingredients;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return result;
    }
}
