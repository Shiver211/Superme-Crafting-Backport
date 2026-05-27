package com.shiver.supremecrafting.client;

import com.shiver.supremecrafting.CommonProxy;
import com.shiver.supremecrafting.block.FurnaceRenderRegion;
import com.shiver.supremecrafting.net.PacketMultiblockSync;
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
