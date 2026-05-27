package com.shiver.supremecrafting.ae2;

import com.shiver.supremecrafting.table.TileSupremeTable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Loader;

import java.lang.reflect.Method;
import java.util.List;

public final class AE2OptionalBridge {
    private static final String AE2_MOD_ID = "appliedenergistics2";

    private AE2OptionalBridge() {
    }

    public static boolean loaded() {
        return Loader.isModLoaded(AE2_MOD_ID);
    }

    public static void writeNode(TileSupremeTable table, NBTTagCompound compound) {
        invokeVoid("writeNode", new Class<?>[] { TileSupremeTable.class, NBTTagCompound.class }, table, compound);
    }

    public static void readNode(TileSupremeTable table, NBTTagCompound compound) {
        invokeVoid("readNode", new Class<?>[] { TileSupremeTable.class, NBTTagCompound.class }, table, compound);
    }

    public static void updateNode(TileSupremeTable table) {
        invokeVoid("updateNode", new Class<?>[] { TileSupremeTable.class }, table);
    }

    public static void destroyNode(TileSupremeTable table) {
        invokeVoid("destroyNode", new Class<?>[] { TileSupremeTable.class }, table);
    }

    public static Object getGridNode(TileSupremeTable table) {
        return invoke("getGridNode", new Class<?>[] { TileSupremeTable.class }, table);
    }

    public static Object getActionableNode(TileSupremeTable table) {
        return invoke("getActionableNode", new Class<?>[] { TileSupremeTable.class }, table);
    }

    public static Object getCableConnectionType(Object dir) {
        if (!loaded()) {
            return null;
        }
        try {
            Class<?> bridge = Class.forName("com.shiver.supremecrafting.ae2.SupremeTableAe2Bridge");
            Class<?> location = Class.forName("appeng.api.util.AEPartLocation");
            Method method = bridge.getMethod("getCableConnectionType", location);
            return method.invoke(null, dir);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to call AE2 bridge method getCableConnectionType", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void addAvailable(TileSupremeTable table, List<ItemStack> stacks) {
        invokeVoid("addAvailable", new Class<?>[] { TileSupremeTable.class, List.class }, table, stacks);
    }

    public static ItemStack takeOne(EntityPlayer player, TileSupremeTable table, List<ItemStack> candidates, ItemStack stackToFill) {
        Object result = invoke("takeOne",
                new Class<?>[] { EntityPlayer.class, TileSupremeTable.class, List.class, ItemStack.class },
                player, table, candidates, stackToFill);
        return result instanceof ItemStack ? (ItemStack) result : ItemStack.EMPTY;
    }

    public static ItemStack insert(EntityPlayer player, TileSupremeTable table, ItemStack stack) {
        Object result = invoke("insert",
                new Class<?>[] { EntityPlayer.class, TileSupremeTable.class, ItemStack.class },
                player, table, stack);
        return result instanceof ItemStack ? (ItemStack) result : stack;
    }

    private static void invokeVoid(String name, Class<?>[] types, Object... args) {
        invoke(name, types, args);
    }

    private static Object invoke(String name, Class<?>[] types, Object... args) {
        if (!loaded()) {
            return null;
        }
        try {
            Class<?> bridge = Class.forName("com.shiver.supremecrafting.ae2.SupremeTableAe2Bridge");
            Method method = bridge.getMethod(name, types);
            return method.invoke(null, args);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to call AE2 bridge method " + name, e);
        }
    }
}
