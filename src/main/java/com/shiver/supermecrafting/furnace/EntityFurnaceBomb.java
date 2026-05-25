package com.shiver.supermecrafting.furnace;

import com.shiver.supermecrafting.registry.SCRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityFurnaceBomb extends EntityThrowable {
    private int size = 32;

    public EntityFurnaceBomb(World worldIn) {
        super(worldIn);
    }

    public EntityFurnaceBomb(World worldIn, EntityLivingBase throwerIn, int size) {
        super(worldIn, throwerIn);
        this.size = size;
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (!world.isRemote) {
            placeShellAndForm(new BlockPos(posX, posY, posZ));
            setDead();
        }
    }

    private void placeShellAndForm(BlockPos landing) {
        int half = size / 2;
        BlockPos min = new BlockPos(landing.getX() - half, landing.getY(), landing.getZ() - half);
        BlockPos max = new BlockPos(min.getX() + size - 1, min.getY() + size - 1, min.getZ() + size - 1);
        IBlockState casing = SCRegistry.SUPREME_FURNACE_CASING.getDefaultState();
        IBlockState air = Blocks.AIR.getDefaultState();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                forceSet(new BlockPos(x, min.getY(), z), casing);
                forceSet(new BlockPos(x, max.getY(), z), casing);
            }
        }
        for (int y = min.getY() + 1; y < max.getY(); y++) {
            for (int x = min.getX(); x <= max.getX(); x++) {
                forceSet(new BlockPos(x, y, min.getZ()), casing);
                forceSet(new BlockPos(x, y, max.getZ()), casing);
            }
            for (int z = min.getZ() + 1; z < max.getZ(); z++) {
                forceSet(new BlockPos(min.getX(), y, z), casing);
                forceSet(new BlockPos(max.getX(), y, z), casing);
            }
        }
        for (int x = min.getX() + 1; x < max.getX(); x++) {
            for (int y = min.getY() + 1; y < max.getY(); y++) {
                for (int z = min.getZ() + 1; z < max.getZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!world.isAirBlock(pos)) forceSet(pos, air);
                }
            }
        }
        FurnaceFormation.tryForm(world, landing, getThrower() instanceof net.minecraft.entity.player.EntityPlayer
                ? (net.minecraft.entity.player.EntityPlayer) getThrower() : null);
    }

    private void forceSet(BlockPos pos, IBlockState state) {
        net.minecraft.tileentity.TileEntity te = world.getTileEntity(pos);
        if (te instanceof net.minecraft.inventory.IInventory) {
            ((net.minecraft.inventory.IInventory) te).clear();
        }
        world.setBlockState(pos, state, 2);
    }
}
