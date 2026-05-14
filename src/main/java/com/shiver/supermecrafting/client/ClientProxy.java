package com.shiver.supermecrafting.client;

import com.shiver.supermecrafting.CommonProxy;
import com.shiver.supermecrafting.table.SupremeTableTileEntity;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        // 注册 Supreme Table 的 TileEntitySpecialRenderer（抖动/摇摆动画）
        ClientRegistry.bindTileEntitySpecialRenderer(SupremeTableTileEntity.class, new SupremeTableRenderer());
    }
}
