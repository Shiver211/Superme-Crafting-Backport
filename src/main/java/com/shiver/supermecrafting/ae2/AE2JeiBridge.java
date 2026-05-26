package com.shiver.supermecrafting.ae2;

import mezz.jei.api.IModRegistry;
import net.minecraft.item.ItemStack;

public final class AE2JeiBridge {
    private AE2JeiBridge() {
    }

    public static void register(IModRegistry registry, String uid) {
        AE2Module.init();
        registry.addRecipeCatalyst(new ItemStack(AE2Module.SUPREME_PATTERN_TERMINAL), uid);
        registry.getRecipeTransferRegistry()
                .addRecipeTransferHandler(new SupremePatternTerminalJeiTransferHandler(), uid);
    }
}
