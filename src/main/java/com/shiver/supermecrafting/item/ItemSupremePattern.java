package com.shiver.supermecrafting.item;

import com.shiver.supermecrafting.ae2.SupremePatternData;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemSupremePattern extends Item {
    public ItemSupremePattern() {
        setMaxStackSize(64);
        addPropertyOverride(new ResourceLocation("encoded"),
                (stack, worldIn, entityIn) -> SupremePatternData.isEncoded(stack) ? 1.0F : 0.0F);
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return SupremePatternData.isEncoded(stack)
                ? super.getTranslationKey(stack) + ".encoded"
                : super.getTranslationKey(stack);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (!playerIn.isSneaking() || !SupremePatternData.isEncoded(stack)) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }
        if (!worldIn.isRemote) {
            NBTTagCompound tag = stack.getTagCompound();
            tag.removeTag(SupremePatternData.TAG_ENCODED);
            if (tag.isEmpty()) {
                stack.setTagCompound(null);
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if (SupremePatternData.isEncoded(stack)) {
            boolean valid = worldIn == null || SupremePatternData.isRecipeValid(stack, worldIn);
            if (!valid) {
                stack.setStackDisplayName(TextFormatting.RED + I18n.translateToLocal("item.supreme_crafting.supreme_pattern.invalid.name"));
            } else if (stack.hasDisplayName()) {
                stack.removeSubCompound("display");
            }
            tooltip.add(SupremePatternData.readRecipeName(stack));
            ItemStack output = SupremePatternData.readOutput(stack);
            if (!output.isEmpty()) {
                tooltip.add(output.getDisplayName() + " x" + output.getCount());
            }
        }
    }
}
