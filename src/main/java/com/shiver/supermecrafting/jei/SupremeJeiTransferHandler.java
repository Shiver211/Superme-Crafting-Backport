package com.shiver.supermecrafting.jei;

import com.shiver.supermecrafting.net.PacketTransferRecipe;
import com.shiver.supermecrafting.net.SCNetwork;
import com.shiver.supermecrafting.table.ContainerSupremeTable;
import com.shiver.supermecrafting.table.SupremeTableInventory;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.ArrayList;
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
            Map<Integer, List<ItemStack>> targets = new HashMap<>();
            for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry
                    : recipeLayout.getItemStacks().getGuiIngredients().entrySet()) {
                IGuiIngredient<ItemStack> ingredient = entry.getValue();
                if (!ingredient.isInput()) continue;
                List<ItemStack> candidates = new ArrayList<>();
                for (ItemStack stack : ingredient.getAllIngredients()) {
                    if (stack != null && !stack.isEmpty()) {
                        candidates.add(stack.copy());
                    }
                }
                if (candidates.isEmpty()) continue;
                int slot = entry.getKey();
                if (slot >= 0 && slot < SupremeTableInventory.SIZE) {
                    targets.put(slot, candidates);
                }
            }
            SCNetwork.CHANNEL.sendToServer(new PacketTransferRecipe(targets, maxTransfer));
        }
        return null;
    }
}
