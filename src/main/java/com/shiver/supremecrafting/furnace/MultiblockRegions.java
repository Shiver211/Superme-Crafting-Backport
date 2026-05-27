package com.shiver.supremecrafting.furnace;

import com.shiver.supremecrafting.SupremeCrafting;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MultiblockRegions extends WorldSavedData {
    private static final String NAME = SupremeCrafting.MOD_ID + "_multiblocks";
    private final Map<UUID, Region> regions = new HashMap<>();

    public MultiblockRegions() {
        super(NAME);
    }

    public MultiblockRegions(String name) {
        super(name);
    }

    public static MultiblockRegions get(World world) {
        MapStorage storage = world.getPerWorldStorage();
        MultiblockRegions data = (MultiblockRegions) storage.getOrLoadData(MultiblockRegions.class, NAME);
        if (data == null) {
            data = new MultiblockRegions();
            storage.setData(NAME, data);
        }
        return data;
    }

    public Region create(BlockPos min, BlockPos max, net.minecraft.util.EnumFacing front) {
        Region region = new Region(UUID.randomUUID(), min, max, front);
        regions.put(region.getId(), region);
        markDirty();
        return region;
    }

    public Region byId(UUID id) {
        return regions.get(id);
    }

    public Collection<Region> all() {
        return regions.values();
    }

    public Region findContaining(BlockPos pos) {
        for (Region region : regions.values()) {
            if (region.contains(pos)) {
                return region;
            }
        }
        return null;
    }

    public void remove(UUID id) {
        regions.remove(id);
        markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        regions.clear();
        NBTTagList list = nbt.getTagList("regions", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            Region region = Region.read(list.getCompoundTagAt(i));
            regions.put(region.getId(), region);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (Region region : regions.values()) {
            list.appendTag(region.write());
        }
        compound.setTag("regions", list);
        return compound;
    }
}
