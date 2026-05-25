package com.shiver.supermecrafting.item;

import com.shiver.supermecrafting.furnace.EntityFurnaceBomb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemSupremeFurnaceBomb extends Item {
    private final int size;

    public ItemSupremeFurnaceBomb(int size) {
        this.size = size;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        if (!worldIn.isRemote) {
            EntityFurnaceBomb bomb = new EntityFurnaceBomb(worldIn, playerIn, size);
            bomb.shoot(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, 1.5F, 1.0F);
            worldIn.spawnEntity(bomb);
        }
        if (!playerIn.capabilities.isCreativeMode) stack.shrink(1);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }
}
