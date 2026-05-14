package com.shiver.supermecrafting.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;

/**
 * Factory for parsing Supreme Shapeless recipes from JSON.
 * Supports up to 6561 ingredients for the Supreme Crafting Table.
 *
 * JSON format:
 * {
 *   "type": "supreme_crafting:supreme_shapeless",
 *   "ingredients": [
 *     { "item": "minecraft:diamond" },
 *     "#logWood",
 *     ...
 *   ],
 *   "result": { "item": "minecraft:diamond_block" },
 *   "group": "supreme_crafting"
 * }
 */
public class SupremeShapelessRecipeFactory implements IRecipeFactory {

    @Override
    public IRecipe parse(JsonContext context, JsonObject json) {
        // Parse ingredients
        JsonArray ingredientsArray = json.getAsJsonArray("ingredients");
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (JsonElement element : ingredientsArray) {
            ingredients.add(CraftingHelper.getIngredient(element, context));
        }

        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Supreme shapeless recipe must have at least one ingredient");
        }

        // Parse result
        JsonObject resultObject = json.getAsJsonObject("result");
        ItemStack result = CraftingHelper.getItemStack(resultObject, context);

        // Parse group (optional)
        String group = json.has("group") ? json.get("group").getAsString() : "";

        return new SupremeShapelessRecipe(group, ingredients, result);
    }
}
