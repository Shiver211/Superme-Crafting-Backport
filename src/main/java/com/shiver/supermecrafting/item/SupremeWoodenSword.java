package com.shiver.supermecrafting.item;

import com.google.common.collect.Multimap;
import com.shiver.supermecrafting.Tags;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

import java.util.UUID;

/** 原版木剑属性 + 100 格触达距离 + 5x 模型。 */
public class SupremeWoodenSword extends ItemSword {
    private static final UUID REACH_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567801");

    public SupremeWoodenSword() {
        super(ToolMaterial.WOOD);
        setRegistryName(Tags.MOD_ID, "supreme_wooden_sword");
        setTranslationKey(Tags.MOD_ID + ".supreme_wooden_sword");
        setCreativeTab(CreativeTabs.COMBAT);
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
