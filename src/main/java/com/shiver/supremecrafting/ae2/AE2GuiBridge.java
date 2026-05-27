package com.shiver.supremecrafting.ae2;

import com.shiver.supremecrafting.client.GuiSupremeInterface;
import com.shiver.supremecrafting.client.GuiSupremePatternTerminal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public final class AE2GuiBridge {
    private AE2GuiBridge() {
    }

    public static Object serverPatternTerminal(EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
        return tile instanceof TileSupremePatternTerminal
                ? new ContainerSupremePatternTerminal(player.inventory, (TileSupremePatternTerminal) tile)
                : null;
    }

    public static Object clientPatternTerminal(EntityPlayer player, World world, int x, int y, int z) {
        Object container = serverPatternTerminal(player, world, x, y, z);
        return container instanceof ContainerSupremePatternTerminal
                ? new GuiSupremePatternTerminal((ContainerSupremePatternTerminal) container)
                : null;
    }

    public static Object serverInterface(EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
        return tile instanceof TileSupremeInterface
                ? new ContainerSupremeInterface(player.inventory, (TileSupremeInterface) tile)
                : null;
    }

    public static Object clientInterface(EntityPlayer player, World world, int x, int y, int z) {
        Object container = serverInterface(player, world, x, y, z);
        return container instanceof ContainerSupremeInterface
                ? new GuiSupremeInterface((ContainerSupremeInterface) container)
                : null;
    }
}
