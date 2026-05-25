package com.shiver.supermecrafting.client;

import com.shiver.supermecrafting.CommonProxy;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit() {
        ClientRegistry.preInit();
    }
}
