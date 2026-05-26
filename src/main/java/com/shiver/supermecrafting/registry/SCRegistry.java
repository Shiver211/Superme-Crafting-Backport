package com.shiver.supermecrafting.registry;

import com.shiver.supermecrafting.SupremeCrafting;
import com.shiver.supermecrafting.ae2.AE2OptionalBridge;
import com.shiver.supermecrafting.block.BlockSupremeFurnaceCasing;
import com.shiver.supermecrafting.block.BlockSupremeFurnaceHatch;
import com.shiver.supermecrafting.block.BlockSupremeTable;
import com.shiver.supermecrafting.furnace.HatchRole;
import com.shiver.supermecrafting.furnace.TileFurnaceHatch;
import com.shiver.supermecrafting.item.ItemFurnaceDestroyer;
import com.shiver.supermecrafting.item.ItemSupremeFurnaceBomb;
import com.shiver.supermecrafting.item.ItemSupremeFurnaceTerminal;
import com.shiver.supermecrafting.item.ItemSupremeWrench;
import com.shiver.supermecrafting.table.TileSupremeTable;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.lang.reflect.Method;

@Mod.EventBusSubscriber(modid = SupremeCrafting.MOD_ID)
public final class SCRegistry {
    public static final BlockSupremeTable SUPREME_TABLE = new BlockSupremeTable();
    public static final BlockSupremeFurnaceCasing SUPREME_FURNACE_CASING = new BlockSupremeFurnaceCasing("supreme_furnace_casing");
    public static final BlockSupremeFurnaceHatch SUPREME_FURNACE_INPUT_HATCH =
            new BlockSupremeFurnaceHatch("supreme_furnace_input_hatch", HatchRole.INPUT);
    public static final BlockSupremeFurnaceHatch SUPREME_FURNACE_OUTPUT_HATCH =
            new BlockSupremeFurnaceHatch("supreme_furnace_output_hatch", HatchRole.OUTPUT);
    public static final BlockSupremeFurnaceHatch SUPREME_FURNACE_FUEL_HATCH =
            new BlockSupremeFurnaceHatch("supreme_furnace_fuel_hatch", HatchRole.FUEL);

    public static final Item SUPREME_WRENCH = named(new ItemSupremeWrench(), "supreme_wrench");
    public static final Item SUPREME_FURNACE_TERMINAL = named(new ItemSupremeFurnaceTerminal(), "supreme_furnace_terminal");
    public static final Item SUPREME_FURNACE_BOMB_T1 = named(new ItemSupremeFurnaceBomb(32), "supreme_furnace_bomb_t1");
    public static final Item SUPREME_FURNACE_BOMB_T2 = named(new ItemSupremeFurnaceBomb(64), "supreme_furnace_bomb_t2");
    public static final Item SUPREME_FURNACE_BOMB_T3 = named(new ItemSupremeFurnaceBomb(128), "supreme_furnace_bomb_t3");
    public static final Item FURNACE_DESTROYER = named(new ItemFurnaceDestroyer(), "furnace_destroyer");

    private SCRegistry() {
    }

    @SubscribeEvent
    public static void blocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(SUPREME_TABLE, SUPREME_FURNACE_CASING,
                SUPREME_FURNACE_INPUT_HATCH, SUPREME_FURNACE_OUTPUT_HATCH, SUPREME_FURNACE_FUEL_HATCH);
        ae2("registerBlocks", new Class<?>[] { RegistryEvent.Register.class }, event);
    }

    @SubscribeEvent
    public static void items(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                blockItem(SUPREME_TABLE),
                blockItem(SUPREME_FURNACE_CASING),
                blockItem(SUPREME_FURNACE_INPUT_HATCH),
                blockItem(SUPREME_FURNACE_OUTPUT_HATCH),
                blockItem(SUPREME_FURNACE_FUEL_HATCH),
                SUPREME_WRENCH, SUPREME_FURNACE_TERMINAL, SUPREME_FURNACE_BOMB_T1,
                SUPREME_FURNACE_BOMB_T2, SUPREME_FURNACE_BOMB_T3, FURNACE_DESTROYER);
        ae2("registerItems", new Class<?>[] { RegistryEvent.Register.class }, event);
    }

    public static void registerTileEntities() {
        GameRegistry.registerTileEntity(TileSupremeTable.class, SupremeCrafting.MOD_ID + ":supreme_table");
        GameRegistry.registerTileEntity(TileFurnaceHatch.class, SupremeCrafting.MOD_ID + ":supreme_furnace_hatch");
        ae2("registerTileEntities", new Class<?>[0]);
    }

    private static void ae2(String methodName, Class<?>[] types, Object... args) {
        if (!AE2OptionalBridge.loaded()) {
            return;
        }
        try {
            Class<?> bridge = Class.forName("com.shiver.supermecrafting.ae2.AE2RegistryBridge");
            Method method = bridge.getMethod(methodName, types);
            method.invoke(null, args);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to call AE2 registry bridge " + methodName, e);
        }
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
