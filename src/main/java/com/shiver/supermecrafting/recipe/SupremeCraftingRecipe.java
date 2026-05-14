package com.shiver.supermecrafting.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Marker interface for Supreme Crafting recipes.
 * Extends IRecipe for compatibility with the 1.12.2 recipe system.
 */
public interface SupremeCraftingRecipe extends IRecipe {

    /**
     * Returns the pattern width, or -1 for shapeless recipes.
     */
    int getPatternWidth();

    /**
     * Returns the pattern height, or -1 for shapeless recipes.
     */
    int getPatternHeight();

    /**
     * Returns the ingredient at the given position, or Ingredient.EMPTY if empty.
     */
    Ingredient getIngredientAt(int x, int y);
}
