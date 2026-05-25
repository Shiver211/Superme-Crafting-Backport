package com.shiver.supermecrafting;

import com.shiver.supermecrafting.client.GuiSupremeTable;
import com.shiver.supermecrafting.furnace.MultiblockRegions;
import com.shiver.supermecrafting.furnace.Region;
import com.shiver.supermecrafting.furnace.RegionFurnaceInventory;
import com.shiver.supermecrafting.table.ContainerSupremeTable;
import com.shiver.supermecrafting.table.TileSupremeTable;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

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
}
