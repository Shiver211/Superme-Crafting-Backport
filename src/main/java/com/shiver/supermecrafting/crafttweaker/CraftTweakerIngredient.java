package com.shiver.supermecrafting.crafttweaker;

import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

public class CraftTweakerIngredient extends Ingredient {
    private final IIngredient ingredient;

    private CraftTweakerIngredient(IIngredient ingredient) {
        super(0);
        this.ingredient = ingredient;
    }

    public static Ingredient of(IIngredient ingredient) {
        return ingredient == null ? Ingredient.EMPTY : new CraftTweakerIngredient(ingredient);
    }

    @Override
    public boolean apply(ItemStack stack) {
        for (IItemStack candidate : ingredient.getItems()) {
            Object internal = candidate.getInternal();
            if (internal instanceof ItemStack) {
                ItemStack itemStack = (ItemStack) internal;
                if (ItemStack.areItemsEqual(itemStack, stack)
                        && (itemStack.getMetadata() == 32767 || itemStack.getMetadata() == stack.getMetadata())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ItemStack[] getMatchingStacks() {
        java.util.List<IItemStack> items = ingredient.getItems();
        ItemStack[] stacks = new ItemStack[items.size()];
        for (int i = 0; i < items.size(); i++) {
            Object internal = items.get(i).getInternal();
            stacks[i] = internal instanceof ItemStack ? (ItemStack) internal : ItemStack.EMPTY;
        }
        return stacks;
    }
}
