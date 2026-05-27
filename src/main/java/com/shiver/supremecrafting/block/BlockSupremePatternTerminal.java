package com.shiver.supremecrafting.block;

import com.shiver.supremecrafting.GuiIds;
import com.shiver.supremecrafting.SupremeCrafting;
import com.shiver.supremecrafting.ae2.TileSupremePatternTerminal;
import com.shiver.supremecrafting.registry.SCCreativeTab;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockSupremePatternTerminal extends Block implements ITileEntityProvider {
    public BlockSupremePatternTerminal() {
        super(Material.IRON);
        setRegistryName(SupremeCrafting.MOD_ID, "supreme_pattern_terminal");
        setTranslationKey(SupremeCrafting.MOD_ID + ".supreme_pattern_terminal");
        setHardness(2.0F);
        setCreativeTab(SCCreativeTab.TAB);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileSupremePatternTerminal();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            playerIn.openGui(SupremeCrafting.INSTANCE, GuiIds.SUPREME_PATTERN_TERMINAL,
                    worldIn, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileSupremePatternTerminal) {
            ((TileSupremePatternTerminal) tile).dropContents();
        }
        super.breakBlock(worldIn, pos, state);
    }
}
