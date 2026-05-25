package com.shiver.supermecrafting.furnace;

import com.shiver.supermecrafting.net.PacketMultiblockSync;
import com.shiver.supermecrafting.net.SCNetwork;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public class MultiblockSync {
    public static void add(World world, Region region) {
        SCNetwork.CHANNEL.sendToAllAround(PacketMultiblockSync.add(region), point(world, region));
    }

    public static void remove(World world, Region region) {
        SCNetwork.CHANNEL.sendToAllAround(PacketMultiblockSync.remove(region), point(world, region));
    }

    @SubscribeEvent
    public void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            SCNetwork.CHANNEL.sendTo(PacketMultiblockSync.reset(MultiblockRegions.get(player.world).all()), player);
        }
    }

    @SubscribeEvent
    public void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            SCNetwork.CHANNEL.sendTo(PacketMultiblockSync.reset(MultiblockRegions.get(player.world).all()), player);
        }
    }

    private static NetworkRegistry.TargetPoint point(World world, Region region) {
        double x = (region.min().getX() + region.max().getX()) * 0.5D;
        double y = (region.min().getY() + region.max().getY()) * 0.5D;
        double z = (region.min().getZ() + region.max().getZ()) * 0.5D;
        return new NetworkRegistry.TargetPoint(world.provider.getDimension(), x, y, z, region.size() + 128.0D);
    }
}
