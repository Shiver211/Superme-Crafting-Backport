package com.shiver.supermecrafting.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * Supreme Crafting shaped recipe for the 81x81 grid.
 * Extends IForgeRegistryEntry.Impl for registry compatibility.
 */
public class SupremeShapedRecipe extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements SupremeCraftingRecipe {

    private final SupremeShapedPattern pattern;
    private final ItemStack result;
    private final String group;

    public SupremeShapedRecipe(String group, SupremeShapedPattern pattern, ItemStack result) {
        this.group = group;
        this.pattern = pattern;
        this.result = result;
    }

    public SupremeShapedPattern getPattern() {
        return pattern;
    }

    @Override
    public int getPatternWidth() {
        return pattern.getWidth();
    }

    @Override
    public int getPatternHeight() {
        return pattern.getHeight();
    }

    @Override
    public Ingredient getIngredientAt(int x, int y) {
        return pattern.getIngredientAt(x, y);
    }

    @Override
    public boolean matches(@Nonnull InventoryCrafting inv, @Nonnull World world) {
        return pattern.matches(inv);
    }

    @Nonnull
    @Override
    public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
        return result.copy();
    }

    @Nonnull
    @Override
    public ItemStack getRecipeOutput() {
        return result.copy();
    }

    @Override
    public boolean canFit(int width, int height) {
        return width >= pattern.getWidth() && height >= pattern.getHeight();
    }

    @Nonnull
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return pattern.getIngredients();
    }

    @Override
    public String getGroup() {
        return group;
    }
}
