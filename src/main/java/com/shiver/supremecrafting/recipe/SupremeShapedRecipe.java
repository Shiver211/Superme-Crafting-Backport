package com.shiver.supremecrafting.recipe;

import com.shiver.supremecrafting.table.SupremeTableInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

public class SupremeShapedRecipe extends SupremeRecipe {
    private final int offsetX;
    private final int offsetY;
    private final int width;
    private final int height;
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack result;

    public SupremeShapedRecipe(int width, int height, NonNullList<Ingredient> ingredients, ItemStack result) {
        this(0, 0, width, height, ingredients, result);
    }

    public SupremeShapedRecipe(int offsetX, int offsetY, int width, int height,
                               NonNullList<Ingredient> ingredients, ItemStack result) {
        super("supreme_shaped");
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.width = width;
        this.height = height;
        this.ingredients = ingredients;
        this.result = result;
    }

    @Override
    public boolean matches(SupremeTableInventory inventory, World world) {
        if (offsetX < 0 || offsetY < 0
                || offsetX + width > SupremeTableInventory.WIDTH
                || offsetY + height > SupremeTableInventory.HEIGHT) {
            return false;
        }
        for (int y = 0; y < SupremeTableInventory.HEIGHT; y++) {
            for (int x = 0; x < SupremeTableInventory.WIDTH; x++) {
                Ingredient ingredient = ingredientAt(x, y);
                ItemStack stack = inventory.get(SupremeTableInventory.indexOf(x, y));
                if (ingredient == Ingredient.EMPTY) {
                    if (!stack.isEmpty()) {
                        return false;
                    }
                } else if (!ingredient.apply(stack)) {
                    return false;
                }
            }
        }
        return true;
    }

    private Ingredient ingredientAt(int tableX, int tableY) {
        int x = tableX - offsetX;
        int y = tableY - offsetY;
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return Ingredient.EMPTY;
        }
        return ingredients.get(x + y * width);
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
    public int getOffsetX() {
        return offsetX;
    }

    @Override
    public int getOffsetY() {
        return offsetY;
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
