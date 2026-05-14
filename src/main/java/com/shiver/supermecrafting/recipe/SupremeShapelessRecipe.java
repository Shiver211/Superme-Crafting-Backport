package com.shiver.supermecrafting.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Supreme Crafting shapeless recipe for the 81x81 grid.
 * Matches regardless of ingredient placement, up to 6561 ingredients.
 */
public class SupremeShapelessRecipe extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements SupremeCraftingRecipe {

    private final NonNullList<Ingredient> ingredients;
    private final ItemStack result;
    private final String group;

    public SupremeShapelessRecipe(String group, NonNullList<Ingredient> ingredients, ItemStack result) {
        this.group = group;
        this.ingredients = ingredients;
        this.result = result;
    }

    @Override
    public int getPatternWidth() {
        return -1;
    }

    @Override
    public int getPatternHeight() {
        return -1;
    }

    @Override
    public Ingredient getIngredientAt(int x, int y) {
        return Ingredient.EMPTY;
    }

    @Override
    public boolean matches(@Nonnull InventoryCrafting inv, @Nonnull World world) {
        List<ItemStack> inputItems = new ArrayList<>();
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!stack.isEmpty()) {
                inputItems.add(stack);
            }
        }

        if (inputItems.size() != ingredients.size()) return false;

        boolean[] matched = new boolean[inputItems.size()];
        for (Ingredient ingredient : ingredients) {
            boolean found = false;
            for (int i = 0; i < inputItems.size(); i++) {
                if (!matched[i] && ingredient.apply(inputItems.get(i))) {
                    matched[i] = true;
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
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
        return width * height >= ingredients.size();
    }

    @Nonnull
    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public String getGroup() {
        return group;
    }
}
