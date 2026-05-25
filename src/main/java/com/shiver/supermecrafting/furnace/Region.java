package com.shiver.supermecrafting.furnace;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class Region {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_COUNT = 3;

    private final UUID id;
    private final BlockPos min;
    private final BlockPos max;
    private final EnumFacing front;
    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private boolean lit;
    private int litTime;

    public Region(UUID id, BlockPos min, BlockPos max, EnumFacing front) {
        this.id = id;
        this.min = min;
        this.max = max;
        this.front = front;
    }

    public UUID getId() {
        return id;
    }

    public BlockPos min() {
        return min;
    }

    public BlockPos max() {
        return max;
    }

    public EnumFacing front() {
        return front;
    }

    public NonNullList<ItemStack> items() {
        return items;
    }

    public boolean isLit() {
        return lit;
    }

    public void setLit(boolean lit) {
        this.lit = lit;
    }

    public int litTime() {
        return litTime;
    }

    public void setLitTime(int litTime) {
        this.litTime = Math.max(0, litTime);
    }

    public int size() {
        return max.getX() - min.getX() + 1;
    }

    public int throughput() {
        int scale = size() / 32;
        return scale * scale * scale;
    }

    public boolean contains(BlockPos pos) {
        return pos.getX() >= min.getX() && pos.getX() <= max.getX()
                && pos.getY() >= min.getY() && pos.getY() <= max.getY()
                && pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();
    }

    public NBTTagCompound write() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setUniqueId("id", id);
        tag.setIntArray("min", new int[]{min.getX(), min.getY(), min.getZ()});
        tag.setIntArray("max", new int[]{max.getX(), max.getY(), max.getZ()});
        tag.setInteger("front", front.getHorizontalIndex());
        tag.setBoolean("lit", lit);
        tag.setInteger("litTime", litTime);
        NBTTagCompound itemsTag = new NBTTagCompound();
        net.minecraft.inventory.ItemStackHelper.saveAllItems(itemsTag, items);
        tag.setTag("items", itemsTag);
        return tag;
    }

    public static Region read(NBTTagCompound tag) {
        int[] minA = tag.getIntArray("min");
        int[] maxA = tag.getIntArray("max");
        Region region = new Region(tag.getUniqueId("id"),
                new BlockPos(minA[0], minA[1], minA[2]),
                new BlockPos(maxA[0], maxA[1], maxA[2]),
                EnumFacing.byHorizontalIndex(tag.getInteger("front")));
        region.lit = tag.getBoolean("lit");
        region.litTime = tag.getInteger("litTime");
        net.minecraft.inventory.ItemStackHelper.loadAllItems(tag.getCompoundTag("items"), region.items);
        return region;
    }
}
