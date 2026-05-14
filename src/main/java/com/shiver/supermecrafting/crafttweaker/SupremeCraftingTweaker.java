package com.shiver.supermecrafting.crafttweaker;

import com.shiver.supermecrafting.Tags;
import com.shiver.supermecrafting.recipe.SupremeShapedPattern;
import com.shiver.supermecrafting.recipe.SupremeShapedRecipe;
import com.shiver.supermecrafting.recipe.SupremeShapelessRecipe;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.annotations.ModOnly;
import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ZenClass("mods.supremecrafting.SupremeCrafting")
@ZenRegister
@ModOnly("supreme_crafting")
public class SupremeCraftingTweaker {

    @ZenMethod
    public static void addShaped(String name, IItemStack output, IIngredient[][] ingredients) {
        CraftTweakerAPI.apply(new AddShapedRecipe(name, output, ingredients));
    }

    @ZenMethod
    public static void addShapeless(String name, IItemStack output, IIngredient[] ingredients) {
        CraftTweakerAPI.apply(new AddShapelessRecipe(name, output, ingredients));
    }

    @ZenMethod
    public static void remove(IItemStack output) {
        CraftTweakerAPI.apply(new RemoveRecipe(output));
    }

    @ZenMethod
    public static void removeByName(String name) {
        CraftTweakerAPI.apply(new RemoveRecipeByName(name));
    }

    @ZenMethod
    public static void removeAll() {
        CraftTweakerAPI.apply(new RemoveAllRecipes());
    }

    // --- IAction implementations ---

    private static class AddShapedRecipe implements IAction {
        private final String name;
        private final IItemStack output;
        private final IIngredient[][] ingredients;

        AddShapedRecipe(String name, IItemStack output, IIngredient[][] ingredients) {
            this.name = name;
            this.output = output;
            this.ingredients = ingredients;
        }

        @Override
        public void apply() {
            ItemStack outputStack = CraftTweakerMC.getItemStack(output);

            // Determine pattern dimensions
            int height = ingredients.length;
            int width = 0;
            for (IIngredient[] row : ingredients) {
                width = Math.max(width, row.length);
            }

            // Build pattern strings and key map
            List<String> pattern = new ArrayList<>();
            Map<String, Ingredient> key = new HashMap<>();
            int keyIndex = 0;

            for (int y = 0; y < height; y++) {
                StringBuilder sb = new StringBuilder();
                for (int x = 0; x < width; x++) {
                    if (x < ingredients[y].length && ingredients[y][x] != null) {
                        IIngredient ctIngredient = ingredients[y][x];
                        String symbol = String.valueOf((char) ('A' + keyIndex));
                        key.put(symbol, CraftTweakerMC.getIngredient(ctIngredient));
                        keyIndex++;
                        sb.append(symbol);
                    } else {
                        sb.append(' ');
                    }
                }
                pattern.add(sb.toString());
            }

            SupremeShapedPattern shapedPattern = SupremeShapedPattern.parse(key, pattern);
            SupremeShapedRecipe recipe = new SupremeShapedRecipe("", shapedPattern, outputStack);
            recipe.setRegistryName(new ResourceLocation(Tags.MOD_ID, name));

            ForgeRegistries.RECIPES.register(recipe);
            CraftTweakerAPI.logInfo("Registered supreme shaped recipe: " + name);
        }

        @Override
        public String describe() {
            return "Adding supreme shaped recipe: " + name + " -> " + output.getDisplayName();
        }
    }

    private static class AddShapelessRecipe implements IAction {
        private final String name;
        private final IItemStack output;
        private final IIngredient[] ingredients;

        AddShapelessRecipe(String name, IItemStack output, IIngredient[] ingredients) {
            this.name = name;
            this.output = output;
            this.ingredients = ingredients;
        }

        @Override
        public void apply() {
            ItemStack outputStack = CraftTweakerMC.getItemStack(output);
            NonNullList<Ingredient> ingredientList = NonNullList.create();
            for (IIngredient ctIngredient : ingredients) {
                ingredientList.add(CraftTweakerMC.getIngredient(ctIngredient));
            }

            SupremeShapelessRecipe recipe = new SupremeShapelessRecipe("", ingredientList, outputStack);
            recipe.setRegistryName(new ResourceLocation(Tags.MOD_ID, name));

            ForgeRegistries.RECIPES.register(recipe);
            CraftTweakerAPI.logInfo("Registered supreme shapeless recipe: " + name);
        }

        @Override
        public String describe() {
            return "Adding supreme shapeless recipe: " + name + " -> " + output.getDisplayName();
        }
    }

    private static class RemoveRecipe implements IAction {
        private final IItemStack output;

        RemoveRecipe(IItemStack output) {
            this.output = output;
        }

        @Override
        public void apply() {
            ItemStack target = CraftTweakerMC.getItemStack(output);
            ResourceLocation targetId = null;

            for (IRecipe recipe : ForgeRegistries.RECIPES) {
                if (recipe.getRegistryName() != null
                        && recipe.getRegistryName().getNamespace().equals(Tags.MOD_ID)
                        && recipe.getRecipeOutput().isItemEqual(target)) {
                    targetId = recipe.getRegistryName();
                    break;
                }
            }

            if (targetId != null) {
                ForgeRegistry<IRecipe> registry = (ForgeRegistry<IRecipe>) ForgeRegistries.RECIPES;
                registry.remove(targetId);
                CraftTweakerAPI.logInfo("Removed supreme recipe for: " + target.getDisplayName());
            } else {
                CraftTweakerAPI.logWarning("No supreme recipe found for: " + target.getDisplayName());
            }
        }

        @Override
        public String describe() {
            return "Removing supreme recipe for: " + output.getDisplayName();
        }
    }

    private static class RemoveRecipeByName implements IAction {
        private final String name;

        RemoveRecipeByName(String name) {
            this.name = name;
        }

        @Override
        public void apply() {
            ResourceLocation id = new ResourceLocation(name);
            ForgeRegistry<IRecipe> registry = (ForgeRegistry<IRecipe>) ForgeRegistries.RECIPES;
            if (registry.containsKey(id)) {
                registry.remove(id);
                CraftTweakerAPI.logInfo("Removed supreme recipe: " + name);
            } else {
                CraftTweakerAPI.logWarning("No supreme recipe found: " + name);
            }
        }

        @Override
        public String describe() {
            return "Removing supreme recipe: " + name;
        }
    }

    private static class RemoveAllRecipes implements IAction {
        @Override
        public void apply() {
            ForgeRegistry<IRecipe> registry = (ForgeRegistry<IRecipe>) ForgeRegistries.RECIPES;
            List<ResourceLocation> toRemove = new ArrayList<>();
            for (IRecipe recipe : registry) {
                if (recipe.getRegistryName() != null
                        && recipe.getRegistryName().getNamespace().equals(Tags.MOD_ID)) {
                    toRemove.add(recipe.getRegistryName());
                }
            }
            for (ResourceLocation id : toRemove) {
                registry.remove(id);
            }
            CraftTweakerAPI.logInfo("Removed " + toRemove.size() + " supreme recipes");
        }

        @Override
        public String describe() {
            return "Removing all supreme recipes";
        }
    }
}
