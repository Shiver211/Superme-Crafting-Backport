package com.shiver.supremecrafting.block;

import com.shiver.supremecrafting.SupremeCrafting;
import com.shiver.supremecrafting.ae2.TileSupremeAssembler;
import com.shiver.supremecrafting.registry.SCCreativeTab;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockSupremeAssembler extends Block implements ITileEntityProvider {
    public BlockSupremeAssembler() {
        super(Material.IRON);
        setRegistryName(SupremeCrafting.MOD_ID, "supreme_assembler");
        setTranslationKey(SupremeCrafting.MOD_ID + ".supreme_assembler");
        setHardness(2.0F);
        setCreativeTab(SCCreativeTab.TAB);
        setLightOpacity(1);
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

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return layer == BlockRenderLayer.CUTOUT || layer == BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }
}
