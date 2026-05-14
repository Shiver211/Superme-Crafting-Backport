package com.shiver.supermecrafting;

import com.shiver.supermecrafting.furnace.MultiblockRegions;
import com.shiver.supermecrafting.furnace.Region;
import com.shiver.supermecrafting.furnace.RegionFurnaceContainer;
import com.shiver.supermecrafting.table.SupremeTableContainer;
import com.shiver.supermecrafting.table.SupremeTableTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class GuiHandler implements IGuiHandler {

    public static final int SUPREME_TABLE = 0;
    public static final int SUPREME_FURNACE = 1;

    @Nullable
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);
        switch (id) {
            case SUPREME_TABLE:
                if (te instanceof SupremeTableTileEntity) {
                    return new SupremeTableContainer(player.inventory, (SupremeTableTileEntity) te);
                }
                return null;
            case SUPREME_FURNACE:
                if (!world.isRemote && world instanceof WorldServer) {
                    Region r = MultiblockRegions.get((WorldServer) world).findContaining(pos);
                    if (r != null) {
                        RegionFurnaceContainer furnaceInv = new RegionFurnaceContainer((WorldServer) world, r.getId());
                        return new ContainerFurnace(player.inventory, furnaceInv);
                    }
                }
                return null;
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);
        switch (id) {
            case SUPREME_TABLE:
                if (te instanceof SupremeTableTileEntity) {
                    return new com.shiver.supermecrafting.client.SupremeTableGui(
                            new SupremeTableContainer(player.inventory, (SupremeTableTileEntity) te));
                }
                return null;
            case SUPREME_FURNACE:
                // Client side: fields are synced via ContainerFurnace's field sync
                return new net.minecraft.client.gui.inventory.GuiFurnace(
                        player.inventory, new com.shiver.supermecrafting.client.ClientFurnaceData());
            default:
                return null;
        }
    }
}
