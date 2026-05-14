package com.shiver.supermecrafting.net;

import com.shiver.supermecrafting.Tags;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class SCNetwork {

    public static SimpleNetworkWrapper INSTANCE;
    private static int id = 0;

    public static void register() {
        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Tags.MOD_ID);

        // Client -> Server: recipe transfer from JEI
        INSTANCE.registerMessage(
                TransferRecipePacket.Handler.class,
                TransferRecipePacket.class,
                id++, Side.SERVER);

        // Server -> Client: multiblock sync (reset/add/remove)
        INSTANCE.registerMessage(
                MultiblockSyncPacket.Handler.class,
                MultiblockSyncPacket.class,
                id++, Side.CLIENT);

        // Server -> Client: furnace lit state toggle
        INSTANCE.registerMessage(
                MultiblockLitPacket.Handler.class,
                MultiblockLitPacket.class,
                id++, Side.CLIENT);
    }
}
