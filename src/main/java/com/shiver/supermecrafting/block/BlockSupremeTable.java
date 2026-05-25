package com.shiver.supermecrafting.block;

import com.shiver.supermecrafting.GuiIds;
import com.shiver.supermecrafting.SupremeCrafting;
import com.shiver.supermecrafting.registry.SCCreativeTab;
import com.shiver.supermecrafting.table.TileSupremeTable;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockSupremeTable extends Block implements ITileEntityProvider {
    public BlockSupremeTable() {
        super(Material.WOOD);
        setRegistryName(SupremeCrafting.MOD_ID, "supreme_table");
        setTranslationKey(SupremeCrafting.MOD_ID + ".supreme_table");
        setCreativeTab(SCCreativeTab.TAB);
        setHardness(2.5F);
        setResistance(3.0F);
        setSoundType(SoundType.WOOD);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileSupremeTable();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(SupremeCrafting.INSTANCE, GuiIds.SUPREME_TABLE, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileSupremeTable) {
            ((TileSupremeTable) te).dropContents(world, pos);
        }
        super.breakBlock(world, pos, state);
    }
}
