package com.shiver.supermecrafting.jei;

import com.shiver.supermecrafting.recipe.SupremeRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SupremeRecipeWrapper implements IRecipeWrapper {
    private final SupremeRecipe recipe;

    public SupremeRecipeWrapper(SupremeRecipe recipe) {
        this.recipe = recipe;
    }

    public SupremeRecipe recipe() {
        return recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<List<ItemStack>> inputs = new ArrayList<>();
        for (Ingredient ingredient : recipe.getSupremeIngredients()) {
            inputs.add(Arrays.asList(ingredient.getMatchingStacks()));
        }
        ingredients.setInputLists(ItemStack.class, inputs);
        ingredients.setOutput(ItemStack.class, recipe.getRecipeOutput());
    }
}
