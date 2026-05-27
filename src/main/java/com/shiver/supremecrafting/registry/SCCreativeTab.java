package com.shiver.supremecrafting.registry;

import com.shiver.supremecrafting.SupremeCrafting;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public final class SCCreativeTab {
    public static final CreativeTabs TAB = new CreativeTabs(SupremeCrafting.MOD_ID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(SCRegistry.SUPREME_TABLE);
        }
    };

    private SCCreativeTab() {
    }
}
