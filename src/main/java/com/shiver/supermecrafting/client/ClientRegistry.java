package com.shiver.supermecrafting.client;

import com.shiver.supermecrafting.SupremeCrafting;
import com.shiver.supermecrafting.registry.SCRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = SupremeCrafting.MOD_ID)
public final class ClientRegistry {
    private ClientRegistry() {
    }

    public static void preInit() {
    }

    @SubscribeEvent
    public static void models(ModelRegistryEvent event) {
        block(SCRegistry.SUPREME_TABLE);
        block(SCRegistry.SUPREME_FURNACE_CASING);
        block(SCRegistry.SUPREME_FURNACE_INPUT_HATCH);
        block(SCRegistry.SUPREME_FURNACE_OUTPUT_HATCH);
        block(SCRegistry.SUPREME_FURNACE_FUEL_HATCH);
        item(SCRegistry.SUPREME_WRENCH);
        item(SCRegistry.SUPREME_FURNACE_TERMINAL);
        item(SCRegistry.SUPREME_FURNACE_BOMB_T1);
        item(SCRegistry.SUPREME_FURNACE_BOMB_T2);
        item(SCRegistry.SUPREME_FURNACE_BOMB_T3);
        item(SCRegistry.FURNACE_DESTROYER);
        item(SCRegistry.SUPREME_WOODEN_SWORD);
        item(SCRegistry.SUPREME_WOODEN_PICKAXE);
        item(SCRegistry.SUPREME_WOODEN_AXE);
        item(SCRegistry.SUPREME_WOODEN_SHOVEL);
        item(SCRegistry.SUPREME_WOODEN_HOE);
    }

    private static void block(Block block) {
        item(Item.getItemFromBlock(block));
    }

    private static void item(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}
