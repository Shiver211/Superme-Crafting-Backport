package com.shiver.supremecrafting;

public class CommonProxy {
    public void preInit() {
    }

    public void init() {
    }

    public void applyMultiblockSync(com.shiver.supremecrafting.net.PacketMultiblockSync packet) {
    }

    public com.shiver.supremecrafting.block.FurnaceRenderRegion furnaceRenderRegion(net.minecraft.world.IBlockAccess world,
                                                                                     net.minecraft.util.math.BlockPos pos,
                                                                                     net.minecraft.util.EnumFacing front) {
        return com.shiver.supremecrafting.block.FurnaceRenderRegion.fromWorld(world, pos, front);
    }
}
