package com.shiver.supermecrafting.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Supreme Table 屏幕上的箭头按钮与已加载的配方查看器插件（JEI 等）之间的桥梁。
 * 每个加载器插件在这里注册一个 {@link Runnable}，调用时打开该查看器的
 * 配方面板，筛选为 Supreme Crafting 类别。
 */
public final class RecipeViewerHooks {
    /** 插件在启动时追加到这里。 */
    public static final List<Runnable> openSupremeRecipes = new ArrayList<>();

    private RecipeViewerHooks() {}

    /**
     * 运行第一个不抛出异常的钩子。JEI 同时安装是正常的；
     * 我们每次点击只想打开一个查看器。
     */
    public static boolean invokeFirst() {
        for (Runnable r : openSupremeRecipes) {
            try {
                r.run();
                return true;
            } catch (Throwable t) {
                // 尝试下一个钩子——查看器可能还没准备好。
            }
        }
        return false;
    }
}
