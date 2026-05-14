package com.shiver.supermecrafting.item;

import com.shiver.supermecrafting.Tags;
import com.shiver.supermecrafting.furnace.SupremeFurnaceCasingBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 镐子，可 void 并级联传播。级联 + 掉落抑制实际上在
 * {@link SupremeFurnaceCasingBlock#onBlockHarvested} 中连接——
 * 在创造模式和生存模式下都会触发，在任何掉落发生之前。
 *
 * <p>此类仅自定义挖掘速度（对方块壳快速）和警告提示。
 */
public class FurnaceDestroyerItem extends ItemPickaxe {
    public FurnaceDestroyerItem() {
        super(ToolMaterial.IRON);
        setRegistryName(Tags.MOD_ID, "furnace_destroyer");
        setTranslationKey(Tags.MOD_ID + ".furnace_destroyer");
        setCreativeTab(CreativeTabs.TOOLS);
        setMaxStackSize(1);
        setMaxDamage(500);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        if (state.getBlock() instanceof SupremeFurnaceCasingBlock) {
            return 100.0f;
        }
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add("");
        tooltip.add(TextFormatting.DARK_RED + "" + TextFormatting.BOLD + "⚠ VOIDS MINED FURNACE BLOCKS ⚠");
        tooltip.add(TextFormatting.RED + "Furnace shell blocks break without");
        tooltip.add(TextFormatting.RED + "dropping, and the chain spreads to");
        tooltip.add(TextFormatting.RED + "every connected casing.");
        tooltip.add("");
    }
}
