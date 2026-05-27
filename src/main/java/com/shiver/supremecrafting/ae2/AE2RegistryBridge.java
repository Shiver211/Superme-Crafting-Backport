package com.shiver.supremecrafting.ae2;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;

public final class AE2RegistryBridge {
    private AE2RegistryBridge() {
    }

    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        AE2Module.registerBlocks(event);
    }

    public static void registerItems(RegistryEvent.Register<Item> event) {
        AE2Module.registerItems(event);
    }

    public static void registerTileEntities() {
        AE2Module.registerTileEntities();
    }

    public static void registerModels() {
        AE2Module.init();
        com.shiver.supremecrafting.client.ClientRegistry.registerAe2Models(
                AE2Module.SUPREME_PATTERN_TERMINAL,
                AE2Module.SUPREME_INTERFACE,
                AE2Module.SUPREME_ASSEMBLER,
                AE2Module.SUPREME_PATTERN);
    }
}
