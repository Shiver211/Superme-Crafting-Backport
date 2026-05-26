package com.shiver.supermecrafting.ae2;

import com.shiver.supermecrafting.recipe.SupremeRecipe;
import com.shiver.supermecrafting.table.SupremeTableInventory;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.List;

public final class AE2PatternTerminalTransferBridge {
    private AE2PatternTerminalTransferBridge() {
    }

    public static boolean transfer(EntityPlayerMP player, ResourceLocation recipeId) {
        if (!(player.openContainer instanceof ContainerSupremePatternTerminal)) {
            return false;
        }
        IRecipe recipe = ForgeRegistries.RECIPES.getValue(recipeId);
        if (!(recipe instanceof SupremeRecipe)) {
            return false;
        }
        ContainerSupremePatternTerminal container = (ContainerSupremePatternTerminal) player.openContainer;
        TileSupremePatternTerminal terminal = container.terminal();
        terminal.grid().clear();
        SupremeRecipe supremeRecipe = (SupremeRecipe) recipe;
        int width = Math.max(1, supremeRecipe.getWidth());
        int height = Math.max(1, supremeRecipe.getHeight());
        int offsetX = supremeRecipe.getOffsetX();
        int offsetY = supremeRecipe.getOffsetY();
        List<Ingredient> ingredients = supremeRecipe.getSupremeIngredients();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int ingredientIndex = x + y * width;
                if (ingredientIndex >= ingredients.size()) {
                    continue;
                }
                ItemStack[] stacks = ingredients.get(ingredientIndex).getMatchingStacks();
                if (stacks.length > 0 && !stacks[0].isEmpty()) {
                    ItemStack ghost = stacks[0].copy();
                    ghost.setCount(1);
                    terminal.setInventorySlotContents(SupremeTableInventory.indexOf(offsetX + x, offsetY + y), ghost);
                }
            }
        }
        terminal.markDirty();
        container.detectAndSendChanges();
        return true;
    }
}
