package com.shiver.supermecrafting.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.HashMap;
import java.util.Map;

public class SupremeShapedFactory implements IRecipeFactory {
    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        String[] pattern = pattern(JsonUtils.getJsonArray(json, "pattern"));
        Map<Character, Ingredient> key = key(context, JsonUtils.getJsonObject(json, "key"));
        int offsetX = JsonUtils.getInt(json, "x", 0);
        int offsetY = JsonUtils.getInt(json, "y", 0);
        int width = pattern[0].length();
        int height = pattern.length;
        if (offsetX < 0 || offsetY < 0 || offsetX + width > 81 || offsetY + height > 81) {
            throw new JsonSyntaxException("Pattern is outside the 81x81 supreme table");
        }
        NonNullList<Ingredient> ingredients = NonNullList.withSize(width * height, Ingredient.EMPTY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                char c = pattern[y].charAt(x);
                ingredients.set(x + y * width, c == '.' || c == ' ' ? Ingredient.EMPTY : key.get(c));
            }
        }
        ItemStack result = CraftingHelper.getItemStack(JsonUtils.getJsonObject(json, "result"), context);
        return new SupremeShapedRecipe(offsetX, offsetY, width, height, ingredients, result);
    }

    private static String[] pattern(JsonArray array) {
        String[] pattern = new String[array.size()];
        for (int i = 0; i < pattern.length; i++) {
            pattern[i] = JsonUtils.getString(array.get(i), "pattern[" + i + "]");
            if (pattern[i].length() > 81) throw new JsonSyntaxException("Pattern row is wider than 81");
        }
        if (pattern.length == 0 || pattern.length > 81) throw new JsonSyntaxException("Pattern height invalid");
        return pattern;
    }

    private static Map<Character, Ingredient> key(JsonContext context, JsonObject object) {
        Map<Character, Ingredient> key = new HashMap<>();
        for (Map.Entry<String, com.google.gson.JsonElement> entry : object.entrySet()) {
            if (entry.getKey().length() != 1) throw new JsonSyntaxException("Invalid key symbol");
            key.put(entry.getKey().charAt(0), CraftingHelper.getIngredient(entry.getValue(), context));
        }
        return key;
    }
}
