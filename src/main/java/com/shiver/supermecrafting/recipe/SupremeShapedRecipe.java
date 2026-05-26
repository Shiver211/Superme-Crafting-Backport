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
        SupremeCraftingMatcher.Bounds inventoryBounds = SupremeCraftingMatcher.bounds(inventory);
        IngredientBounds recipeBounds = ingredientBounds();
        if (inventoryBounds == null || recipeBounds == null
                || inventoryBounds.width() != recipeBounds.width()
                || inventoryBounds.height() != recipeBounds.height()) {
            return false;
        }
        return matchesAligned(inventory, inventoryBounds, recipeBounds, false)
                || matchesAligned(inventory, inventoryBounds, recipeBounds, true);
    }

    private boolean matchesAligned(SupremeTableInventory inventory, SupremeCraftingMatcher.Bounds inventoryBounds,
                                   IngredientBounds recipeBounds, boolean mirror) {
        int minX = mirror ? width - recipeBounds.maxX - 1 : recipeBounds.minX;
        int startX = inventoryBounds.minX - minX;
        int startY = inventoryBounds.minY - recipeBounds.minY;
        return startX >= 0 && startY >= 0
                && startX + width <= SupremeTableInventory.WIDTH
                && startY + height <= SupremeTableInventory.HEIGHT
                && matchesAt(inventory, startX, startY, mirror);
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

    private IngredientBounds ingredientBounds() {
        int minX = width;
        int minY = height;
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (ingredients.get(x + y * width) != Ingredient.EMPTY) {
                    if (x < minX) minX = x;
                    if (y < minY) minY = y;
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                }
            }
        }
        return maxX < 0 ? null : new IngredientBounds(minX, minY, maxX, maxY);
    }

    private static final class IngredientBounds {
        private final int minX;
        private final int minY;
        private final int maxX;
        private final int maxY;

        private IngredientBounds(int minX, int minY, int maxX, int maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }

        private int width() {
            return maxX - minX + 1;
        }

        private int height() {
            return maxY - minY + 1;
        }
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
