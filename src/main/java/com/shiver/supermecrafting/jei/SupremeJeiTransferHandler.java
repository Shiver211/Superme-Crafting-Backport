package com.shiver.supermecrafting.jei;

import com.shiver.supermecrafting.net.PacketTransferRecipe;
import com.shiver.supermecrafting.net.SCNetwork;
import com.shiver.supermecrafting.recipe.SupremeRecipe;
import com.shiver.supermecrafting.table.ContainerSupremeTable;
import com.shiver.supermecrafting.table.SupremeTableInventory;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SupremeJeiTransferHandler implements IRecipeTransferHandler<ContainerSupremeTable> {
    @Override
    public Class<ContainerSupremeTable> getContainerClass() {
        return ContainerSupremeTable.class;
    }

    @Override
    @Nullable
    public IRecipeTransferError transferRecipe(ContainerSupremeTable container, mezz.jei.api.gui.IRecipeLayout recipeLayout,
                                               EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        if (doTransfer) {
            ResourceLocation recipeId = findRecipeId(recipeLayout);
            if (recipeId != null) {
                SCNetwork.CHANNEL.sendToServer(new PacketTransferRecipe(recipeId, maxTransfer));
            }
        }
        return null;
    }

    @Nullable
    private ResourceLocation findRecipeId(mezz.jei.api.gui.IRecipeLayout recipeLayout) {
        Map<Integer, List<ItemStack>> layoutInputs = new HashMap<>();
        ItemStack layoutOutput = ItemStack.EMPTY;
        for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry
                : recipeLayout.getItemStacks().getGuiIngredients().entrySet()) {
            IGuiIngredient<ItemStack> ingredient = entry.getValue();
            if (ingredient.isInput()) {
                if (entry.getKey() >= 0 && entry.getKey() < SupremeTableInventory.SIZE) {
                    List<ItemStack> candidates = ingredient.getAllIngredients();
                    if (!candidates.isEmpty()) {
                        layoutInputs.put(entry.getKey(), candidates);
                    }
                }
            } else if (entry.getKey() == SupremeTableInventory.SIZE) {
                ItemStack displayed = ingredient.getDisplayedIngredient();
                if (displayed != null) {
                    layoutOutput = displayed;
                }
            }
        }
        for (IRecipe recipe : ForgeRegistries.RECIPES) {
            if (!(recipe instanceof SupremeRecipe)) {
                continue;
            }
            SupremeRecipe supremeRecipe = (SupremeRecipe) recipe;
            if (!ItemStack.areItemStacksEqual(layoutOutput, supremeRecipe.getRecipeOutput())) {
                continue;
            }
            if (sameInputs(layoutInputs, targets(supremeRecipe))) {
                return supremeRecipe.getRegistryName();
            }
        }
        return null;
    }

    private Map<Integer, List<ItemStack>> targets(SupremeRecipe recipe) {
        Map<Integer, List<ItemStack>> targets = new HashMap<>();
        int width = Math.max(1, recipe.getWidth());
        int height = Math.max(1, recipe.getHeight());
        List<net.minecraft.item.crafting.Ingredient> ingredients = recipe.getSupremeIngredients();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int ingredientIndex = x + y * width;
                if (ingredientIndex >= ingredients.size()) {
                    continue;
                }
                ItemStack[] stacks = ingredients.get(ingredientIndex).getMatchingStacks();
                if (stacks.length > 0) {
                    targets.put(SupremeTableInventory.indexOf(x, y), Arrays.asList(stacks));
                }
            }
        }
        return targets;
    }

    private boolean sameInputs(Map<Integer, List<ItemStack>> left, Map<Integer, List<ItemStack>> right) {
        if (left.size() != right.size()) {
            return false;
        }
        for (Map.Entry<Integer, List<ItemStack>> entry : left.entrySet()) {
            List<ItemStack> rightCandidates = right.get(entry.getKey());
            if (rightCandidates == null || !sameCandidates(entry.getValue(), rightCandidates)) {
                return false;
            }
        }
        return true;
    }

    private boolean sameCandidates(List<ItemStack> left, List<ItemStack> right) {
        if (left.size() != right.size()) {
            return false;
        }
        for (ItemStack leftStack : left) {
            boolean found = false;
            for (ItemStack rightStack : right) {
                if (ItemStack.areItemStacksEqual(leftStack, rightStack)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

}
