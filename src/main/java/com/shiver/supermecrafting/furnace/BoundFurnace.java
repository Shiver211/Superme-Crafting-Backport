package com.shiver.supermecrafting.furnace;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

/**
 * What the Supreme Furnace Terminal stores when bound: the dimension ID and
 * UUID of a formed Region.
 */
public class BoundFurnace {
    private final int dimensionId;
    private final UUID regionId;

    public BoundFurnace(int dimensionId, UUID regionId) {
        this.dimensionId = dimensionId;
        this.regionId = regionId;
    }

    public int dimensionId() { return dimensionId; }
    public UUID regionId() { return regionId; }

    public NBTTagCompound save() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("dim", dimensionId);
        tag.setString("regionId", regionId.toString());
        return tag;
    }

    public static BoundFurnace load(NBTTagCompound tag) {
        int dim = tag.getInteger("dim");
        UUID id = UUID.fromString(tag.getString("regionId"));
        return new BoundFurnace(dim, id);
    }
}
