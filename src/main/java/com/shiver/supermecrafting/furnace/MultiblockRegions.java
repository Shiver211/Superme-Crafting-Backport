package com.shiver.supermecrafting.furnace;

import com.shiver.supermecrafting.Tags;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Per-world index of formed Supreme Furnace structures.
 * Uses chunk-based secondary index for O(1) "what region contains this block?" lookups.
 */
public class MultiblockRegions extends WorldSavedData {
    private static final String DATA_NAME = Tags.MOD_ID + "_multiblocks";

    private final Map<UUID, Region> regions = new HashMap<>();
    private final Map<Long, List<UUID>> chunkIndex = new HashMap<>();

    public MultiblockRegions() {
        super(DATA_NAME);
    }

    public MultiblockRegions(String name) {
        super(name);
    }

    public static MultiblockRegions get(WorldServer world) {
        MapStorage storage = world.getMapStorage();
        MultiblockRegions data = (MultiblockRegions) storage.getOrLoadData(MultiblockRegions.class, DATA_NAME);
        if (data == null) {
            data = new MultiblockRegions();
            storage.setData(DATA_NAME, data);
        }
        return data;
    }

    @Nullable
    public Region findContaining(BlockPos pos) {
        long key = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
        List<UUID> ids = chunkIndex.get(key);
        if (ids == null) return null;
        for (UUID id : ids) {
            Region r = regions.get(id);
            if (r != null && r.contains(pos)) return r;
        }
        return null;
    }

    public Region create(int[] bounds, EnumFacing front) {
        Region r = new Region(UUID.randomUUID(), bounds, front);
        regions.put(r.getId(), r);
        addToIndex(r);
        markDirty();
        return r;
    }

    public Collection<Region> all() {
        return regions.values();
    }

    public void remove(UUID id) {
        Region r = regions.remove(id);
        if (r == null) return;
        removeFromIndex(r);
        markDirty();
    }

    @Nullable
    public Region byId(UUID id) {
        return regions.get(id);
    }

    private void addToIndex(Region r) {
        forEachChunk(r.getBounds(), key ->
                chunkIndex.computeIfAbsent(key, k -> new ArrayList<>()).add(r.getId()));
    }

    private void removeFromIndex(Region r) {
        forEachChunk(r.getBounds(), key -> {
            List<UUID> list = chunkIndex.get(key);
            if (list == null) return;
            list.remove(r.getId());
            if (list.isEmpty()) chunkIndex.remove(key);
        });
    }

    private static void forEachChunk(int[] bounds, LongConsumer fn) {
        int minCx = bounds[0] >> 4;
        int maxCx = bounds[3] >> 4;
        int minCz = bounds[2] >> 4;
        int maxCz = bounds[5] >> 4;
        for (int cx = minCx; cx <= maxCx; cx++) {
            for (int cz = minCz; cz <= maxCz; cz++) {
                fn.accept(ChunkPos.asLong(cx, cz));
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        regions.clear();
        chunkIndex.clear();
        NBTTagList list = tag.getTagList("regions", 10); // 10 = TAG_COMPOUND
        for (int i = 0; i < list.tagCount(); i++) {
            Region r = Region.load(list.getCompoundTagAt(i));
            regions.put(r.getId(), r);
            addToIndex(r);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        NBTTagList list = new NBTTagList();
        for (Region r : regions.values()) {
            list.appendTag(r.save());
        }
        tag.setTag("regions", list);
        return tag;
    }

    @FunctionalInterface
    private interface LongConsumer {
        void accept(long value);
    }
}
