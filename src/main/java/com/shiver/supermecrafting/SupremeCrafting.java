package com.shiver.supermecrafting;

import com.shiver.supermecrafting.net.SCNetwork;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MOD_ID,
     name = Tags.MOD_NAME,
     version = Tags.VERSION,
     acceptedMinecraftVersions = "[1.12.2]")
public class SupremeCrafting {

    @Mod.Instance(Tags.MOD_ID)
    public static SupremeCrafting instance;

    private static final Logger LOGGER = LogManager.getLogger(Tags.MOD_ID);

    @SidedProxy(clientSide = "com.shiver.supermecrafting.client.ClientProxy",
                serverSide = "com.shiver.supermecrafting.server.ServerProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Initializing {} {}", Tags.MOD_NAME, Tags.VERSION);
        SCNetwork.register();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
        // CraftTweaker integration is handled automatically via @ZenRegister on SupremeCraftingTweaker
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
