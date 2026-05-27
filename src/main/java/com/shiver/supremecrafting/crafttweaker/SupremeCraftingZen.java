package com.shiver.supremecrafting.crafttweaker;

import com.shiver.supremecrafting.SupremeCrafting;
import com.shiver.supremecrafting.recipe.SupremeShapedRecipe;
import com.shiver.supremecrafting.recipe.SupremeShapelessRecipe;
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

import java.util.HashMap;
import java.util.Map;

@ZenRegister
@ZenClass("mods.supreme_crafting.SupremeCrafting")
public final class SupremeCraftingZen {
    private static final int TABLE_SIZE = 81;
    private static final int TOKEN_LENGTH = 3;
    private static final String EMPTY_TOKEN = "___";
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
    public static void addShaped(String name, IItemStack output, int x, int y,
                                 String[] pattern, String[] keys, IIngredient[] ingredients) {
        CraftTweakerAPI.apply(new AddRecipe(name, toShaped(output, x, y, pattern, keys, ingredients)));
    }

    private static IRecipe toShaped(IItemStack output, int offsetX, int offsetY,
                                    String[] pattern, String[] keys, IIngredient[] ingredients) {
        int height = pattern.length;
        int width = height == 0 ? 0 : tokens(pattern[0]).length;
        if (height == 0 || width == 0) {
            throw new IllegalArgumentException("Pattern must not be empty");
        }
        if (offsetX < 0 || offsetY < 0 || offsetX + width > TABLE_SIZE || offsetY + height > TABLE_SIZE) {
            throw new IllegalArgumentException("Pattern is outside the 81x81 supreme table");
        }
        Map<String, Ingredient> key = key(keys, ingredients);
        NonNullList<Ingredient> list = NonNullList.withSize(width * height, Ingredient.EMPTY);
        for (int y = 0; y < height; y++) {
            String[] row = tokens(pattern[y]);
            if (row.length != width) {
                throw new IllegalArgumentException("Pattern rows must have the same width");
            }
            for (int x = 0; x < width; x++) {
                String token = row[x];
                list.set(x + y * width, EMPTY_TOKEN.equals(token) ? Ingredient.EMPTY : ingredient(token, key));
            }
        }
        return new SupremeShapedRecipe(offsetX, offsetY, width, height, list, (ItemStack) output.getInternal());
    }

    private static Map<String, Ingredient> key(String[] keys, IIngredient[] ingredients) {
        if (keys.length != ingredients.length) {
            throw new IllegalArgumentException("Key count must match ingredient count");
        }
        Map<String, Ingredient> key = new HashMap<>();
        for (int i = 0; i < keys.length; i++) {
            String token = keys[i];
            validateToken(token);
            if (EMPTY_TOKEN.equals(token)) {
                throw new IllegalArgumentException("___ is reserved for empty slots");
            }
            if (key.put(token, CraftTweakerIngredient.of(ingredients[i])) != null) {
                throw new IllegalArgumentException("Duplicate token " + token);
            }
        }
        return key;
    }

    private static Ingredient ingredient(String token, Map<String, Ingredient> key) {
        validateToken(token);
        Ingredient ingredient = key.get(token);
        if (ingredient == null) {
            throw new IllegalArgumentException("Unknown token " + token);
        }
        return ingredient;
    }

    private static void validateToken(String token) {
        if (token == null || token.length() != TOKEN_LENGTH) {
            throw new IllegalArgumentException("Token must be exactly 3 characters");
        }
    }

    private static String[] tokens(String row) {
        if (row == null || row.trim().isEmpty()) {
            throw new IllegalArgumentException("Pattern row must not be empty");
        }
        return row.trim().split("\\s+");
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
