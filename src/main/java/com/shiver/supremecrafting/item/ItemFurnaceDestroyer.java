package com.shiver.supremecrafting.item;

import com.shiver.supremecrafting.block.BlockSupremeFurnaceCasing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;

public class ItemFurnaceDestroyer extends ItemPickaxe {
    public ItemFurnaceDestroyer() {
        super(ToolMaterial.IRON);
        setMaxDamage(500);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        if (state.getBlock() instanceof BlockSupremeFurnaceCasing) {
            return 100.0F;
        }
        return super.getDestroySpeed(stack, state);
    }
}
