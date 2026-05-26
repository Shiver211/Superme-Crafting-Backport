package com.shiver.supermecrafting.ae2;

import com.shiver.supermecrafting.net.PacketEncodeSupremePattern;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class AE2NetworkBridge {
    private AE2NetworkBridge() {
    }

    public static void register(SimpleNetworkWrapper channel, int id) {
        channel.registerMessage(PacketEncodeSupremePattern.Handler.class, PacketEncodeSupremePattern.class, id, Side.SERVER);
    }

    public static void sendEncode() {
        com.shiver.supermecrafting.net.SCNetwork.CHANNEL.sendToServer(new PacketEncodeSupremePattern());
    }
}
