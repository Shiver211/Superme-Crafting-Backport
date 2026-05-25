package com.shiver.supermecrafting.net;

import com.shiver.supermecrafting.SupremeCrafting;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class SCNetwork {
    public static SimpleNetworkWrapper CHANNEL;

    private SCNetwork() {
    }

    public static void init() {
        CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(SupremeCrafting.MOD_ID);
        CHANNEL.registerMessage(PacketTransferRecipe.Handler.class, PacketTransferRecipe.class, 0, Side.SERVER);
        CHANNEL.registerMessage(PacketMultiblockSync.Handler.class, PacketMultiblockSync.class, 1, Side.CLIENT);
    }
}
