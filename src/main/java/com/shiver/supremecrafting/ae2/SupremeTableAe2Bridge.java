package com.shiver.supremecrafting.ae2;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import com.shiver.supremecrafting.table.TileSupremeTable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public final class SupremeTableAe2Bridge {
    private static final String NODE_TAG = "ae2Node";

    private SupremeTableAe2Bridge() {
    }

    public static IGridNode getGridNode(TileSupremeTable table) {
        IGridNode node = (IGridNode) table.ae2Node();
        if (node == null && table.getWorld() != null && !table.getWorld().isRemote && !table.isInvalid()) {
            node = AEApi.instance().grid().createGridNode(new SupremeTableAe2GridBlock(table));
            table.setAe2Node(node);
            NBTTagCompound data = table.ae2NodeData();
            if (data != null) {
                node.loadFromNBT(NODE_TAG, data);
                table.setAe2NodeData(null);
            }
            node.updateState();
        }
        return node;
    }

    public static AECableType getCableConnectionType(AEPartLocation dir) {
        return AECableType.SMART;
    }

    public static IGridNode getActionableNode(TileSupremeTable table) {
        return getGridNode(table);
    }

    public static void writeNode(TileSupremeTable table, NBTTagCompound compound) {
        IGridNode node = (IGridNode) table.ae2Node();
        if (node != null) {
            node.saveToNBT(NODE_TAG, compound);
        }
    }

    public static void readNode(TileSupremeTable table, NBTTagCompound compound) {
        table.setAe2NodeData(compound);
        IGridNode node = (IGridNode) table.ae2Node();
        if (node != null) {
            node.loadFromNBT(NODE_TAG, compound);
            table.setAe2NodeData(null);
        }
    }

    public static void updateNode(TileSupremeTable table) {
        IGridNode node = getGridNode(table);
        if (node != null) {
            node.updateState();
        }
    }

    public static void destroyNode(TileSupremeTable table) {
        IGridNode node = (IGridNode) table.ae2Node();
        if (node != null) {
            node.destroy();
            table.setAe2Node(null);
        }
    }

    public static void addAvailable(TileSupremeTable table, List<ItemStack> stacks) {
        AeStorage storage = storage(table);
        if (storage == null) {
            return;
        }
        for (IAEItemStack stack : storage.items.getStorageList()) {
            if (stack.getStackSize() > 0) {
                ItemStack itemStack = stack.createItemStack();
                itemStack.setCount((int) Math.min(stack.getStackSize(), Integer.MAX_VALUE));
                stacks.add(itemStack);
            }
        }
    }

    public static ItemStack takeOne(EntityPlayer player, TileSupremeTable table, List<ItemStack> candidates,
                                    ItemStack stackToFill) {
        AeStorage storage = storage(table);
        if (storage == null) {
            return ItemStack.EMPTY;
        }
        IActionSource source = new TableActionSource(player, table);
        for (ItemStack candidate : candidates) {
            IAEItemStack request = AEApi.instance().storage()
                    .getStorageChannel(IItemStorageChannel.class)
                    .createStack(candidate);
            if (request == null) {
                continue;
            }
            request.setStackSize(1);
            IAEItemStack simulated = AEApi.instance().storage()
                    .poweredExtraction(storage.energy, storage.items, request, source, Actionable.SIMULATE);
            if (simulated == null || simulated.getStackSize() <= 0) {
                continue;
            }
            ItemStack simulatedStack = simulated.createItemStack();
            if (!stackToFill.isEmpty() && !canStack(simulatedStack, stackToFill)) {
                continue;
            }
            IAEItemStack extracted = AEApi.instance().storage()
                    .poweredExtraction(storage.energy, storage.items, simulated.copy().setStackSize(1), source, Actionable.MODULATE);
            if (extracted != null && extracted.getStackSize() > 0) {
                ItemStack out = extracted.createItemStack();
                out.setCount(1);
                return out;
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack insert(EntityPlayer player, TileSupremeTable table, ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        AeStorage storage = storage(table);
        if (storage == null) {
            return stack;
        }
        IAEItemStack input = AEApi.instance().storage()
                .getStorageChannel(IItemStorageChannel.class)
                .createStack(stack);
        if (input == null) {
            return stack;
        }
        IAEItemStack leftover = AEApi.instance().storage()
                .poweredInsert(storage.energy, storage.items, input, new TableActionSource(player, table), Actionable.MODULATE);
        return leftover == null ? ItemStack.EMPTY : leftover.createItemStack();
    }

    private static AeStorage storage(TileSupremeTable table) {
        IGridNode node = getGridNode(table);
        if (node == null || !node.isActive()) {
            return null;
        }
        IGrid grid = node.getGrid();
        if (grid == null) {
            return null;
        }
        IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
        IEnergyGrid energyGrid = grid.getCache(IEnergyGrid.class);
        ISecurityGrid securityGrid = grid.getCache(ISecurityGrid.class);
        if (storageGrid == null || energyGrid == null || securityGrid == null || !energyGrid.isNetworkPowered()) {
            return null;
        }
        return new AeStorage(storageGrid.getInventory(AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class)),
                energyGrid);
    }

    private static boolean canStack(ItemStack left, ItemStack right) {
        return left.getItem() == right.getItem()
                && left.getMetadata() == right.getMetadata()
                && ItemStack.areItemStackTagsEqual(left, right);
    }

    private static final class AeStorage {
        private final IMEMonitor<IAEItemStack> items;
        private final IEnergyGrid energy;

        private AeStorage(IMEMonitor<IAEItemStack> items, IEnergyGrid energy) {
            this.items = items;
            this.energy = energy;
        }
    }

    private static final class TableActionSource implements IActionSource {
        private final EntityPlayer player;
        private final TileSupremeTable table;

        private TableActionSource(EntityPlayer player, TileSupremeTable table) {
            this.player = player;
            this.table = table;
        }

        @Override
        @Nonnull
        public Optional<EntityPlayer> player() {
            return Optional.of(player);
        }

        @Override
        @Nonnull
        public Optional<appeng.api.networking.security.IActionHost> machine() {
            return Optional.of(table);
        }

        @Override
        @Nonnull
        public <T> Optional<T> context(@Nonnull Class<T> key) {
            return Optional.empty();
        }
    }
}
