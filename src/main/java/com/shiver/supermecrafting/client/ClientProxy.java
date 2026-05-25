package com.shiver.supermecrafting.client;

import com.shiver.supermecrafting.CommonProxy;
import com.shiver.supermecrafting.block.FurnaceRenderRegion;
import com.shiver.supermecrafting.net.PacketMultiblockSync;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit() {
        ClientRegistry.preInit();
    }

    @Override
    public void applyMultiblockSync(PacketMultiblockSync packet) {
        ClientMultiblockRegions.apply(packet);
    }

    @Override
    public FurnaceRenderRegion furnaceRenderRegion(IBlockAccess world, BlockPos pos, EnumFacing front) {
        return ClientMultiblockRegions.findOrCreate(world, pos, front);
    }
}
