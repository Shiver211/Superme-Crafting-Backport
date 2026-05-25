package com.shiver.supermecrafting;

import com.shiver.supermecrafting.client.ClientRegistry;
import com.shiver.supermecrafting.crafttweaker.SupremeCraftingTweaker;
import com.shiver.supermecrafting.furnace.FurnaceTickHandler;
import com.shiver.supermecrafting.net.SCNetwork;
import com.shiver.supermecrafting.registry.SCEntities;
import com.shiver.supermecrafting.registry.SCRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@Mod(modid = SupremeCrafting.MOD_ID, name = SupremeCrafting.NAME, version = SupremeCrafting.VERSION,
        acceptedMinecraftVersions = "[1.12.2]")
public class SupremeCrafting {
    public static final String MOD_ID = "supreme_crafting";
    public static final String NAME = "Superme Crafting Backport";
    public static final String VERSION = Tags.VERSION;

    @Mod.Instance(MOD_ID)
    public static SupremeCrafting INSTANCE;

    @SidedProxy(clientSide = "com.shiver.supermecrafting.client.ClientProxy",
            serverSide = "com.shiver.supermecrafting.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        SCNetwork.init();
        SCRegistry.registerTileEntities();
        SCEntities.register();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        MinecraftForge.EVENT_BUS.register(new FurnaceTickHandler());
        proxy.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        SupremeCraftingTweaker.init();
        proxy.init();
    }
}
