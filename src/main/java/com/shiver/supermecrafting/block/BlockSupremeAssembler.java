package com.shiver.supermecrafting.block;

import com.shiver.supermecrafting.SupremeCrafting;
import com.shiver.supermecrafting.ae2.TileSupremeAssembler;
import com.shiver.supermecrafting.registry.SCCreativeTab;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockSupremeAssembler extends Block implements ITileEntityProvider {
    public BlockSupremeAssembler() {
        super(Material.IRON);
        setRegistryName(SupremeCrafting.MOD_ID, "supreme_assembler");
        setTranslationKey(SupremeCrafting.MOD_ID + ".supreme_assembler");
        setHardness(2.0F);
        setCreativeTab(SCCreativeTab.TAB);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileSupremeAssembler();
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileSupremeAssembler) {
            ((TileSupremeAssembler) tile).dropContents();
        }
        super.breakBlock(worldIn, pos, state);
    }
}
