package com.shiver.supremecrafting.table;

import com.shiver.supremecrafting.recipe.SupremeCraftingMatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;

public class SupremeResultSlot extends SlotCrafting {
    private final TileSupremeTable table;

    public SupremeResultSlot(EntityPlayer player, TileSupremeTable table, InventoryCraftResult result, int index, int x, int y) {
        super(player, null, result, index, x, y);
        this.table = table;
    }

    @Override
    public ItemStack onTake(EntityPlayer player, ItemStack stack) {
        SupremeCraftingMatcher.consume(table.supremeInventory(), player.world);
        table.markDirty();
        return stack;
    }
}
