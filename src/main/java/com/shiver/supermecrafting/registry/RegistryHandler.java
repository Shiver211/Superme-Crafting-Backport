package com.shiver.supermecrafting.registry;

import com.shiver.supermecrafting.Tags;
import com.shiver.supermecrafting.furnace.HatchRole;
import com.shiver.supermecrafting.furnace.SupremeFurnaceCasingBlock;
import com.shiver.supermecrafting.furnace.SupremeFurnaceHatchBlock;
import com.shiver.supermecrafting.item.*;
import com.shiver.supermecrafting.table.SupremeTableBlock;
import com.shiver.supermecrafting.table.SupremeTableTileEntity;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public class RegistryHandler {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
                // Supreme Table (already existed)
                new SupremeTableBlock()
                        .setRegistryName(Tags.MOD_ID, "supreme_table")
                        .setTranslationKey(Tags.MOD_ID + ".supreme_table")
                        .setHardness(2.5f)
                        .setResistance(3.0f)
                        .setCreativeTab(CreativeTabs.REDSTONE),

                // Supreme Furnace blocks
                new SupremeFurnaceCasingBlock(),
                new SupremeFurnaceHatchBlock("supreme_furnace_input_hatch", HatchRole.INPUT),
                new SupremeFurnaceHatchBlock("supreme_furnace_output_hatch", HatchRole.OUTPUT),
                new SupremeFurnaceHatchBlock("supreme_furnace_fuel_hatch", HatchRole.FUEL)
        );

        // Register TileEntities
        GameRegistry.registerTileEntity(SupremeTableTileEntity.class,
                new ResourceLocation(Tags.MOD_ID, "supreme_table"));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                // --- BlockItems ---
                new ItemBlock(ModBlocks.SUPREME_TABLE)
                        .setRegistryName(ModBlocks.SUPREME_TABLE.getRegistryName())
                        .setCreativeTab(CreativeTabs.REDSTONE),
                new ItemBlock(ModBlocks.SUPREME_FURNACE_CASING)
                        .setRegistryName(ModBlocks.SUPREME_FURNACE_CASING.getRegistryName())
                        .setCreativeTab(CreativeTabs.BUILDING_BLOCKS),
                new ItemBlock(ModBlocks.SUPREME_FURNACE_INPUT_HATCH)
                        .setRegistryName(ModBlocks.SUPREME_FURNACE_INPUT_HATCH.getRegistryName())
                        .setCreativeTab(CreativeTabs.BUILDING_BLOCKS),
                new ItemBlock(ModBlocks.SUPREME_FURNACE_OUTPUT_HATCH)
                        .setRegistryName(ModBlocks.SUPREME_FURNACE_OUTPUT_HATCH.getRegistryName())
                        .setCreativeTab(CreativeTabs.BUILDING_BLOCKS),
                new ItemBlock(ModBlocks.SUPREME_FURNACE_FUEL_HATCH)
                        .setRegistryName(ModBlocks.SUPREME_FURNACE_FUEL_HATCH.getRegistryName())
                        .setCreativeTab(CreativeTabs.BUILDING_BLOCKS),

                // --- Standalone Items ---
                new SupremeWrenchItem(),
                new SupremeFurnaceTerminalItem(),
                new FurnaceDestroyerItem(),
                new SupremeFurnaceBombItem("supreme_furnace_bomb_t1", 32),
                new SupremeFurnaceBombItem("supreme_furnace_bomb_t2", 64),
                new SupremeFurnaceBombItem("supreme_furnace_bomb_t3", 128),

                // --- Supreme Wooden Tools ---
                new SupremeWoodenSword(),
                new SupremeWoodenPickaxe(),
                new SupremeWoodenAxe(),
                new SupremeWoodenShovel(),
                new SupremeWoodenHoe()
        );
    }
}
