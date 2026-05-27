package com.shiver.supremecrafting.recipe;

import com.shiver.supremecrafting.table.SupremeTableInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class SupremeShapelessRecipe extends SupremeRecipe {
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack result;

    public SupremeShapelessRecipe(NonNullList<Ingredient> ingredients, ItemStack result) {
        super("supreme_shapeless");
        this.ingredients = ingredients;
        this.result = result;
    }

    @Override
    public boolean matches(SupremeTableInventory inventory, World world) {
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemStack stack : inventory.items()) {
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        if (stacks.size() != ingredients.size()) {
            return false;
        }
        boolean[] used = new boolean[stacks.size()];
        for (Ingredient ingredient : ingredients) {
            boolean found = false;
            for (int i = 0; i < stacks.size(); i++) {
                if (!used[i] && ingredient.apply(stacks.get(i))) {
                    used[i] = true;
                    found = true;
                    break;
                }
            }
            if (!found) return false;
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
        return (int) Math.ceil(Math.sqrt(ingredients.size()));
    }

    @Override
    public int getHeight() {
        return (int) Math.ceil((double) ingredients.size() / getWidth());
    }

    @Override
    public ItemStack getRecipeOutput() {
        return result;
    }
}
