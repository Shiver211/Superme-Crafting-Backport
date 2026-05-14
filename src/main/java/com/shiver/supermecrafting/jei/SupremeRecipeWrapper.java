package com.shiver.supermecrafting.jei;

import com.shiver.supermecrafting.recipe.RecipeGridLayout;
import com.shiver.supermecrafting.recipe.SupremeCraftingRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;

import java.util.Arrays;
import java.util.List;

public class SupremeRecipeWrapper implements IRecipeWrapper {

    private final SupremeCraftingRecipe recipe;
    private final RecipeGridLayout gridLayout;

    public SupremeRecipeWrapper(SupremeCraftingRecipe recipe) {
        this.recipe = recipe;
        this.gridLayout = RecipeGridLayout.fromRecipe(recipe);
    }

    public SupremeCraftingRecipe getRecipe() {
        return recipe;
    }

    public RecipeGridLayout getGridLayout() {
        return gridLayout;
    }

    public NonNullList<Ingredient> getIngredients() {
        return recipe.getIngredients();
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        // 设置输入
        List<Ingredient> inputIngredients = recipe.getIngredients();
        List<List<ItemStack>> inputs = new java.util.ArrayList<>();
        for (Ingredient ing : inputIngredients) {
            inputs.add(Arrays.asList(ing.getMatchingStacks()));
        }
        ingredients.setInputLists(ItemStack.class, inputs);

        // 设置输出
        ingredients.setOutput(ItemStack.class, recipe.getRecipeOutput());
    }

    @Override
    public void drawInfo(Minecraft mc, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        RecipeGridLayout layout = gridLayout;
        if (layout != null) {
            renderGrid(mc, layout, 0, 0, SupremeJeiCategory.GRID_PANEL_SIZE, SupremeJeiCategory.GRID_PANEL_SIZE);
        }
    }

    private void renderGrid(Minecraft mc, RecipeGridLayout layout, int x, int y, int panelW, int panelH) {
        int w = layout.getWidth();
        int h = layout.getHeight();

        RenderHelper.enableGUIStandardItemLighting();
        for (int gy = 0; gy < h; gy++) {
            int sy = y + Math.round((float) panelH * gy / h);
            int cellH = Math.round((float) panelH * (gy + 1) / h) - Math.round((float) panelH * gy / h);
            for (int gx = 0; gx < w; gx++) {
                Ingredient ing = layout.getIngredientAt(gx, gy);
                if (ing == null) {
                    continue;
                }
                ItemStack[] items = ing.getMatchingStacks();
                if (items.length == 0) {
                    continue;
                }
                int sx = x + Math.round((float) panelW * gx / w);
                int cellW = Math.round((float) panelW * (gx + 1) / w) - Math.round((float) panelW * gx / w);
                float scale = Math.min(cellW, cellH) / 16f;
                // 循环显示标签中的所有物品
                ItemStack stack = items[(int) ((System.currentTimeMillis() / 1000) % items.length)];
                GlStateManager.pushMatrix();
                GlStateManager.translate(sx, sy, 0);
                GlStateManager.scale(scale, scale, 1);
                mc.getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
                GlStateManager.popMatrix();
            }
        }
        RenderHelper.disableStandardItemLighting();
    }
}
