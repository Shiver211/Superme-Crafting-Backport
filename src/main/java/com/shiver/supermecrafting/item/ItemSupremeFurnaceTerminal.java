package com.shiver.supermecrafting.item;

import com.shiver.supermecrafting.GuiHandler;
import com.shiver.supermecrafting.GuiIds;
import com.shiver.supermecrafting.SupremeCrafting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import java.util.UUID;

public class ItemSupremeFurnaceTerminal extends Item {
    public ItemSupremeFurnaceTerminal() {
        setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        NBTTagCompound tag = stack.getSubCompound("supreme_crafting");
        if (!worldIn.isRemote && tag != null && tag.hasUniqueId("region")) {
            UUID id = tag.getUniqueId("region");
            playerIn.getEntityData().setLong(GuiHandler.TERMINAL_MOST, id.getMostSignificantBits());
            playerIn.getEntityData().setLong(GuiHandler.TERMINAL_LEAST, id.getLeastSignificantBits());
            playerIn.openGui(SupremeCrafting.INSTANCE, GuiIds.SUPREME_TERMINAL, worldIn, 0, 0, 0);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }
}
