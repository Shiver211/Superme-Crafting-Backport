package com.shiver.supermecrafting.furnace;

import com.shiver.supermecrafting.net.MultiblockLitPacket;
import com.shiver.supermecrafting.net.SCNetwork;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.NonNullList;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;

/**
 * Per-region furnace tick — accumulator model with size-based batch throughput.
 *
 * Each tick smelts up to Region.throughput() items in one batch.
 * 32³ → 1, 64³ → 8, 128³ → 64.
 *
 * Region.litTime is a banked fuel-tick reservoir. Refilled one fuel item at a time,
 * then drained batch × FUEL_PER_ITEM on smelt.
 */
public final class FurnaceTick {
    /** Vanilla cook-cost (200) × 4 = 800 fuel-ticks per smelt. */
    public static final int FUEL_PER_ITEM = 200 * 4;

    private FurnaceTick() {}

    public static void tickAll(WorldServer level) {
        MultiblockRegions regions = MultiblockRegions.get(level);
        for (Region r : new ArrayList<>(regions.all())) {
            tick(level, r, regions);
        }
    }

    private static void tick(WorldServer level, Region r, MultiblockRegions regions) {
        boolean wasLit = r.isLit();
        boolean changed = false;
        boolean cookedThisTick = false;

        NonNullList<ItemStack> items = r.getItems();
        ItemStack input = items.get(Region.SLOT_INPUT);
        ItemStack smeltingResult = input.isEmpty() ? ItemStack.EMPTY
                : FurnaceRecipes.instance().getSmeltingResult(input);

        if (!smeltingResult.isEmpty()) {
            int outRoom = outputRoom(items, smeltingResult);
            if (outRoom > 0) {
                // 1. Top up the bank until it can cover a full throughput batch
                int neededForFullBatch = r.throughput() * FUEL_PER_ITEM;
                while (r.getLitTime() < neededForFullBatch && !items.get(Region.SLOT_FUEL).isEmpty()) {
                    ItemStack fuel = items.get(Region.SLOT_FUEL);
                    int burn = TileEntityFurnace.getItemBurnTime(fuel);
                    if (burn <= 0) break;
                    consumeOneFuel(items, fuel);
                    r.setLitTime(r.getLitTime() + burn);
                    changed = true;
                }

                // 2. Batch size limited by fuel, input, output, and throughput
                int byFuel = r.getLitTime() / FUEL_PER_ITEM;
                int batch = Math.min(r.throughput(),
                        Math.min(byFuel, Math.min(input.getCount(), outRoom)));
                if (batch > 0) {
                    burnBatch(smeltingResult, items, batch);
                    r.setLitTime(r.getLitTime() - batch * FUEL_PER_ITEM);
                    cookedThisTick = true;
                    changed = true;
                }
            }
        }

        if (cookedThisTick != r.isLit()) {
            r.setLit(cookedThisTick);
            changed = true;
        }
        if (wasLit != r.isLit()) {
            broadcastLit(level, r);
        }
        if (changed) regions.markDirty();
    }

    private static int outputRoom(NonNullList<ItemStack> items, ItemStack result) {
        ItemStack out = items.get(Region.SLOT_OUTPUT);
        if (out.isEmpty()) return result.getMaxStackSize();
        if (!ItemStack.areItemsEqual(out, result)) return 0;
        if (!ItemStack.areItemStackTagsEqual(out, result)) return 0;
        return out.getMaxStackSize() - out.getCount();
    }

    private static void burnBatch(ItemStack result, NonNullList<ItemStack> items, int batch) {
        ItemStack input = items.get(Region.SLOT_INPUT);
        ItemStack out = items.get(Region.SLOT_OUTPUT);
        if (out.isEmpty()) {
            ItemStack newOut = result.copy();
            newOut.setCount(batch);
            items.set(Region.SLOT_OUTPUT, newOut);
        } else {
            out.grow(batch);
        }
        // 湿海绵 → 水桶（与原版一样，单次触发；每批次只触发一次）
        if (input.getItem() == Item.getItemFromBlock(Blocks.SPONGE) && input.getMetadata() == 1
                && items.get(Region.SLOT_FUEL).getItem() == Items.BUCKET) {
            items.set(Region.SLOT_FUEL, new ItemStack(Items.WATER_BUCKET));
        }
        input.shrink(batch);
    }

    private static void consumeOneFuel(NonNullList<ItemStack> items, ItemStack fuel) {
        Item fuelItem = fuel.getItem();
        fuel.shrink(1);
        if (fuel.isEmpty()) {
            Item remain = fuelItem.getContainerItem();
            items.set(Region.SLOT_FUEL, remain == null ? ItemStack.EMPTY : new ItemStack(remain));
        }
    }

    private static void broadcastLit(WorldServer level, Region r) {
        SCNetwork.INSTANCE.sendToAll(new MultiblockLitPacket(r.getId(), r.isLit()));
    }
}
