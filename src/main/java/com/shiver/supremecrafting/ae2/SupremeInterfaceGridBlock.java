package com.shiver.supremecrafting.ae2;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridNotification;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import java.util.EnumSet;

public class SupremeInterfaceGridBlock implements IGridBlock {
    private final TileSupremeInterface tile;

    public SupremeInterfaceGridBlock(TileSupremeInterface tile) {
        this.tile = tile;
    }

    @Override public double getIdlePowerUsage() { return 1.0D; }

    @Override
    @Nonnull
    public EnumSet<GridFlags> getFlags() {
        return EnumSet.of(GridFlags.REQUIRE_CHANNEL);
    }

    @Override public boolean isWorldAccessible() { return true; }

    @Override
    @Nonnull
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(tile);
    }

    @Override
    @Nonnull
    public AEColor getGridColor() {
        return AEColor.TRANSPARENT;
    }

    @Override public void onGridNotification(@Nonnull GridNotification notification) {}
    @Override public void setNetworkStatus(IGrid grid, int channelsInUse) {}

    @Override
    @Nonnull
    public EnumSet<EnumFacing> getConnectableSides() {
        return EnumSet.allOf(EnumFacing.class);
    }

    @Override
    @Nonnull
    public IGridHost getMachine() {
        return tile;
    }

    @Override public void gridChanged() {}

    @Override
    @Nonnull
    public ItemStack getMachineRepresentation() {
        return new ItemStack(AE2Module.SUPREME_INTERFACE);
    }
}
