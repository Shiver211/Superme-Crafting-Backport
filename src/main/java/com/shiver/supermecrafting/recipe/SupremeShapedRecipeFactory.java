package com.shiver.supermecrafting.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory for parsing Supreme Shaped recipes from JSON.
 * Supports patterns up to 81x81 for the Supreme Crafting Table.
 *
 * JSON format:
 * {
 *   "type": "supreme_crafting:supreme_shaped",
 *   "pattern": ["ABC", "DEF", "GHI"],
 *   "key": {
 *     "A": { "item": "minecraft:diamond" },
 *     "B": "#logWood",
 *     ...
 *   },
 *   "result": { "item": "minecraft:diamond_block" },
 *   "group": "supreme_crafting"
 * }
 */
public class SupremeShapedRecipeFactory implements IRecipeFactory {

    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        // Parse pattern
        JsonArray patternArray = json.getAsJsonArray("pattern");
        List<String> pattern = new ArrayList<>();
        for (JsonElement row : patternArray) {
            pattern.add(row.getAsString());
        }

        // Parse key
        JsonObject keyObject = json.getAsJsonObject("key");
        Map<String, Ingredient> key = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : keyObject.entrySet()) {
            String symbol = entry.getKey();
            if (symbol.length() != 1) {
                throw new IllegalArgumentException("Key symbol must be a single character: '" + symbol + "'");
            }
            char ch = symbol.charAt(0);
            if (ch == ' ' || ch == '.') {
                throw new IllegalArgumentException("Key symbol '" + ch + "' is reserved");
            }
            Ingredient ingredient = CraftingHelper.getIngredient(entry.getValue(), context);
            key.put(symbol, ingredient);
        }

        // Parse result
        JsonObject resultObject = json.getAsJsonObject("result");
        ItemStack result = CraftingHelper.getItemStack(resultObject, context);

        // Parse group (optional)
        String group = json.has("group") ? json.get("group").getAsString() : "";

        // Build the pattern
        SupremeShapedPattern shapedPattern = SupremeShapedPattern.parse(key, pattern);

        return new SupremeShapedRecipe(group, shapedPattern, result);
    }
}
