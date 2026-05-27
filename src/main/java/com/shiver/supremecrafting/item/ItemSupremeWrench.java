package com.shiver.supremecrafting.item;

import com.shiver.supremecrafting.recipe.SupremeRecipeScriptExporter;
import com.shiver.supremecrafting.table.TileSupremeTable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class ItemSupremeWrench extends Item {
    public ItemSupremeWrench() {
        setMaxStackSize(1);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                      EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return EnumActionResult.SUCCESS;
        }
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileSupremeTable)) {
            return EnumActionResult.PASS;
        }
        if (!player.capabilities.isCreativeMode) {
            player.sendMessage(new TextComponentTranslation("supreme_crafting.message.script_export.creative_only"));
            return EnumActionResult.SUCCESS;
        }
        ItemStack output = player.getHeldItemOffhand();
        if (output.isEmpty()) {
            player.sendMessage(new TextComponentTranslation("supreme_crafting.message.script_export.no_output"));
            return EnumActionResult.SUCCESS;
        }
        try {
            String file = SupremeRecipeScriptExporter.export((TileSupremeTable) tile, output);
            player.sendMessage(new TextComponentTranslation("supreme_crafting.message.script_export.success", file));
        } catch (SupremeRecipeScriptExporter.ExportException e) {
            player.sendMessage(new TextComponentTranslation(e.key(), e.args()));
        }
        return EnumActionResult.SUCCESS;
    }
}
