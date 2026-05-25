package com.shiver.supermecrafting.jei;

import com.shiver.supermecrafting.SupremeCrafting;
import com.shiver.supermecrafting.client.RecipeViewerHooks;
import com.shiver.supermecrafting.recipe.SupremeRecipe;
import com.shiver.supermecrafting.registry.SCRegistry;
import com.shiver.supermecrafting.table.ContainerSupremeTable;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

@JEIPlugin
public class SupremeJeiPlugin implements IModPlugin {
    public static final String UID = SupremeCrafting.MOD_ID + ".supreme_crafting";
    private IJeiRuntime runtime;

    public SupremeJeiPlugin() {
        RecipeViewerHooks.OPEN_SUPREME_RECIPES.add(() -> {
            if (runtime != null) runtime.getRecipesGui().showCategories(java.util.Collections.singletonList(UID));
        });
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new SupremeJeiCategory(registry.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void register(IModRegistry registry) {
        List<SupremeRecipeWrapper> wrappers = new ArrayList<>();
        for (IRecipe recipe : ForgeRegistries.RECIPES) {
            if (recipe instanceof SupremeRecipe) wrappers.add(new SupremeRecipeWrapper((SupremeRecipe) recipe));
        }
        registry.addRecipes(wrappers, UID);
        registry.addRecipeCatalyst(new ItemStack(SCRegistry.SUPREME_TABLE), UID);
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(new SupremeJeiTransferHandler(), UID);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }
}
