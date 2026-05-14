package com.shiver.supermecrafting.item;

import com.shiver.supermecrafting.Tags;
import com.shiver.supermecrafting.furnace.FurnaceBombEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 像雪球一样投掷；命中时，弹丸摧毁并 void 掉 Supreme Furnace 大小的体积
 * （32 / 64 / 128）中的每个方块，并尝试在那里形成熔炉。
 */
public class SupremeFurnaceBombItem extends Item {
    private final int bombSize;

    public SupremeFurnaceBombItem(String name, int bombSize) {
        this.bombSize = bombSize;
        setRegistryName(Tags.MOD_ID, name);
        setTranslationKey(Tags.MOD_ID + "." + name);
        setCreativeTab(CreativeTabs.COMBAT);
        setMaxStackSize(64);
    }

    public int getBombSize() {
        return bombSize;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }

        world.playSound(null, player.posX, player.posY, player.posZ,
                SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL,
                0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

        if (!world.isRemote) {
            FurnaceBombEntity bomb = new FurnaceBombEntity(world, player);
            bomb.setBombSize(bombSize);
            bomb.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.5F, 1.0F);
            world.spawnEntity(bomb);
        }

        player.addStat(StatList.getObjectUseStats(this));
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add("");
        tooltip.add(TextFormatting.GOLD + "Builds a " + bombSize + "³ Supreme Furnace");
        tooltip.add(TextFormatting.GOLD + "where it lands.");
        tooltip.add("");
        tooltip.add(TextFormatting.DARK_RED + "" + TextFormatting.BOLD + "⚠ DESTROYS AND VOIDS BLOCKS ⚠");
        tooltip.add(TextFormatting.RED + "Every block inside the target cube is");
        tooltip.add(TextFormatting.RED + "erased without drops, including chests,");
        tooltip.add(TextFormatting.RED + "spawners, and anything else in the way.");
        tooltip.add("");
    }
}
