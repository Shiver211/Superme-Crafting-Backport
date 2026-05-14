package com.shiver.supermecrafting.jei;

import com.shiver.supermecrafting.Tags;
import com.shiver.supermecrafting.client.RecipeViewerHooks;
import com.shiver.supermecrafting.recipe.SupremeCraftingRecipe;
import com.shiver.supermecrafting.registry.ModBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

@JEIPlugin
public class SupremeJeiPlugin implements IModPlugin {

    public static final String CATEGORY_ID = Tags.MOD_ID + ".supreme_crafting";

    @Override
    public void register(IModRegistry registry) {
        // 注册配方处理器
        registry.handleRecipes(SupremeCraftingRecipe.class,
                recipe -> new SupremeRecipeWrapper(recipe), CATEGORY_ID);

        // 添加所有 Supreme Crafting 配方
        List<IRecipe> recipes = new ArrayList<>();
        for (IRecipe recipe : ForgeRegistries.RECIPES) {
            if (recipe instanceof SupremeCraftingRecipe) {
                recipes.add(recipe);
            }
        }
        registry.addRecipes(recipes, CATEGORY_ID);

        // 添加配方催化剂（承载配方的方块）
        registry.addRecipeCatalyst(new ItemStack(ModBlocks.SUPREME_TABLE), CATEGORY_ID);

        // 注册 RecipeViewerHooks（JEI 1.12.2 不支持 showCategories，留空）
        RecipeViewerHooks.openSupremeRecipes.add(() -> {
            // JEI 1.12.2 没有公开的 showCategories API
        });
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new SupremeJeiCategory(registry.getJeiHelpers().getGuiHelper()));
    }
}
