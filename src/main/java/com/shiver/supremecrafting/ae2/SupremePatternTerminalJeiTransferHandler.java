package com.shiver.supremecrafting.ae2;

import com.shiver.supremecrafting.jei.SupremeJeiTransferSupport;
import com.shiver.supremecrafting.net.PacketTransferRecipe;
import com.shiver.supremecrafting.net.SCNetwork;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class SupremePatternTerminalJeiTransferHandler implements IRecipeTransferHandler<ContainerSupremePatternTerminal> {
    @Override
    public Class<ContainerSupremePatternTerminal> getContainerClass() {
        return ContainerSupremePatternTerminal.class;
    }

    @Override
    @Nullable
    public IRecipeTransferError transferRecipe(ContainerSupremePatternTerminal container,
                                               mezz.jei.api.gui.IRecipeLayout recipeLayout,
                                               EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        if (doTransfer) {
            ResourceLocation recipeId = SupremeJeiTransferSupport.findRecipeId(recipeLayout);
            if (recipeId != null) {
                SCNetwork.CHANNEL.sendToServer(new PacketTransferRecipe(recipeId, false));
            }
        }
        return null;
    }
}
