package com.shiver.supermecrafting.block;

import com.shiver.supermecrafting.SupremeCrafting;
import com.shiver.supermecrafting.GuiIds;
import com.shiver.supermecrafting.ae2.TileSupremeInterface;
import com.shiver.supermecrafting.registry.SCCreativeTab;
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

public class BlockSupremeInterface extends Block implements ITileEntityProvider {
    public BlockSupremeInterface() {
        super(Material.IRON);
        setRegistryName(SupremeCrafting.MOD_ID, "supreme_interface");
        setTranslationKey(SupremeCrafting.MOD_ID + ".supreme_interface");
        setHardness(2.0F);
        setCreativeTab(SCCreativeTab.TAB);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileSupremeInterface();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            playerIn.openGui(SupremeCrafting.INSTANCE, GuiIds.SUPREME_INTERFACE,
                    worldIn, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileSupremeInterface) {
            ((TileSupremeInterface) tile).dropContents();
        }
        super.breakBlock(worldIn, pos, state);
    }
}
