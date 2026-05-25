package com.shiver.supermecrafting.client;

import java.util.ArrayList;
import java.util.List;

public final class RecipeViewerHooks {
    public static final List<Runnable> OPEN_SUPREME_RECIPES = new ArrayList<>();

    private RecipeViewerHooks() {
    }

    public static void invokeFirst() {
        for (Runnable runnable : OPEN_SUPREME_RECIPES) {
            try {
                runnable.run();
                return;
            } catch (RuntimeException ignored) {
            }
        }
    }
}
