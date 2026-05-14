package com.shiver.supermecrafting.item;

import com.google.common.collect.Multimap;
import com.shiver.supermecrafting.Tags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemAxe;

import java.util.UUID;

/** 原版木斧属性 + 100 格触达距离。 */
public class SupremeWoodenAxe extends ItemAxe {
    private static final UUID REACH_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567803");

    public SupremeWoodenAxe() {
        super(ToolMaterial.WOOD, 6.0F, -3.2F);
        setRegistryName(Tags.MOD_ID, "supreme_wooden_axe");
        setTranslationKey(Tags.MOD_ID + ".supreme_wooden_axe");
        setCreativeTab(CreativeTabs.TOOLS);
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        Multimap<String, AttributeModifier> modifiers = super.getAttributeModifiers(slot, stack);
        if (slot == EntityEquipmentSlot.MAINHAND) {
            SupremeToolAttributes.addReachModifier(modifiers, REACH_UUID);
        }
        return modifiers;
    }
}
