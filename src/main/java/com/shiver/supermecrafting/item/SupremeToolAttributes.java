package com.shiver.supermecrafting.item;

import com.google.common.collect.Multimap;
import com.shiver.supermecrafting.Tags;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

import java.util.UUID;

/**
 * 共享辅助类，为原版工具的属性集添加 +97 方块/实体交互距离——
 * 产生约 100 格的有效触达距离。
 *
 * <p>每个 Supreme<Tool> 类提供自己的 {@code UUID} 用于修改器
 * （以便属性系统能正确去重每个物品的修改器）。
 */
public final class SupremeToolAttributes {
    /** 添加到触达距离属性的加成值。 */
    public static final double REACH_BONUS = 97.0;

    private SupremeToolAttributes() {}

    /**
     * 将触达距离修改器添加到给定的属性多重映射中。
     * 在 1.12.2 中，触达距离通过自定义属性 "generic.reachDistance" 控制。
     */
    public static void addReachModifier(Multimap<String, AttributeModifier> modifiers,
                                        UUID modifierId) {
        modifiers.put("generic.reachDistance",
                new AttributeModifier(modifierId, "supreme_reach", REACH_BONUS, 0));
    }
}
