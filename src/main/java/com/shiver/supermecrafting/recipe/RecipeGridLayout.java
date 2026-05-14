package com.shiver.supermecrafting.recipe;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

/**
 * 2D grid layout for recipe viewers (JEI).
 * Describes how a recipe should be rendered in a grid display.
 */
public class RecipeGridLayout {

    private final int width;
    private final int height;
    private final NonNullList<Ingredient> ingredients;

    public RecipeGridLayout(int width, int height, NonNullList<Ingredient> ingredients) {
        this.width = width;
        this.height = height;
        this.ingredients = ingredients;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public NonNullList<Ingredient> getIngredients() { return ingredients; }

    public Ingredient getIngredientAt(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return Ingredient.EMPTY;
        return ingredients.get(x + y * width);
    }

    /**
     * Create a RecipeGridLayout from any IRecipe (Supreme or vanilla).
     */
    public static RecipeGridLayout fromRecipe(IRecipe recipe) {
        if (recipe instanceof SupremeShapedRecipe) {
            return fromSupremeShaped((SupremeShapedRecipe) recipe);
        } else if (recipe instanceof SupremeShapelessRecipe) {
            return fromSupremeShapeless((SupremeShapelessRecipe) recipe);
        } else if (recipe instanceof ShapedRecipes || recipe instanceof ShapedOreRecipe) {
            return fromVanillaShaped(recipe);
        } else {
            return fromVanillaShapeless(recipe);
        }
    }

    private static RecipeGridLayout fromSupremeShaped(SupremeShapedRecipe recipe) {
        SupremeShapedPattern pattern = recipe.getPattern();
        return new RecipeGridLayout(pattern.getWidth(), pattern.getHeight(), pattern.getIngredients());
    }

    private static RecipeGridLayout fromSupremeShapeless(SupremeShapelessRecipe recipe) {
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        int count = ingredients.size();
        // Arrange in a square-ish grid
        int side = (int) Math.ceil(Math.sqrt(count));
        int width = Math.min(side, 9);
        int height = (count + width - 1) / width;
        return new RecipeGridLayout(width, height, ingredients);
    }

    private static RecipeGridLayout fromVanillaShaped(IRecipe recipe) {
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        int width;
        int height;
        if (recipe instanceof ShapedRecipes) {
            ShapedRecipes shaped = (ShapedRecipes) recipe;
            width = shaped.getRecipeWidth();
            height = shaped.getRecipeHeight();
        } else if (recipe instanceof ShapedOreRecipe) {
            ShapedOreRecipe oreRecipe = (ShapedOreRecipe) recipe;
            width = oreRecipe.getRecipeWidth();
            height = oreRecipe.getRecipeHeight();
        } else {
            // 回退：猜测尺寸
            int size = ingredients.size();
            width = 3;
            height = 3;
            if (size <= 4) { width = 2; height = 2; }
            if (size <= 1) { width = 1; height = 1; }
        }
        return new RecipeGridLayout(width, height, ingredients);
    }

    private static RecipeGridLayout fromVanillaShapeless(IRecipe recipe) {
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        int count = ingredients.size();
        int side = (int) Math.ceil(Math.sqrt(count));
        int width = Math.min(side, 3);
        int height = Math.min((count + width - 1) / width, 3);
        return new RecipeGridLayout(width, height, ingredients);
    }
}
