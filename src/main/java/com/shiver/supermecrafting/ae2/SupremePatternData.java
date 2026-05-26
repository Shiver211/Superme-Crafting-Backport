package com.shiver.supermecrafting.ae2;

import com.shiver.supermecrafting.recipe.SupremeCraftingMatcher;
import com.shiver.supermecrafting.table.SupremeTableInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public final class SupremePatternData {
    public static final String TAG_ENCODED = "SupremePattern";
    private static final String TAG_RECIPE_ID = "RecipeId";
    private static final String TAG_RECIPE_NAME = "RecipeName";
    private static final String TAG_INPUTS = "Inputs";
    private static final String TAG_OUTPUT = "Output";

    private SupremePatternData() {
    }

    public static boolean isEncoded(ItemStack stack) {
        return !stack.isEmpty() && stack.hasTagCompound() && stack.getTagCompound().hasKey(TAG_ENCODED, 10);
    }

    public static ItemStack encode(ItemStack pattern, SupremeTableInventory inventory, IRecipe recipe) {
        ItemStack out = pattern.copy();
        out.setCount(1);
        NBTTagCompound root = new NBTTagCompound();
        NBTTagCompound data = new NBTTagCompound();
        ResourceLocation id = recipe.getRegistryName();
        data.setString(TAG_RECIPE_ID, id == null ? "" : id.toString());
        data.setString(TAG_RECIPE_NAME, id == null ? recipe.getRecipeOutput().getDisplayName() : id.toString());
        data.setTag(TAG_INPUTS, writeInputs(compressInputs(inventory)));
        data.setTag(TAG_OUTPUT, recipe.getRecipeOutput().writeToNBT(new NBTTagCompound()));
        root.setTag(TAG_ENCODED, data);
        out.setTagCompound(root);
        return out;
    }

    public static List<ItemStack> readInputs(ItemStack pattern) {
        List<ItemStack> inputs = new ArrayList<>();
        if (!isEncoded(pattern)) {
            return inputs;
        }
        NBTTagList list = pattern.getTagCompound().getCompoundTag(TAG_ENCODED).getTagList(TAG_INPUTS, 10);
        for (int i = 0; i < list.tagCount(); i++) {
            ItemStack stack = new ItemStack(list.getCompoundTagAt(i));
            if (!stack.isEmpty()) {
                inputs.add(stack);
            }
        }
        return inputs;
    }

    public static ItemStack readOutput(ItemStack pattern) {
        if (!isEncoded(pattern)) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(pattern.getTagCompound().getCompoundTag(TAG_ENCODED).getCompoundTag(TAG_OUTPUT));
    }

    public static String readRecipeName(ItemStack pattern) {
        if (!isEncoded(pattern)) {
            return "";
        }
        return pattern.getTagCompound().getCompoundTag(TAG_ENCODED).getString(TAG_RECIPE_NAME);
    }

    public static ItemStack encodeCurrent(ItemStack pattern, SupremeTableInventory inventory, net.minecraft.world.World world) {
        IRecipe recipe = SupremeCraftingMatcher.findRecipe(inventory, world);
        return recipe == null ? ItemStack.EMPTY : encode(pattern, inventory, recipe);
    }

    private static List<ItemStack> compressInputs(SupremeTableInventory inventory) {
        List<ItemStack> inputs = new ArrayList<>();
        for (ItemStack stack : inventory.items()) {
            if (stack.isEmpty()) {
                continue;
            }
            ItemStack one = stack.copy();
            one.setCount(1);
            ItemStack existing = findStack(inputs, one);
            if (existing == null) {
                inputs.add(one);
            } else {
                existing.grow(1);
            }
        }
        return inputs;
    }

    private static ItemStack findStack(List<ItemStack> inputs, ItemStack stack) {
        for (ItemStack existing : inputs) {
            if (ItemHandlerHelper.canItemStacksStack(existing, stack)) {
                return existing;
            }
        }
        return null;
    }

    private static NBTTagList writeInputs(List<ItemStack> inputs) {
        NBTTagList list = new NBTTagList();
        for (ItemStack stack : inputs) {
            list.appendTag(stack.writeToNBT(new NBTTagCompound()));
        }
        return list;
    }
}
