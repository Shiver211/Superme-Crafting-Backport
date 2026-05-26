package com.shiver.supermecrafting.client;

import com.shiver.supermecrafting.SupremeCrafting;
import com.shiver.supermecrafting.ae2.AE2OptionalBridge;
import com.shiver.supermecrafting.block.BlockSupremeFurnaceCasing;
import com.shiver.supermecrafting.registry.SCRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Method;

@Mod.EventBusSubscriber(modid = SupremeCrafting.MOD_ID)
public final class ClientRegistry {
    private ClientRegistry() {
    }

    public static void preInit() {
    }

    @SubscribeEvent
    public static void models(ModelRegistryEvent event) {
        block(SCRegistry.SUPREME_TABLE);
        furnaceBlock(SCRegistry.SUPREME_FURNACE_CASING);
        furnaceBlock(SCRegistry.SUPREME_FURNACE_INPUT_HATCH);
        furnaceBlock(SCRegistry.SUPREME_FURNACE_OUTPUT_HATCH);
        furnaceBlock(SCRegistry.SUPREME_FURNACE_FUEL_HATCH);
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
        ae2Models();
    }

    public static void registerAe2Models(Block terminal, Block iface, Block assembler, Item pattern) {
        block(terminal);
        block(iface);
        block(assembler);
        item(pattern);
    }

    private static void block(Block block) {
        item(Item.getItemFromBlock(block));
    }

    private static void furnaceBlock(Block block) {
        block(block);
    }

    private static void item(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0,
                new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

    private static void ae2Models() {
        if (!AE2OptionalBridge.loaded()) {
            return;
        }
        try {
            Class<?> bridge = Class.forName("com.shiver.supermecrafting.ae2.AE2RegistryBridge");
            Method method = bridge.getMethod("registerModels");
            method.invoke(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to register AE2 models", e);
        }
    }

    @SubscribeEvent
    public static void bake(ModelBakeEvent event) {
        wrapFurnaceModels(event, SCRegistry.SUPREME_FURNACE_CASING);
        wrapFurnaceModels(event, SCRegistry.SUPREME_FURNACE_INPUT_HATCH);
        wrapFurnaceModels(event, SCRegistry.SUPREME_FURNACE_OUTPUT_HATCH);
        wrapFurnaceModels(event, SCRegistry.SUPREME_FURNACE_FUEL_HATCH);
    }

    @SubscribeEvent
    public static void textures(TextureStitchEvent.Pre event) {
        event.getMap().registerSprite(new ResourceLocation(SupremeCrafting.MOD_ID, "blocks/furnace_top"));
        event.getMap().registerSprite(new ResourceLocation(SupremeCrafting.MOD_ID, "blocks/furnace_side"));
        event.getMap().registerSprite(new ResourceLocation(SupremeCrafting.MOD_ID, "blocks/furnace_front_off"));
        event.getMap().registerSprite(new ResourceLocation(SupremeCrafting.MOD_ID, "blocks/furnace_front_on"));
        event.getMap().registerSprite(new ResourceLocation(SupremeCrafting.MOD_ID, "blocks/coal_block"));
    }

    private static void wrapFurnaceModels(ModelBakeEvent event, Block block) {
        ResourceLocation name = block.getRegistryName();
        for (IBlockState state : block.getBlockState().getValidStates()) {
            if (!state.getValue(BlockSupremeFurnaceCasing.FORMED)) continue;
            String variant = "formed=true,front=" + state.getValue(BlockSupremeFurnaceCasing.FRONT).getName2();
            ModelResourceLocation location = new ModelResourceLocation(name, variant);
            IBakedModel model = event.getModelRegistry().getObject(location);
            if (model != null) {
                event.getModelRegistry().putObject(location, new FormedCasingModel(model));
            }
        }
    }
}
