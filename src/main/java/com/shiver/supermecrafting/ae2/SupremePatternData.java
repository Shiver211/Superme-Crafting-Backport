package com.shiver.supermecrafting.ae2;

import com.shiver.supermecrafting.recipe.SupremeCraftingMatcher;
import com.shiver.supermecrafting.recipe.SupremeRecipe;
import com.shiver.supermecrafting.table.SupremeTableInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;

public final class SupremePatternData {
    public static final String TAG_ENCODED = "SupremePattern";
    private static final String TAG_RECIPE_ID = "RecipeId";
    private static final String TAG_RECIPE_NAME = "RecipeName";
    private static final String TAG_INPUTS = "Inputs";
    private static final String TAG_OUTPUT = "Output";
    private static final String TAG_EXTRA_OUTPUTS = "ExtraOutputs";

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
        ItemStack output = craftingOutput(recipe, inventory);
        if (output.isEmpty()) {
            return ItemStack.EMPTY;
        }
        data.setString(TAG_RECIPE_ID, id == null ? "" : id.toString());
        data.setString(TAG_RECIPE_NAME, id == null ? output.getDisplayName() : id.toString());
        data.setTag(TAG_INPUTS, writeInputs(compressInputs(inventory)));
        data.setTag(TAG_OUTPUT, output.writeToNBT(new NBTTagCompound()));
        data.setTag(TAG_EXTRA_OUTPUTS, writeInputs(extraOutputs(recipe, inventory)));
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

    public static List<ItemStack> readOutputs(ItemStack pattern) {
        List<ItemStack> outputs = new ArrayList<>();
        ItemStack output = readOutput(pattern);
        if (!output.isEmpty()) {
            outputs.add(output);
        }
        if (!isEncoded(pattern)) {
            return outputs;
        }
        NBTTagList list = pattern.getTagCompound().getCompoundTag(TAG_ENCODED).getTagList(TAG_EXTRA_OUTPUTS, 10);
        for (int i = 0; i < list.tagCount(); i++) {
            ItemStack stack = new ItemStack(list.getCompoundTagAt(i));
            if (!stack.isEmpty()) {
                outputs.add(stack);
            }
        }
        return outputs;
    }

    public static String readRecipeName(ItemStack pattern) {
        if (!isEncoded(pattern)) {
            return "";
        }
        return pattern.getTagCompound().getCompoundTag(TAG_ENCODED).getString(TAG_RECIPE_NAME);
    }

    public static boolean isRecipeValid(ItemStack pattern, net.minecraft.world.World world) {
        if (!isEncoded(pattern)) {
            return false;
        }
        NBTTagCompound data = pattern.getTagCompound().getCompoundTag(TAG_ENCODED);
        String recipeId = data.getString(TAG_RECIPE_ID);
        if (recipeId.isEmpty()) {
            return false;
        }
        IRecipe recipe;
        try {
            recipe = ForgeRegistries.RECIPES.getValue(new ResourceLocation(recipeId));
        } catch (RuntimeException e) {
            return false;
        }
        return recipe != null && sameOutput(readOutput(pattern), recipe.getRecipeOutput()) && sameInputs(readInputs(pattern), recipe);
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

    private static ItemStack craftingOutput(IRecipe recipe, SupremeTableInventory inventory) {
        if (recipe instanceof SupremeRecipe) {
            return recipe.getRecipeOutput().copy();
        }
        InventoryCrafting crafting = SupremeCraftingMatcher.craftingInventory(inventory);
        return crafting == null ? recipe.getRecipeOutput().copy() : recipe.getCraftingResult(crafting);
    }

    private static boolean sameOutput(ItemStack stored, ItemStack current) {
        return !stored.isEmpty()
                && !current.isEmpty()
                && stored.getCount() == current.getCount()
                && ItemHandlerHelper.canItemStacksStack(stored, current);
    }

    private static boolean sameInputs(List<ItemStack> storedInputs, IRecipe recipe) {
        List<Ingredient> ingredients = recipeIngredients(recipe);
        int storedCount = 0;
        for (ItemStack stack : storedInputs) {
            storedCount += stack.getCount();
        }
        if (storedCount != ingredients.size()) {
            return false;
        }

        boolean[] used = new boolean[ingredients.size()];
        for (ItemStack stored : storedInputs) {
            int matched = 0;
            for (int i = 0; i < ingredients.size() && matched < stored.getCount(); i++) {
                if (!used[i] && ingredients.get(i).apply(stored)) {
                    used[i] = true;
                    matched++;
                }
            }
            if (matched != stored.getCount()) {
                return false;
            }
        }
        return true;
    }

    private static List<Ingredient> recipeIngredients(IRecipe recipe) {
        NonNullList<Ingredient> source = recipe instanceof SupremeRecipe
                ? ((SupremeRecipe) recipe).getSupremeIngredients()
                : recipe.getIngredients();
        List<Ingredient> ingredients = new ArrayList<>();
        for (Ingredient ingredient : source) {
            if (ingredient != null && ingredient != Ingredient.EMPTY) {
                ingredients.add(ingredient);
            }
        }
        return ingredients;
    }

    private static List<ItemStack> extraOutputs(IRecipe recipe, SupremeTableInventory inventory) {
        List<ItemStack> outputs = new ArrayList<>();
        if (recipe instanceof SupremeRecipe) {
            return outputs;
        }
        InventoryCrafting crafting = SupremeCraftingMatcher.craftingInventory(inventory);
        if (crafting == null) {
            return outputs;
        }
        NonNullList<ItemStack> remaining = recipe.getRemainingItems(crafting);
        for (ItemStack stack : remaining) {
            if (!stack.isEmpty()) {
                addOrGrow(outputs, stack);
            }
        }
        return outputs;
    }

    private static ItemStack findStack(List<ItemStack> inputs, ItemStack stack) {
        for (ItemStack existing : inputs) {
            if (ItemHandlerHelper.canItemStacksStack(existing, stack)) {
                return existing;
            }
        }
        return null;
    }

    private static void addOrGrow(List<ItemStack> stacks, ItemStack stack) {
        ItemStack copy = stack.copy();
        ItemStack existing = findStack(stacks, copy);
        if (existing == null) {
            stacks.add(copy);
        } else {
            existing.grow(copy.getCount());
        }
    }

    private static NBTTagList writeInputs(List<ItemStack> inputs) {
        NBTTagList list = new NBTTagList();
        for (ItemStack stack : inputs) {
            list.appendTag(stack.writeToNBT(new NBTTagCompound()));
        }
        return list;
    }
}
