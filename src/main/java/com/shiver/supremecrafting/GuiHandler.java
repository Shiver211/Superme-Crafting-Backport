package com.shiver.supremecrafting;

import com.shiver.supremecrafting.client.GuiSupremeTable;
import com.shiver.supremecrafting.ae2.AE2OptionalBridge;
import com.shiver.supremecrafting.furnace.MultiblockRegions;
import com.shiver.supremecrafting.furnace.Region;
import com.shiver.supremecrafting.furnace.RegionFurnaceInventory;
import com.shiver.supremecrafting.table.ContainerSupremeTable;
import com.shiver.supremecrafting.table.TileSupremeTable;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import java.lang.reflect.Method;
import java.util.UUID;

public class GuiHandler implements IGuiHandler {
    public static final String TERMINAL_MOST = "SupremeCraftingTerminalMost";
    public static final String TERMINAL_LEAST = "SupremeCraftingTerminalLeast";

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == GuiIds.SUPREME_TABLE) {
            TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
            if (te instanceof TileSupremeTable) {
                return new ContainerSupremeTable(player.inventory, (TileSupremeTable) te);
            }
        }
        if (id == GuiIds.SUPREME_PATTERN_TERMINAL) {
            return ae2Gui("serverPatternTerminal", player, world, x, y, z);
        }
        if (id == GuiIds.SUPREME_INTERFACE) {
            return ae2Gui("serverInterface", player, world, x, y, z);
        }
        Region region = regionFor(id, player, world, x, y, z);
        if (region != null) {
            return new ContainerFurnace(player.inventory, new RegionFurnaceInventory(world, region.getId()));
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == GuiIds.SUPREME_TABLE) {
            TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
            if (te instanceof TileSupremeTable) {
                return new GuiSupremeTable(new ContainerSupremeTable(player.inventory, (TileSupremeTable) te), player.inventory);
            }
        }
        if (id == GuiIds.SUPREME_PATTERN_TERMINAL) {
            return ae2Gui("clientPatternTerminal", player, world, x, y, z);
        }
        if (id == GuiIds.SUPREME_INTERFACE) {
            return ae2Gui("clientInterface", player, world, x, y, z);
        }
        if (id == GuiIds.SUPREME_FURNACE || id == GuiIds.SUPREME_TERMINAL) {
            return new GuiFurnace(player.inventory,
                    new InventoryBasic("container.supreme_crafting.supreme_furnace", false, Region.SLOT_COUNT));
        }
        Region region = regionFor(id, player, world, x, y, z);
        if (region != null) {
            return new GuiFurnace(player.inventory, new RegionFurnaceInventory(world, region.getId()));
        }
        return null;
    }

    private static Region regionFor(int id, EntityPlayer player, World world, int x, int y, int z) {
        if (id == GuiIds.SUPREME_FURNACE) {
            return MultiblockRegions.get(world).findContaining(new net.minecraft.util.math.BlockPos(x, y, z));
        }
        if (id == GuiIds.SUPREME_TERMINAL) {
            NBTTagCompound data = player.getEntityData();
            if (data.hasKey(TERMINAL_MOST) && data.hasKey(TERMINAL_LEAST)) {
                return MultiblockRegions.get(world).byId(new UUID(data.getLong(TERMINAL_MOST), data.getLong(TERMINAL_LEAST)));
            }
        }
        return null;
    }

    private static Object ae2Gui(String methodName, EntityPlayer player, World world, int x, int y, int z) {
        if (!AE2OptionalBridge.loaded()) {
            return null;
        }
        try {
            Class<?> bridge = Class.forName("com.shiver.supremecrafting.ae2.AE2GuiBridge");
            Method method = bridge.getMethod(methodName, EntityPlayer.class, World.class,
                    int.class, int.class, int.class);
            return method.invoke(null, player, world, x, y, z);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to open AE2 GUI " + methodName, e);
        }
    }
}
