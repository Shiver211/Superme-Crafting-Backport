package com.shiver.supermecrafting;

public class CommonProxy {
    public void preInit() {
    }

    public void init() {
    }

    public void applyMultiblockSync(com.shiver.supermecrafting.net.PacketMultiblockSync packet) {
    }

    public com.shiver.supermecrafting.block.FurnaceRenderRegion furnaceRenderRegion(net.minecraft.world.IBlockAccess world,
                                                                                     net.minecraft.util.math.BlockPos pos,
                                                                                     net.minecraft.util.EnumFacing front) {
        return com.shiver.supermecrafting.block.FurnaceRenderRegion.fromWorld(world, pos, front);
    }
}
