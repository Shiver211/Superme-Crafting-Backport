package com.shiver.supermecrafting.item;

import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraftforge.common.util.EnumHelper;

public final class SupremeToolMaterial {
    public static final ToolMaterial INSTANCE = EnumHelper.addToolMaterial(
            "SUPREME_WOOD", 0, 59, 2.0F, 0.0F, 15);

    private SupremeToolMaterial() {
    }
}
