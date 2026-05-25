package com.shiver.supermecrafting.registry;

import com.shiver.supermecrafting.SupremeCrafting;
import com.shiver.supermecrafting.furnace.EntityFurnaceBomb;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public final class SCEntities {
    private SCEntities() {
    }

    public static void register() {
        EntityRegistry.registerModEntity(new ResourceLocation(SupremeCrafting.MOD_ID, "furnace_bomb"),
                EntityFurnaceBomb.class, "furnace_bomb", 1, SupremeCrafting.INSTANCE, 64, 10, true);
    }
}
