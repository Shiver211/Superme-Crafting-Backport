package com.shiver.supermecrafting.furnace;

import com.shiver.supermecrafting.Tags;
import com.shiver.supermecrafting.registry.ModBlocks;
import com.shiver.supermecrafting.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Casing block for the Supreme Furnace multiblock.
 * One block type for the whole structure — FORMED property toggles between
 * unformed and formed states.
 */
public class SupremeFurnaceCasingBlock extends Block {
    public static final PropertyBool FORMED = PropertyBool.create("formed");

    public SupremeFurnaceCasingBlock() {
        this("supreme_furnace_casing");
    }

    public SupremeFurnaceCasingBlock(String name) {
        super(Material.IRON);
        setRegistryName(Tags.MOD_ID, name);
        setTranslationKey(Tags.MOD_ID + "." + name);
        setHardness(5.0F);
        setResistance(10.0F);
        setCreativeTab(net.minecraft.creativetab.CreativeTabs.BUILDING_BLOCKS);
        setDefaultState(blockState.getBaseState().withProperty(FORMED, false));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FORMED);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FORMED, (meta & 1) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FORMED) ? 1 : 0;
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (!world.isRemote) {
            Region r = MultiblockRegions.get((net.minecraft.world.WorldServer) world).findContaining(pos);
            if (r != null) {
                player.sendMessage(new TextComponentString("Supreme Furnace disassembled."));
            }
            // Furnace Destroyer: seed cascade
            ItemStack held = player.getHeldItemMainhand();
            if (!held.isEmpty() && held.getItem() == ModItems.FURNACE_DESTROYER) {
                CasingCascade.seed((net.minecraft.world.WorldServer) world, pos);
            }
        }
        super.onBlockHarvested(world, pos, state, player);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            Region r = MultiblockRegions.get((net.minecraft.world.WorldServer) world).findContaining(pos);
            if (r != null) {
                FurnaceFormation.disassemble((net.minecraft.world.WorldServer) world, r);
            }
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                     EntityPlayer player, EnumHand hand, EnumFacing facing,
                                     float hitX, float hitY, float hitZ) {
        ItemStack held = player.getHeldItem(hand);
        if (held.isEmpty()) {
            // Open furnace GUI if formed
            if (!world.isRemote && world instanceof net.minecraft.world.WorldServer) {
                net.minecraft.world.WorldServer server = (net.minecraft.world.WorldServer) world;
                Region r = MultiblockRegions.get(server).findContaining(pos);
                if (r == null) {
                    player.sendMessage(new TextComponentString("Not part of a formed structure"));
                    return true;
                }
                player.openGui(Tags.MOD_ID, 1, world, pos.getX(), pos.getY(), pos.getZ());
            }
            return true;
        }

        // Wrench: try to form
        if (held.getItem() == ModItems.SUPREME_WRENCH) {
            if (world.isRemote) return true;
            if (!(world instanceof net.minecraft.world.WorldServer)) return false;

            FurnaceFormation.Result result = FurnaceFormation.tryForm(
                    (net.minecraft.world.WorldServer) world, pos, player);
            if (result instanceof FurnaceFormation.Result.Success) {
                FurnaceFormation.Result.Success s = (FurnaceFormation.Result.Success) result;
                player.sendMessage(new TextComponentString("Supreme Furnace formed: "
                        + s.region().size() + "x" + s.region().size() + "x" + s.region().size()
                        + " front=" + s.region().getFront()));
                return true;
            }
            if (result instanceof FurnaceFormation.Result.Failure) {
                FurnaceFormation.Result.Failure f = (FurnaceFormation.Result.Failure) result;
                player.sendMessage(new TextComponentString("Cannot form: " + f.reason()));
            }
            return false;
        }

        // Terminal: bind to furnace
        if (held.getItem() == ModItems.SUPREME_FURNACE_TERMINAL) {
            if (world.isRemote) return true;
            if (!(world instanceof net.minecraft.world.WorldServer)) return false;

            net.minecraft.world.WorldServer server = (net.minecraft.world.WorldServer) world;
            Region r = MultiblockRegions.get(server).findContaining(pos);
            if (r == null) {
                player.sendMessage(new TextComponentString("Not part of a formed Supreme Furnace."));
                return false;
            }
            // Store bound furnace data in NBT
            if (!held.hasTagCompound()) held.setTagCompound(new net.minecraft.nbt.NBTTagCompound());
            BoundFurnace bound = new BoundFurnace(world.provider.getDimension(), r.getId());
            held.getTagCompound().setTag("BoundFurnace", bound.save());
            player.sendMessage(new TextComponentString("Terminal bound: " + r.size() + "x" + r.size() + "x" + r.size()));
            return true;
        }

        return false;
    }
}
