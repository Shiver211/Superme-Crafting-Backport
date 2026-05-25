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
            Map<Integer, String> targets = new HashMap<>();
            for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry
                    : recipeLayout.getItemStacks().getGuiIngredients().entrySet()) {
                IGuiIngredient<ItemStack> ingredient = entry.getValue();
                if (!ingredient.isInput()) continue;
                ItemStack displayed = ingredient.getDisplayedIngredient();
                if (displayed == null || displayed.isEmpty()) continue;
                int index = entry.getKey();
                int gridSlot = (index % 81) + (index / 81) * SupremeTableInventory.WIDTH;
                targets.put(gridSlot, stackKey(displayed));
            }
            SCNetwork.CHANNEL.sendToServer(new PacketTransferRecipe(targets));
        }
        return null;
    }

    private static String stackKey(ItemStack stack) {
        return stack.getItem().getRegistryName().toString() + "@" + stack.getMetadata();
    }
}
