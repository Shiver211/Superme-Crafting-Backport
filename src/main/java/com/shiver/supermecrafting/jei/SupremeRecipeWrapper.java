package com.shiver.supermecrafting.jei;

import com.shiver.supermecrafting.recipe.SupremeRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SupremeRecipeWrapper implements IRecipeWrapper {
    private static final int ITEM_STEP = 20;
    private static final int TEXT_COLOR = 0xFF404040;
    private static final int PAGE_BUTTON_W = 12;
    private static final int PAGE_BUTTON_H = 12;
    private static final int PAGE_GAP = 2;
    private static final int PAGE_NEXT_X = SupremeJeiCategory.MATERIAL_X + SupremeJeiCategory.MATERIAL_W - PAGE_BUTTON_W - 2;
    private static final int PAGE_PREV_X = PAGE_NEXT_X - PAGE_BUTTON_W - PAGE_GAP;
    private static final int PAGE_BUTTON_Y = SupremeJeiCategory.MATERIAL_Y + SupremeJeiCategory.MATERIAL_H - PAGE_BUTTON_H - 2;

    private final SupremeRecipe recipe;
    private int materialPage;

    public SupremeRecipeWrapper(SupremeRecipe recipe) {
        this.recipe = recipe;
    }

    public SupremeRecipe recipe() {
        return recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<List<ItemStack>> inputs = new ArrayList<>();
        for (Ingredient ingredient : recipe.getSupremeIngredients()) {
            inputs.add(Arrays.asList(ingredient.getMatchingStacks()));
        }
        ingredients.setInputLists(ItemStack.class, inputs);
        ingredients.setOutput(ItemStack.class, recipe.getRecipeOutput());
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        List<MaterialEntry> materials = materials();
        int columns = Math.max(1, SupremeJeiCategory.MATERIAL_W / ITEM_STEP);
        int visibleRows = Math.max(1, SupremeJeiCategory.MATERIAL_H / ITEM_STEP);
        int pageSize = columns * visibleRows;
        int maxPage = Math.max(0, (materials.size() + pageSize - 1) / pageSize - 1);
        materialPage = Math.max(0, Math.min(materialPage, maxPage));

        minecraft.fontRenderer.drawString("Materials", SupremeJeiCategory.MATERIAL_X,
                SupremeJeiCategory.MATERIAL_Y - 11, TEXT_COLOR);
        if (maxPage > 0) {
            String page = (materialPage + 1) + "/" + (maxPage + 1);
            minecraft.fontRenderer.drawString(page, PAGE_PREV_X - minecraft.fontRenderer.getStringWidth(page) - 4,
                    PAGE_BUTTON_Y + 4, TEXT_COLOR);
        }
        drawPageButton(minecraft, mouseX, mouseY, PAGE_PREV_X, PAGE_BUTTON_Y, "<", materialPage > 0);
        drawPageButton(minecraft, mouseX, mouseY, PAGE_NEXT_X, PAGE_BUTTON_Y, ">", materialPage < maxPage);

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepth();
        GlStateManager.pushMatrix();
        for (int i = materialPage * pageSize; i < materials.size(); i++) {
            int visibleIndex = i - materialPage * pageSize;
            int row = visibleIndex / columns;
            if (row >= visibleRows) {
                break;
            }
            int col = visibleIndex % columns;
            int x = SupremeJeiCategory.MATERIAL_X + col * ITEM_STEP;
            int y = SupremeJeiCategory.MATERIAL_Y + row * ITEM_STEP;
            MaterialEntry entry = materials.get(i);
            minecraft.getRenderItem().renderItemAndEffectIntoGUI(entry.stack, x, y);
            minecraft.getRenderItem().renderItemOverlayIntoGUI(minecraft.fontRenderer, entry.stack, x, y,
                    entry.count > 1 ? String.valueOf(entry.count) : null);
        }
        GlStateManager.popMatrix();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        RenderHelper.disableStandardItemLighting();
    }

    private void drawPageButton(Minecraft minecraft, int mouseX, int mouseY, int x, int y, String label, boolean enabled) {
        GuiButton button = new GuiButton(0, x, y, PAGE_BUTTON_W, PAGE_BUTTON_H, label);
        button.enabled = enabled;
        button.drawButton(minecraft, mouseX, mouseY, 0.0F);
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        MaterialEntry entry = hoveredMaterial(mouseX, mouseY);
        if (entry == null) {
            return java.util.Collections.emptyList();
        }
        return entry.tooltip();
    }

    private MaterialEntry hoveredMaterial(int mouseX, int mouseY) {
        if (!isInMaterialArea(mouseX, mouseY)) {
            return null;
        }
        List<MaterialEntry> materials = materials();
        int columns = Math.max(1, SupremeJeiCategory.MATERIAL_W / ITEM_STEP);
        int visibleRows = Math.max(1, SupremeJeiCategory.MATERIAL_H / ITEM_STEP);
        int pageSize = columns * visibleRows;
        int col = (mouseX - SupremeJeiCategory.MATERIAL_X) / ITEM_STEP;
        int row = (mouseY - SupremeJeiCategory.MATERIAL_Y) / ITEM_STEP;
        if (col >= columns || row >= visibleRows) {
            return null;
        }
        int index = materialPage * pageSize + row * columns + col;
        return index >= 0 && index < materials.size() ? materials.get(index) : null;
    }

    @Override
    public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) {
            return false;
        }
        List<MaterialEntry> materials = materials();
        int columns = Math.max(1, SupremeJeiCategory.MATERIAL_W / ITEM_STEP);
        int visibleRows = Math.max(1, SupremeJeiCategory.MATERIAL_H / ITEM_STEP);
        int pageSize = columns * visibleRows;
        int maxPage = Math.max(0, (materials.size() + pageSize - 1) / pageSize - 1);
        if (inRect(mouseX, mouseY, PAGE_PREV_X, PAGE_BUTTON_Y, PAGE_BUTTON_W, PAGE_BUTTON_H) && materialPage > 0) {
            materialPage--;
            return true;
        }
        if (inRect(mouseX, mouseY, PAGE_NEXT_X, PAGE_BUTTON_Y, PAGE_BUTTON_W, PAGE_BUTTON_H) && materialPage < maxPage) {
            materialPage++;
            return true;
        }
        return false;
    }

    private boolean inRect(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private boolean isInMaterialArea(int mouseX, int mouseY) {
        return mouseX >= SupremeJeiCategory.MATERIAL_X
                && mouseX < SupremeJeiCategory.MATERIAL_X + SupremeJeiCategory.MATERIAL_W
                && mouseY >= SupremeJeiCategory.MATERIAL_Y
                && mouseY < SupremeJeiCategory.MATERIAL_Y + SupremeJeiCategory.MATERIAL_H;
    }

    private List<MaterialEntry> materials() {
        List<MaterialEntry> materials = new ArrayList<>();
        for (Ingredient ingredient : recipe.getSupremeIngredients()) {
            ItemStack[] stacks = ingredient.getMatchingStacks();
            if (stacks.length == 0 || stacks[0].isEmpty()) {
                continue;
            }
            ItemStack display = stacks[0].copy();
            display.setCount(1);
            MaterialEntry existing = findMaterial(materials, display);
            if (existing == null) {
                materials.add(new MaterialEntry(display, 1));
            } else {
                existing.count++;
            }
        }
        return materials;
    }

    private MaterialEntry findMaterial(List<MaterialEntry> materials, ItemStack stack) {
        for (MaterialEntry entry : materials) {
            if (ItemStack.areItemsEqual(entry.stack, stack)
                    && ItemStack.areItemStackTagsEqual(entry.stack, stack)) {
                return entry;
            }
        }
        return null;
    }

    private static final class MaterialEntry {
        private final ItemStack stack;
        private int count;
        private List<String> tooltip;

        private MaterialEntry(ItemStack stack, int count) {
            this.stack = stack;
            this.count = count;
        }

        private List<String> tooltip() {
            if (tooltip == null) {
                tooltip = new ArrayList<>();
                tooltip.add(stack.getDisplayName());
                tooltip.add("x" + count);
            }
            return tooltip;
        }
    }
}
