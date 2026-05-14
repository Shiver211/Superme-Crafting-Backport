package com.shiver.supermecrafting.jei;

import com.shiver.supermecrafting.Tags;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;

public class SupremeJeiCategory implements IRecipeCategory<SupremeRecipeWrapper> {

    public static final int GRID_PANEL_SIZE = 144;
    public static final int ARROW_WIDTH = 24;
    public static final int RESULT_SLOT_SIZE = 18;
    public static final int H_GAP = 8;

    public static final int PANEL_WIDTH = GRID_PANEL_SIZE + H_GAP + ARROW_WIDTH + H_GAP + RESULT_SLOT_SIZE;
    public static final int PANEL_HEIGHT = GRID_PANEL_SIZE;

    private static final int RESULT_SLOT_X = GRID_PANEL_SIZE + H_GAP + ARROW_WIDTH + H_GAP;
    private static final int RESULT_SLOT_Y = (GRID_PANEL_SIZE - RESULT_SLOT_SIZE) / 2;
    private static final int ARROW_X = GRID_PANEL_SIZE + H_GAP;
    private static final int ARROW_Y = (GRID_PANEL_SIZE - 17) / 2;

    private final IDrawable icon;

    public SupremeJeiCategory(IGuiHelper guiHelper) {
        // 使用程序化渲染，不依赖纹理文件
        this.icon = guiHelper.createDrawableIngredient(
                new net.minecraft.item.ItemStack(net.minecraft.init.Blocks.CRAFTING_TABLE));
    }

    @Override
    public String getUid() {
        return SupremeJeiPlugin.CATEGORY_ID;
    }

    @Override
    public String getTitle() {
        return I18n.format("jei." + Tags.MOD_ID + ".title");
    }

    @Override
    public String getModName() {
        return Tags.MOD_NAME;
    }

    @Override
    public IDrawable getBackground() {
        return null; // 程序化渲染背景
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayout layout, SupremeRecipeWrapper wrapper, IIngredients ingredients) {
        // 设置输出槽位
        layout.getItemStacks().init(0, false, RESULT_SLOT_X, RESULT_SLOT_Y);
        java.util.List<ItemStack> outputs = ingredients.getOutputs(ItemStack.class).get(0);
        if (!outputs.isEmpty()) {
            layout.getItemStacks().set(0, outputs.get(0));
        }

        // 设置输入配料（不可见但可搜索）
        NonNullList<Ingredient> inputIngredients = wrapper.getIngredients();
        int index = 0;
        for (int i = 0; i < inputIngredients.size(); i++) {
            Ingredient ing = inputIngredients.get(i);
            if (ing != null && ing.getMatchingStacks().length > 0) {
                layout.getItemStacks().init(index + 1, true, -1000, -1000);
                layout.getItemStacks().set(index + 1, java.util.Arrays.asList(ing.getMatchingStacks()));
                index++;
            }
        }
    }

    @Override
    public void drawExtras(Minecraft mc) {
        // 程序化绘制背景
        net.minecraft.client.gui.Gui.drawRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT, 0xFFC6C6C6);

        // 绘制网格背景
        net.minecraft.client.gui.Gui.drawRect(0, 0, GRID_PANEL_SIZE, GRID_PANEL_SIZE, 0xFF373737);

        // 绘制箭头
        int color = 0xFF8B8B8B;
        net.minecraft.client.gui.Gui.drawRect(ARROW_X, ARROW_Y + 7, ARROW_X + ARROW_WIDTH - 6, ARROW_Y + 10, color);
        for (int i = 0; i < 6; i++) {
            net.minecraft.client.gui.Gui.drawRect(
                    ARROW_X + ARROW_WIDTH - 6 - i, ARROW_Y + 4 + i,
                    ARROW_X + ARROW_WIDTH - 5 - i, ARROW_Y + 13 - i, color);
        }
    }

}
