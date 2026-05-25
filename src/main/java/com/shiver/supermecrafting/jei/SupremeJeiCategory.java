package com.shiver.supermecrafting.jei;

import com.shiver.supermecrafting.SupremeCrafting;
import com.shiver.supermecrafting.registry.SCRegistry;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class SupremeJeiCategory implements IRecipeCategory<SupremeRecipeWrapper> {
    private final IDrawable icon;
    private final IDrawableStatic background;

    public SupremeJeiCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableIngredient(new ItemStack(SCRegistry.SUPREME_TABLE));
        this.background = helper.createBlankDrawable(202, 144);
    }

    @Override public String getUid() { return SupremeJeiPlugin.UID; }
    @Override public String getTitle() { return net.minecraft.client.resources.I18n.format("jei.supreme_crafting.title"); }
    @Override public String getModName() { return SupremeCrafting.NAME; }
    @Override public IDrawable getIcon() { return icon; }
    @Override public IDrawable getBackground() { return background; }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, SupremeRecipeWrapper wrapper, IIngredients ingredients) {
        int gridSize = 144;
        int width = Math.max(1, wrapper.recipe().getWidth());
        int height = Math.max(1, wrapper.recipe().getHeight());
        int slot = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int sx = Math.round((float) gridSize * x / width);
                int sy = Math.round((float) gridSize * y / height);
                recipeLayout.getItemStacks().init(slot, true, sx, sy);
                slot++;
            }
        }
        recipeLayout.getItemStacks().init(slot, false, 184, 63);
        recipeLayout.getItemStacks().set(ingredients);
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        int color = 0xFF8B8B8B;
        net.minecraft.client.gui.Gui.drawRect(152, 70, 174, 73, color);
        for (int i = 0; i < 6; i++) {
            net.minecraft.client.gui.Gui.drawRect(174 - i, 67 + i, 175 - i, 76 - i, color);
        }
    }
}
