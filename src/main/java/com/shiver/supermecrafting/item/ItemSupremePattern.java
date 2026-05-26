package com.shiver.supermecrafting.item;

import com.shiver.supermecrafting.ae2.SupremePatternData;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemSupremePattern extends Item {
    public ItemSupremePattern() {
        setMaxStackSize(64);
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return SupremePatternData.isEncoded(stack)
                ? super.getTranslationKey(stack) + ".encoded"
                : super.getTranslationKey(stack);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (SupremePatternData.isEncoded(stack)) {
            tooltip.add(SupremePatternData.readRecipeName(stack));
            ItemStack output = SupremePatternData.readOutput(stack);
            if (!output.isEmpty()) {
                tooltip.add(output.getDisplayName() + " x" + output.getCount());
            }
        }
    }
}
