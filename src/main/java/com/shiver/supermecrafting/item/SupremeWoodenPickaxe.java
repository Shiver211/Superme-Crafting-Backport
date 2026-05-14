package com.shiver.supermecrafting.item;

import com.google.common.collect.Multimap;
import com.shiver.supermecrafting.Tags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemPickaxe;

import java.util.UUID;

/** 原版木镐属性 + 100 格触达距离。 */
public class SupremeWoodenPickaxe extends ItemPickaxe {
    private static final UUID REACH_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567802");

    public SupremeWoodenPickaxe() {
        super(ToolMaterial.WOOD);
        setRegistryName(Tags.MOD_ID, "supreme_wooden_pickaxe");
        setTranslationKey(Tags.MOD_ID + ".supreme_wooden_pickaxe");
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
