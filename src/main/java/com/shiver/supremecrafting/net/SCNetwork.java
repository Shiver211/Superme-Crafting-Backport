package com.shiver.supremecrafting.net;

import com.shiver.supremecrafting.SupremeCrafting;
import com.shiver.supremecrafting.ae2.AE2OptionalBridge;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.lang.reflect.Method;

public final class SCNetwork {
    public static SimpleNetworkWrapper CHANNEL;

    private SCNetwork() {
    }

    public static void init() {
        CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(SupremeCrafting.MOD_ID);
        CHANNEL.registerMessage(PacketTransferRecipe.Handler.class, PacketTransferRecipe.class, 0, Side.SERVER);
        CHANNEL.registerMessage(PacketMultiblockSync.Handler.class, PacketMultiblockSync.class, 1, Side.CLIENT);
        CHANNEL.registerMessage(PacketReturnTableItems.Handler.class, PacketReturnTableItems.class, 2, Side.SERVER);
        ae2Messages();
    }

    private static void ae2Messages() {
        if (!AE2OptionalBridge.loaded()) {
            return;
        }
        try {
            Class<?> bridge = Class.forName("com.shiver.supremecrafting.ae2.AE2NetworkBridge");
            Method method = bridge.getMethod("register", SimpleNetworkWrapper.class, int.class);
            method.invoke(null, CHANNEL, 3);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to register AE2 packets", e);
        }
    }
}
