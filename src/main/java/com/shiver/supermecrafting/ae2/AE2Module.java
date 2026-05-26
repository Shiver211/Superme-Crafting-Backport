package com.shiver.supermecrafting.ae2;

import com.shiver.supermecrafting.SupremeCrafting;
import com.shiver.supermecrafting.block.BlockSupremeAssembler;
import com.shiver.supermecrafting.block.BlockSupremeInterface;
import com.shiver.supermecrafting.block.BlockSupremePatternTerminal;
import com.shiver.supermecrafting.item.ItemSupremePattern;
import com.shiver.supermecrafting.registry.SCCreativeTab;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class AE2Module {
    public static final String AE2_MOD_ID = "appliedenergistics2";

    public static BlockSupremePatternTerminal SUPREME_PATTERN_TERMINAL;
    public static BlockSupremeInterface SUPREME_INTERFACE;
    public static BlockSupremeAssembler SUPREME_ASSEMBLER;
    public static Item SUPREME_PATTERN;

    private AE2Module() {
    }

    public static boolean enabled() {
        return Loader.isModLoaded(AE2_MOD_ID);
    }

    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        if (enabled()) {
            init();
            event.getRegistry().registerAll(SUPREME_PATTERN_TERMINAL, SUPREME_INTERFACE, SUPREME_ASSEMBLER);
        }
    }

    public static void registerItems(RegistryEvent.Register<Item> event) {
        if (enabled()) {
            init();
            event.getRegistry().registerAll(
                    blockItem(SUPREME_PATTERN_TERMINAL),
                    blockItem(SUPREME_INTERFACE),
                    blockItem(SUPREME_ASSEMBLER),
                    SUPREME_PATTERN);
        }
    }

    public static void registerTileEntities() {
        if (enabled()) {
            init();
            GameRegistry.registerTileEntity(TileSupremePatternTerminal.class,
                    SupremeCrafting.MOD_ID + ":supreme_pattern_terminal");
            GameRegistry.registerTileEntity(TileSupremeInterface.class,
                    SupremeCrafting.MOD_ID + ":supreme_interface");
            GameRegistry.registerTileEntity(TileSupremeAssembler.class,
                    SupremeCrafting.MOD_ID + ":supreme_assembler");
        }
    }

    public static void init() {
        if (SUPREME_PATTERN_TERMINAL != null) {
            return;
        }
        SUPREME_PATTERN_TERMINAL = new BlockSupremePatternTerminal();
        SUPREME_INTERFACE = new BlockSupremeInterface();
        SUPREME_ASSEMBLER = new BlockSupremeAssembler();
        SUPREME_PATTERN = named(new ItemSupremePattern(), "supreme_pattern");
    }

    private static Item blockItem(Block block) {
        return new ItemBlock(block).setRegistryName(block.getRegistryName());
    }

    private static Item named(Item item, String name) {
        item.setRegistryName(SupremeCrafting.MOD_ID, name);
        item.setTranslationKey(SupremeCrafting.MOD_ID + "." + name);
        item.setCreativeTab(SCCreativeTab.TAB);
        return item;
    }
}
