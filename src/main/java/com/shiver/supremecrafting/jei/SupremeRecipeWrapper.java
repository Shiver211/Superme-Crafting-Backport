package com.shiver.supremecrafting.jei;

import com.shiver.supremecrafting.recipe.SupremeRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SupremeRecipeWrapper implements IRecipeWrapper {
    private static final int TEXT_COLOR = 0xFF404040;
    private static final int DISABLED_TEXT_COLOR = 0xFF808080;
    private static final int PAGE_BUTTON_W = 12;
    private static final int PAGE_BUTTON_H = 12;
    private static final int PAGE_GAP = 2;
    private static final int PAGE_NEXT_X = SupremeJeiCategory.MATERIAL_X + SupremeJeiCategory.MATERIAL_W - PAGE_BUTTON_W - 2;
    private static final int PAGE_PREV_X = PAGE_NEXT_X - PAGE_BUTTON_W - PAGE_GAP;
    private static final int PAGE_BUTTON_Y = SupremeJeiCategory.MATERIAL_Y + SupremeJeiCategory.MATERIAL_H - PAGE_BUTTON_H - 2;
    private static final int TEXT_X = SupremeJeiCategory.MATERIAL_X + 2;
    private static final int TEXT_Y = SupremeJeiCategory.MATERIAL_Y + 3;
    private static final int LINE_HEIGHT = 10;
    private static final int MATERIAL_PAGE_SIZE = Math.max(1, (SupremeJeiCategory.MATERIAL_H - 6) / LINE_HEIGHT);

    private final SupremeRecipe recipe;
    private final List<MaterialEntry> materials;
    private int materialPage;

    public SupremeRecipeWrapper(SupremeRecipe recipe) {
        this.recipe = recipe;
        this.materials = createMaterials();
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
        int maxPage = maxPage();
        materialPage = Math.max(0, Math.min(materialPage, maxPage));

        minecraft.fontRenderer.drawString(net.minecraft.client.resources.I18n.format("jei.supreme_crafting.materials"),
                SupremeJeiCategory.MATERIAL_X,
                SupremeJeiCategory.MATERIAL_Y - 11, TEXT_COLOR);
        if (maxPage > 0) {
            String page = (materialPage + 1) + "/" + (maxPage + 1);
            minecraft.fontRenderer.drawString(page, PAGE_PREV_X - minecraft.fontRenderer.getStringWidth(page) - 4,
                    PAGE_BUTTON_Y + 4, TEXT_COLOR);
        }
        drawPageButton(minecraft, mouseX, mouseY, PAGE_PREV_X, PAGE_BUTTON_Y, "<", materialPage > 0);
        drawPageButton(minecraft, mouseX, mouseY, PAGE_NEXT_X, PAGE_BUTTON_Y, ">", materialPage < maxPage);

        for (int i = materialPage * MATERIAL_PAGE_SIZE; i < materials.size(); i++) {
            int visibleIndex = i - materialPage * MATERIAL_PAGE_SIZE;
            if (visibleIndex >= MATERIAL_PAGE_SIZE) {
                break;
            }
            MaterialEntry entry = materials.get(i);
            minecraft.fontRenderer.drawString(entry.label, TEXT_X, TEXT_Y + visibleIndex * LINE_HEIGHT, TEXT_COLOR);
        }
    }

    private void drawPageButton(Minecraft minecraft, int mouseX, int mouseY, int x, int y, String label, boolean enabled) {
        boolean hovered = enabled && inRect(mouseX, mouseY, x, y, PAGE_BUTTON_W, PAGE_BUTTON_H);
        Gui.drawRect(x, y, x + PAGE_BUTTON_W, y + PAGE_BUTTON_H, enabled ? 0xFF606060 : 0xFF9A9A9A);
        Gui.drawRect(x + 1, y + 1, x + PAGE_BUTTON_W - 1, y + PAGE_BUTTON_H - 1,
                hovered ? 0xFFE0E0E0 : 0xFFC6C6C6);
        int textX = x + (PAGE_BUTTON_W - minecraft.fontRenderer.getStringWidth(label)) / 2;
        minecraft.fontRenderer.drawString(label, textX, y + 2, enabled ? TEXT_COLOR : DISABLED_TEXT_COLOR);
    }

    @Override
    public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) {
            return false;
        }
        int maxPage = maxPage();
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

    private int maxPage() {
        return Math.max(0, (materials.size() + MATERIAL_PAGE_SIZE - 1) / MATERIAL_PAGE_SIZE - 1);
    }

    private List<MaterialEntry> createMaterials() {
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
                existing.grow();
            }
        }
        return materials;
    }

    private MaterialEntry findMaterial(List<MaterialEntry> materials, ItemStack stack) {
        for (MaterialEntry entry : materials) {
            if (ItemStack.areItemsEqual(entry.matchStack, stack)
                    && ItemStack.areItemStackTagsEqual(entry.matchStack, stack)) {
                return entry;
            }
        }
        return null;
    }

    private static final class MaterialEntry {
        private final ItemStack matchStack;
        private final String name;
        private int count;
        private String label;

        private MaterialEntry(ItemStack stack, int count) {
            this.matchStack = stack;
            this.name = stack.getDisplayName();
            this.count = count;
            updateLabel();
        }

        private void grow() {
            count++;
            updateLabel();
        }

        private void updateLabel() {
            label = name + " x" + count;
        }
    }
}
