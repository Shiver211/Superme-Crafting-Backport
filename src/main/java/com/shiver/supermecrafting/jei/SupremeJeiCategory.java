package com.shiver.supermecrafting.jei;

import com.shiver.supermecrafting.SupremeCrafting;
import com.shiver.supermecrafting.registry.SCRegistry;
import com.shiver.supermecrafting.table.SupremeTableInventory;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

import java.util.Arrays;
import java.util.List;

public class SupremeJeiCategory implements IRecipeCategory<SupremeRecipeWrapper> {
    private static final int GRID_SIZE = 144;
    private static final int OUTPUT_X = 180;
    private static final int OUTPUT_Y = 63;
    public static final int WIDTH = 202;
    public static final int HEIGHT = 214;
    public static final int MATERIAL_X = 8;
    public static final int MATERIAL_Y = 160;
    public static final int MATERIAL_W = 186;
    public static final int MATERIAL_H = 52;

    private final IDrawable icon;
    private final IDrawableStatic background;

    public SupremeJeiCategory(IGuiHelper helper) {
        this.icon = helper.createDrawableIngredient(new ItemStack(SCRegistry.SUPREME_TABLE));
        this.background = helper.createBlankDrawable(WIDTH, HEIGHT);
    }

    @Override public String getUid() { return SupremeJeiPlugin.UID; }
    @Override public String getTitle() { return net.minecraft.client.resources.I18n.format("jei.supreme_crafting.title"); }
    @Override public String getModName() { return SupremeCrafting.NAME; }
    @Override public IDrawable getIcon() { return icon; }
    @Override public IDrawable getBackground() { return background; }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, SupremeRecipeWrapper wrapper, IIngredients ingredients) {
        int width = Math.max(1, wrapper.recipe().getWidth());
        int height = Math.max(1, wrapper.recipe().getHeight());
        float cellSize = (float) GRID_SIZE / height;
        float startX = (GRID_SIZE - width * cellSize) / 2.0F;
        int slotSize = Math.max(1, Math.min(18, (int) Math.floor(cellSize)));
        IIngredientRenderer<ItemStack> inputRenderer = new ScaledItemRenderer(slotSize);
        List<Ingredient> recipeInputs = wrapper.recipe().getSupremeIngredients();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int sx = Math.round(startX + x * cellSize + (cellSize - slotSize) / 2.0F);
                int sy = Math.round(y * cellSize + (cellSize - slotSize) / 2.0F);
                int slot = SupremeTableInventory.indexOf(x, y);
                recipeLayout.getItemStacks().init(slot, true, inputRenderer, sx, sy, slotSize, slotSize, 0, 0);
                int ingredientIndex = x + y * width;
                if (ingredientIndex < recipeInputs.size()) {
                    recipeLayout.getItemStacks().set(slot, Arrays.asList(recipeInputs.get(ingredientIndex).getMatchingStacks()));
                }
            }
        }
        recipeLayout.getItemStacks().init(SupremeTableInventory.SIZE, false, OUTPUT_X, OUTPUT_Y);
        recipeLayout.getItemStacks().set(SupremeTableInventory.SIZE, wrapper.recipe().getRecipeOutput());
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        int color = 0xFF8B8B8B;
        net.minecraft.client.gui.Gui.drawRect(152, 70, 174, 73, color);
        for (int i = 0; i < 6; i++) {
            net.minecraft.client.gui.Gui.drawRect(174 - i, 67 + i, 175 - i, 76 - i, color);
        }
        net.minecraft.client.gui.Gui.drawRect(MATERIAL_X - 2, MATERIAL_Y - 2,
                MATERIAL_X + MATERIAL_W + 2, MATERIAL_Y + MATERIAL_H + 2, 0xFF8B8B8B);
        net.minecraft.client.gui.Gui.drawRect(MATERIAL_X - 1, MATERIAL_Y - 1,
                MATERIAL_X + MATERIAL_W + 1, MATERIAL_Y + MATERIAL_H + 1, 0xFFFFFFFF);
        net.minecraft.client.gui.Gui.drawRect(MATERIAL_X, MATERIAL_Y,
                MATERIAL_X + MATERIAL_W, MATERIAL_Y + MATERIAL_H, 0xFFC6C6C6);
    }

    private static class ScaledItemRenderer implements IIngredientRenderer<ItemStack> {
        private final float scale;

        private ScaledItemRenderer(int slotSize) {
            this.scale = Math.min(1.0F, slotSize / 18.0F);
        }

        @Override
        public void render(Minecraft minecraft, int xPosition, int yPosition, ItemStack ingredient) {
            if (ingredient == null || ingredient.isEmpty()) {
                return;
            }
            GlStateManager.enableDepth();
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.pushMatrix();
            GlStateManager.translate(xPosition, yPosition, 0.0F);
            GlStateManager.scale(scale, scale, 1.0F);
            minecraft.getRenderItem().renderItemAndEffectIntoGUI(ingredient, 0, 0);
            GlStateManager.popMatrix();
            GlStateManager.disableBlend();
            RenderHelper.disableStandardItemLighting();
        }

        @Override
        public FontRenderer getFontRenderer(Minecraft minecraft, ItemStack ingredient) {
            return minecraft.fontRenderer;
        }

        @Override
        public List<String> getTooltip(Minecraft minecraft, ItemStack ingredient, ITooltipFlag tooltipFlag) {
            return ingredient.getTooltip(minecraft.player, tooltipFlag);
        }
    }
}
