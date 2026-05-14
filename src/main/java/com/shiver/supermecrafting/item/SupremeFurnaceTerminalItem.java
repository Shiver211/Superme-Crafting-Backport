package com.shiver.supermecrafting.item;

import com.shiver.supermecrafting.Tags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class SupremeFurnaceTerminalItem extends Item {
    public SupremeFurnaceTerminalItem() {
        setRegistryName(Tags.MOD_ID, "supreme_furnace_terminal");
        setTranslationKey(Tags.MOD_ID + ".supreme_furnace_terminal");
        setCreativeTab(CreativeTabs.TOOLS);
        setMaxStackSize(1);
    }
}
