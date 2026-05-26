package com.shiver.supermecrafting.crafttweaker;

import com.shiver.supermecrafting.SupremeCrafting;
import com.shiver.supermecrafting.recipe.SupremeShapedRecipe;
import com.shiver.supermecrafting.recipe.SupremeShapelessRecipe;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenRegister
@ZenClass("mods.supreme_crafting.SupremeCrafting")
public final class SupremeCraftingZen {
    private static int counter;

    private SupremeCraftingZen() {
    }

    @ZenMethod
    public static void addShapeless(String name, IItemStack output, IIngredient[] ingredients) {
        CraftTweakerAPI.apply(new AddRecipe(name, toShapeless(output, ingredients)));
    }

    private static IRecipe toShapeless(IItemStack output, IIngredient[] ingredients) {
        NonNullList<Ingredient> list = NonNullList.create();
        for (IIngredient ingredient : ingredients) {
            list.add(CraftTweakerIngredient.of(ingredient));
        }
        return new SupremeShapelessRecipe(list, (ItemStack) output.getInternal());
    }

    @ZenMethod
    public static void addShaped(String name, IItemStack output, int x, int y, IIngredient[][] pattern) {
        CraftTweakerAPI.apply(new AddRecipe(name, toShaped(output, x, y, pattern)));
    }

    private static IRecipe toShaped(IItemStack output, int offsetX, int offsetY, IIngredient[][] pattern) {
        int height = pattern.length;
        int width = height == 0 ? 0 : pattern[0].length;
        if (offsetX < 0 || offsetY < 0 || offsetX + width > 81 || offsetY + height > 81) {
            throw new IllegalArgumentException("Pattern is outside the 81x81 supreme table");
        }
        NonNullList<Ingredient> list = NonNullList.withSize(width * height, Ingredient.EMPTY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                list.set(x + y * width, CraftTweakerIngredient.of(pattern[y][x]));
            }
        }
        return new SupremeShapedRecipe(offsetX, offsetY, width, height, list, (ItemStack) output.getInternal());
    }

    private static class AddRecipe implements IAction {
        private final String name;
        private final IRecipe recipe;

        AddRecipe(String name, IRecipe recipe) {
            this.name = name;
            this.recipe = recipe;
        }

        @Override
        public void apply() {
            ResourceLocation id = new ResourceLocation(SupremeCrafting.MOD_ID, name == null || name.isEmpty()
                    ? "ct_" + counter++ : name);
            recipe.setRegistryName(id);
            ForgeRegistries.RECIPES.register(recipe);
        }

        @Override
        public String describe() {
            return "Adding Supreme Crafting recipe " + name;
        }
    }
}
