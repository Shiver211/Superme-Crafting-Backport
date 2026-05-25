package com.shiver.supermecrafting.recipe;

import com.shiver.supermecrafting.SupremeCrafting;
import com.shiver.supermecrafting.table.SupremeTableInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

public abstract class SupremeRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
    private final ResourceLocation type;

    protected SupremeRecipe(String type) {
        this.type = new ResourceLocation(SupremeCrafting.MOD_ID, type);
    }

    public abstract boolean matches(SupremeTableInventory inventory, World world);

    public abstract NonNullList<net.minecraft.item.crafting.Ingredient> getSupremeIngredients();

    public abstract int getWidth();

    public abstract int getHeight();

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        return false;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width <= SupremeTableInventory.WIDTH && height <= SupremeTableInventory.HEIGHT;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        return getRecipeOutput().copy();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    public ResourceLocation type() {
        return type;
    }
}
