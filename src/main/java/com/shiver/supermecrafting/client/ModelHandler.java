package com.shiver.supermecrafting.client;

import com.shiver.supermecrafting.Tags;
import com.shiver.supermecrafting.registry.ModBlocks;
import com.shiver.supermecrafting.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = Tags.MOD_ID, value = Side.CLIENT)
public class ModelHandler {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        // --- Block items ---
        regBlockItem(ModBlocks.SUPREME_TABLE);
        regBlockItem(ModBlocks.SUPREME_FURNACE_CASING);
        regBlockItem(ModBlocks.SUPREME_FURNACE_INPUT_HATCH);
        regBlockItem(ModBlocks.SUPREME_FURNACE_OUTPUT_HATCH);
        regBlockItem(ModBlocks.SUPREME_FURNACE_FUEL_HATCH);

        // --- Standalone items ---
        regItem(ModItems.SUPREME_WRENCH);
        regItem(ModItems.SUPREME_FURNACE_TERMINAL);
        regItem(ModItems.FURNACE_DESTROYER);
        regItem(ModItems.SUPREME_FURNACE_BOMB_T1);
        regItem(ModItems.SUPREME_FURNACE_BOMB_T2);
        regItem(ModItems.SUPREME_FURNACE_BOMB_T3);

        // --- Tools ---
        regItem(ModItems.SUPREME_WOODEN_SWORD);
        regItem(ModItems.SUPREME_WOODEN_PICKAXE);
        regItem(ModItems.SUPREME_WOODEN_AXE);
        regItem(ModItems.SUPREME_WOODEN_SHOVEL);
        regItem(ModItems.SUPREME_WOODEN_HOE);
    }

    private static void regItem(Item item) {
        if (item == null) return;
        ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

    private static void regBlockItem(Block block) {
        if (block == null) return;
        Item item = Item.getItemFromBlock(block);
        if (item != null) {
            ModelLoader.setCustomModelResourceLocation(item, 0,
                    new ModelResourceLocation(block.getRegistryName(), "inventory"));
        }
    }
}
