package com.shiver.supermecrafting.item;

import com.shiver.supermecrafting.Tags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class SupremeWrenchItem extends Item {
    public SupremeWrenchItem() {
        setRegistryName(Tags.MOD_ID, "supreme_wrench");
        setTranslationKey(Tags.MOD_ID + ".supreme_wrench");
        setCreativeTab(CreativeTabs.TOOLS);
        setMaxStackSize(1);
    }
}
