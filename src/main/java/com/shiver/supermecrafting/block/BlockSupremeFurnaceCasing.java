package com.shiver.supermecrafting.block;

import com.shiver.supermecrafting.GuiIds;
import com.shiver.supermecrafting.SupremeCrafting;
import com.shiver.supermecrafting.furnace.CasingCascade;
import com.shiver.supermecrafting.furnace.FurnaceFormation;
import com.shiver.supermecrafting.furnace.MultiblockRegions;
import com.shiver.supermecrafting.furnace.Region;
import com.shiver.supermecrafting.registry.SCCreativeTab;
import com.shiver.supermecrafting.registry.SCRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;

public class BlockSupremeFurnaceCasing extends Block {
    public static final PropertyBool FORMED = PropertyBool.create("formed");
    public static final PropertyDirection FRONT = PropertyDirection.create("front", EnumFacing.Plane.HORIZONTAL);
    public static final FurnaceRenderRegionProperty RENDER_REGION = new FurnaceRenderRegionProperty();

    public BlockSupremeFurnaceCasing(String name) {
        super(Material.ROCK);
        setRegistryName(SupremeCrafting.MOD_ID, name);
        setTranslationKey(SupremeCrafting.MOD_ID + "." + name);
        setCreativeTab(SCCreativeTab.TAB);
        setHardness(3.0F);
        setResistance(6.0F);
        setSoundType(SoundType.STONE);
        setDefaultState(blockState.getBaseState().withProperty(FORMED, false).withProperty(FRONT, EnumFacing.NORTH));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new net.minecraft.block.properties.IProperty[]{FORMED, FRONT},
                new net.minecraftforge.common.property.IUnlistedProperty[]{RENDER_REGION});
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int front = state.getValue(FRONT).getHorizontalIndex();
        return (state.getValue(FORMED) ? 4 : 0) | front;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState()
                .withProperty(FORMED, (meta & 4) != 0)
                .withProperty(FRONT, EnumFacing.byHorizontalIndex(meta & 3));
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (!state.getValue(FORMED) || !(state instanceof IExtendedBlockState)) {
            return state;
        }
        return ((IExtendedBlockState) state).withProperty(RENDER_REGION,
                SupremeCrafting.proxy.furnaceRenderRegion(world, pos, state.getValue(FRONT)));
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (!stack.isEmpty() && stack.getItem() == SCRegistry.SUPREME_WRENCH) {
            if (!world.isRemote) {
                FurnaceFormation.Result result = FurnaceFormation.tryForm(world, pos, player);
                player.sendMessage(result.message());
            }
            return true;
        }
        if (!stack.isEmpty() && stack.getItem() == SCRegistry.SUPREME_FURNACE_TERMINAL) {
            if (!world.isRemote) {
                Region region = MultiblockRegions.get(world).findContaining(pos);
                if (region == null) {
                    player.sendMessage(new TextComponentTranslation("supreme_crafting.message.not_formed_furnace"));
                } else {
                    stack.getOrCreateSubCompound("supreme_crafting").setUniqueId("region", region.getId());
                    player.sendMessage(new TextComponentTranslation("supreme_crafting.message.terminal_bound"));
                }
            }
            return true;
        }
        if (!world.isRemote) {
            Region region = MultiblockRegions.get(world).findContaining(pos);
            if (region != null) {
                player.openGui(SupremeCrafting.INSTANCE, GuiIds.SUPREME_FURNACE, world, pos.getX(), pos.getY(), pos.getZ());
            } else {
                player.sendMessage(new TextComponentTranslation("supreme_crafting.message.not_formed_structure"));
            }
        }
        return true;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            Region region = MultiblockRegions.get(world).findContaining(pos);
            if (region != null) {
                FurnaceFormation.disassemble(world, region);
            }
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (!world.isRemote && player.getHeldItemMainhand().getItem() == SCRegistry.FURNACE_DESTROYER) {
            CasingCascade.destroyConnected(world, pos);
        }
        super.onBlockHarvested(world, pos, state, player);
    }
}
