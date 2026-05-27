package com.shiver.supremecrafting.jei;

import com.shiver.supremecrafting.SupremeCrafting;
import com.shiver.supremecrafting.ae2.AE2OptionalBridge;
import com.shiver.supremecrafting.client.RecipeViewerHooks;
import com.shiver.supremecrafting.recipe.SupremeRecipe;
import com.shiver.supremecrafting.registry.SCRegistry;
import com.shiver.supremecrafting.table.ContainerSupremeTable;
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
import java.lang.reflect.Method;

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
        registerAe2(registry);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    private static void registerAe2(IModRegistry registry) {
        if (!AE2OptionalBridge.loaded()) {
            return;
        }
        try {
            Class<?> bridge = Class.forName("com.shiver.supremecrafting.ae2.AE2JeiBridge");
            Method method = bridge.getMethod("register", IModRegistry.class, String.class);
            method.invoke(null, registry, UID);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to register AE2 JEI integration", e);
        }
    }
}
