package com.shiver.supremecrafting.ae2;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TileSupremeInterface extends TileEntity implements IInventory, IGridHost, IActionHost, ICraftingProvider, ITickable {
    private static final String NODE_TAG = "ae2Node";
    public static final int PATTERN_SLOTS = 36;

    private final NonNullList<ItemStack> patterns = NonNullList.withSize(PATTERN_SLOTS, ItemStack.EMPTY);
    private final NonNullList<ItemStack> pendingOutputs = NonNullList.create();
    private List<BlockPos> lastAssemblerPositions = new ArrayList<>();
    private boolean patternChangePending;
    private Object ae2Node;
    private NBTTagCompound ae2NodeData;

    @Override
    public void update() {
        if (world != null && !world.isRemote) {
            retryOutputs();
            refreshAssemblerPositions();
            if (patternChangePending) {
                patternChangePending = !postPatternChange();
            }
        }
    }

    @Override
    public void provideCrafting(ICraftingProviderHelper craftingTracker) {
        IGridNode node = getGridNode(AEPartLocation.INTERNAL);
        if (node == null || !node.isActive()) {
            return;
        }
        List<BlockPos> assemblerPositions = assemblerPositions();
        for (ItemStack pattern : patterns) {
            if (SupremePatternData.isRecipeValid(pattern, world)) {
                SupremeCraftingPatternDetails details = new SupremeCraftingPatternDetails(pattern, world);
                for (BlockPos assemblerPos : assemblerPositions) {
                    craftingTracker.addCraftingOption(new AssemblerProvider(this, assemblerPos), details);
                }
            }
        }
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
        if (isBusy()) {
            return false;
        }
        TileSupremeAssembler assembler = idleAssembler();
        if (assembler == null) {
            return false;
        }
        return pushPatternTo(assembler.getPos(), patternDetails, table);
    }

    private boolean pushPatternTo(BlockPos assemblerPos, ICraftingPatternDetails patternDetails, InventoryCrafting table) {
        if (!pendingOutputs.isEmpty()) {
            return false;
        }
        TileEntity tile = world.getTileEntity(assemblerPos);
        if (!(tile instanceof TileSupremeAssembler)) {
            return false;
        }
        TileSupremeAssembler assembler = (TileSupremeAssembler) tile;
        if (assembler.isBusy()) {
            return false;
        }
        IEnergyGrid energy = energyGrid();
        if (energy == null || energy.extractAEPower(500.0D, Actionable.MODULATE, appeng.api.config.PowerMultiplier.CONFIG) < 499.99D) {
            return false;
        }
        NonNullList<ItemStack> inputs = NonNullList.create();
        for (int i = 0; i < table.getSizeInventory(); i++) {
            ItemStack stack = table.getStackInSlot(i);
            if (!stack.isEmpty()) {
                inputs.add(stack.copy());
            }
        }
        NonNullList<ItemStack> outputs = NonNullList.create();
        for (appeng.api.storage.data.IAEItemStack stack : patternDetails.getCondensedOutputs()) {
            if (stack != null) {
                outputs.add(stack.createItemStack());
            }
        }
        return assembler.start(this, inputs, outputs);
    }

    @Override
    public boolean isBusy() {
        return !pendingOutputs.isEmpty() || idleAssembler() == null;
    }

    public boolean receiveAssemblerOutput(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        ItemStack remaining = insertToNetwork(stack);
        if (!remaining.isEmpty()) {
            pendingOutputs.add(remaining);
        }
        markDirty();
        return true;
    }

    public boolean receiveAssemblerOutputs(List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            receiveAssemblerOutput(stack);
        }
        return true;
    }

    private void retryOutputs() {
        if (pendingOutputs.isEmpty()) {
            return;
        }
        for (int i = 0; i < pendingOutputs.size(); i++) {
            ItemStack remaining = insertToNetwork(pendingOutputs.get(i));
            if (remaining.isEmpty()) {
                pendingOutputs.remove(i--);
            } else {
                pendingOutputs.set(i, remaining);
            }
        }
        markDirty();
    }

    private ItemStack insertToNetwork(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        IGridNode node = getGridNode(AEPartLocation.INTERNAL);
        if (node == null || !node.isActive() || node.getGrid() == null) {
            return stack;
        }
        IStorageGrid storageGrid = node.getGrid().getCache(IStorageGrid.class);
        IEnergyGrid energyGrid = node.getGrid().getCache(IEnergyGrid.class);
        if (storageGrid == null || energyGrid == null || !energyGrid.isNetworkPowered()) {
            return stack;
        }
        IMEMonitor<IAEItemStack> inventory = storageGrid.getInventory(
                AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class));
        IAEItemStack input = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(stack);
        IAEItemStack leftover = AEApi.instance().storage().poweredInsert(energyGrid, inventory, input,
                new InterfaceActionSource(this), Actionable.MODULATE);
        return leftover == null ? ItemStack.EMPTY : leftover.createItemStack();
    }

    private IEnergyGrid energyGrid() {
        IGridNode node = getGridNode(AEPartLocation.INTERNAL);
        if (node == null || !node.isActive() || node.getGrid() == null) {
            return null;
        }
        IEnergyGrid grid = node.getGrid().getCache(IEnergyGrid.class);
        return grid != null && grid.isNetworkPowered() ? grid : null;
    }

    private boolean hasAssembler() {
        return idleAssembler() != null;
    }

    private List<BlockPos> assemblerPositions() {
        List<BlockPos> positions = new ArrayList<>();
        if (world == null) {
            return positions;
        }
        for (EnumFacing face : EnumFacing.values()) {
            BlockPos assemblerPos = pos.offset(face);
            if (world.getTileEntity(assemblerPos) instanceof TileSupremeAssembler) {
                positions.add(assemblerPos);
            }
        }
        return positions;
    }

    private void refreshAssemblerPositions() {
        List<BlockPos> currentPositions = assemblerPositions();
        if (!currentPositions.equals(lastAssemblerPositions)) {
            lastAssemblerPositions = currentPositions;
            patternChangePending = !postPatternChange();
        }
    }

    private boolean isAssemblerBusy(BlockPos assemblerPos) {
        if (!pendingOutputs.isEmpty() || world == null) {
            return true;
        }
        TileEntity tile = world.getTileEntity(assemblerPos);
        return !(tile instanceof TileSupremeAssembler) || ((TileSupremeAssembler) tile).isBusy();
    }

    private TileSupremeAssembler idleAssembler() {
        if (world == null) {
            return null;
        }
        for (EnumFacing face : EnumFacing.values()) {
            TileEntity tile = world.getTileEntity(pos.offset(face));
            if (tile instanceof TileSupremeAssembler && !((TileSupremeAssembler) tile).isBusy()) {
                return (TileSupremeAssembler) tile;
            }
        }
        return null;
    }

    @Override
    public IGridNode getGridNode(AEPartLocation dir) {
        IGridNode node = (IGridNode) ae2Node;
        if (node == null && world != null && !world.isRemote && !isInvalid()) {
            node = AEApi.instance().grid().createGridNode(new SupremeInterfaceGridBlock(this));
            ae2Node = node;
            if (ae2NodeData != null) {
                node.loadFromNBT(NODE_TAG, ae2NodeData);
                ae2NodeData = null;
            }
            node.updateState();
        }
        return node;
    }

    @Override public AECableType getCableConnectionType(AEPartLocation dir) { return AECableType.SMART; }

    @Override
    public void securityBreak() {
        if (world != null) {
            world.destroyBlock(pos, true);
        }
    }

    @Override public IGridNode getActionableNode() { return getGridNode(AEPartLocation.INTERNAL); }

    @Override
    public void validate() {
        super.validate();
        if (world != null && !world.isRemote) {
            getGridNode(AEPartLocation.INTERNAL);
        }
    }

    @Override
    public void invalidate() {
        destroyNode();
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        destroyNode();
        super.onChunkUnload();
    }

    private void destroyNode() {
        IGridNode node = (IGridNode) ae2Node;
        if (node != null) {
            node.destroy();
            ae2Node = null;
        }
    }

    private boolean postPatternChange() {
        IGridNode node = getGridNode(AEPartLocation.INTERNAL);
        if (node != null && node.isActive() && node.getGrid() != null) {
            node.getGrid().postEvent(new MENetworkCraftingPatternChange(this, node));
            return true;
        }
        return false;
    }

    @Override public int getSizeInventory() { return PATTERN_SLOTS; }
    @Override public boolean isEmpty() { return patterns.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getStackInSlot(int index) { return patterns.get(index); }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack existing = patterns.get(index);
        if (existing.isEmpty()) return ItemStack.EMPTY;
        ItemStack taken = existing.splitStack(count);
        if (existing.isEmpty()) patterns.set(index, ItemStack.EMPTY);
        markDirty();
        patternChangePending = !postPatternChange();
        return taken;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = patterns.get(index);
        patterns.set(index, ItemStack.EMPTY);
        markDirty();
        patternChangePending = !postPatternChange();
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        patterns.set(index, stack == null ? ItemStack.EMPTY : stack);
        markDirty();
        patternChangePending = !postPatternChange();
    }

    @Override public String getName() { return "container.supreme_crafting.supreme_interface"; }
    @Override public boolean hasCustomName() { return false; }
    @Override public int getInventoryStackLimit() { return 1; }
    @Override public boolean isUsableByPlayer(EntityPlayer player) { return true; }
    @Override public void openInventory(EntityPlayer player) {}
    @Override public void closeInventory(EntityPlayer player) {}
    @Override public boolean isItemValidForSlot(int index, ItemStack stack) { return SupremePatternData.isEncoded(stack); }
    @Override public int getField(int id) { return 0; }
    @Override public void setField(int id, int value) {}
    @Override public int getFieldCount() { return 0; }

    @Override
    public void clear() {
        for (int i = 0; i < patterns.size(); i++) {
            patterns.set(i, ItemStack.EMPTY);
        }
        markDirty();
        patternChangePending = !postPatternChange();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        IGridNode node = (IGridNode) ae2Node;
        if (node != null) {
            node.saveToNBT(NODE_TAG, compound);
        }
        compound.setTag("Patterns", writeList(patterns));
        compound.setTag("PendingOutputs", writeList(pendingOutputs));
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        ae2NodeData = compound;
        readFixedList(compound.getTagList("Patterns", 10), patterns);
        pendingOutputs.clear();
        readGrowList(compound.getTagList("PendingOutputs", 10), pendingOutputs);
        patternChangePending = !isEmpty();
    }

    private static NBTTagList writeList(List<ItemStack> stacks) {
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack stack = stacks.get(i);
            if (!stack.isEmpty()) {
                NBTTagCompound tag = stack.writeToNBT(new NBTTagCompound());
                tag.setInteger("Slot", i);
                list.appendTag(tag);
            }
        }
        return list;
    }

    private static void readFixedList(NBTTagList list, NonNullList<ItemStack> stacks) {
        for (int i = 0; i < stacks.size(); i++) {
            stacks.set(i, ItemStack.EMPTY);
        }
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int slot = tag.getInteger("Slot");
            if (slot >= 0 && slot < stacks.size()) {
                stacks.set(slot, new ItemStack(tag));
            }
        }
    }

    private static void readGrowList(NBTTagList list, NonNullList<ItemStack> stacks) {
        for (int i = 0; i < list.tagCount(); i++) {
            ItemStack stack = new ItemStack(list.getCompoundTagAt(i));
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
    }

    public void dropContents() {
        if (world == null) {
            return;
        }
        List<ItemStack> drops = new ArrayList<>();
        drops.addAll(patterns);
        drops.addAll(pendingOutputs);
        for (ItemStack stack : drops) {
            if (!stack.isEmpty()) {
                InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
    }

    private static final class InterfaceActionSource implements IActionSource {
        private final TileSupremeInterface tile;

        private InterfaceActionSource(TileSupremeInterface tile) {
            this.tile = tile;
        }

        @Override
        @Nonnull
        public Optional<EntityPlayer> player() {
            return Optional.empty();
        }

        @Override
        @Nonnull
        public Optional<IActionHost> machine() {
            return Optional.of(tile);
        }

        @Override
        @Nonnull
        public <T> Optional<T> context(@Nonnull Class<T> key) {
            return Optional.empty();
        }
    }

    private static final class AssemblerProvider implements ICraftingProvider {
        private final TileSupremeInterface owner;
        private final BlockPos assemblerPos;

        private AssemblerProvider(TileSupremeInterface owner, BlockPos assemblerPos) {
            this.owner = owner;
            this.assemblerPos = assemblerPos;
        }

        @Override
        public void provideCrafting(ICraftingProviderHelper craftingTracker) {
        }

        @Override
        public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
            return owner.pushPatternTo(assemblerPos, patternDetails, table);
        }

        @Override
        public boolean isBusy() {
            return owner.isAssemblerBusy(assemblerPos);
        }
    }
}
