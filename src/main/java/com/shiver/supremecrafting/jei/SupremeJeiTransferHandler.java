package com.shiver.supremecrafting.jei;

import com.shiver.supremecrafting.net.PacketTransferRecipe;
import com.shiver.supremecrafting.net.SCNetwork;
import com.shiver.supremecrafting.table.ContainerSupremeTable;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

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
            ResourceLocation recipeId = SupremeJeiTransferSupport.findRecipeId(recipeLayout);
            if (recipeId != null) {
                SCNetwork.CHANNEL.sendToServer(new PacketTransferRecipe(recipeId, maxTransfer));
            }
        }
        return null;
    }

}
