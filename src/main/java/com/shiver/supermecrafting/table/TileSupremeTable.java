package com.shiver.supermecrafting.table;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import com.shiver.supermecrafting.ae2.AE2OptionalBridge;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

@Optional.InterfaceList({
        @Optional.Interface(iface = "appeng.api.networking.IGridHost", modid = "appliedenergistics2"),
        @Optional.Interface(iface = "appeng.api.networking.security.IActionHost", modid = "appliedenergistics2")
})
public class TileSupremeTable extends TileEntity implements IInventory, IGridHost, IActionHost {
    private static final String AE2_MOD_ID = "appliedenergistics2";

    private final SupremeTableInventory inventory = new SupremeTableInventory();
    private long modVersion;
    private Object ae2Node;
    private NBTTagCompound ae2NodeData;

    public SupremeTableInventory supremeInventory() {
        return inventory;
    }

    public long modVersion() {
        return modVersion;
    }

    public Object ae2Node() {
        return ae2Node;
    }

    public void setAe2Node(Object ae2Node) {
        this.ae2Node = ae2Node;
    }

    public NBTTagCompound ae2NodeData() {
        return ae2NodeData;
    }

    public void setAe2NodeData(NBTTagCompound ae2NodeData) {
        this.ae2NodeData = ae2NodeData;
    }

    @Override
    public int getSizeInventory() {
        return SupremeTableInventory.SIZE;
    }

    @Override
    public boolean isEmpty() {
        return inventory.isEmpty();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventory.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        ItemStack existing = inventory.get(index);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack taken = existing.splitStack(count);
        if (existing.getCount() <= 0) {
            inventory.set(index, ItemStack.EMPTY);
        }
        markDirty();
        return taken;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = inventory.get(index);
        inventory.set(index, ItemStack.EMPTY);
        markDirty();
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventory.set(index, stack);
        if (!stack.isEmpty() && stack.getCount() > getInventoryStackLimit()) {
            stack.setCount(getInventoryStackLimit());
        }
        markDirty();
    }

    @Override
    public String getName() {
        return "container.supreme_crafting.supreme_table";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return world != null && world.getTileEntity(pos) == this
                && player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override public void openInventory(EntityPlayer player) {}
    @Override public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        inventory.clear();
        markDirty();
    }

    @Override
    public void markDirty() {
        modVersion++;
        super.markDirty();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("Items", inventory.save());
        AE2OptionalBridge.writeNode(this, compound);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        inventory.load(compound.getTagList("Items", 10));
        AE2OptionalBridge.readNode(this, compound);
    }

    public void dropContents(World world, BlockPos pos) {
        NonNullList<ItemStack> copy = NonNullList.create();
        for (ItemStack stack : inventory.items()) {
            if (!stack.isEmpty()) {
                copy.add(stack.copy());
            }
        }
        for (ItemStack stack : copy) {
            net.minecraft.inventory.InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
        }
    }

    @Override
    public void validate() {
        super.validate();
        if (world != null && !world.isRemote) {
            AE2OptionalBridge.updateNode(this);
        }
    }

    @Override
    public void invalidate() {
        AE2OptionalBridge.destroyNode(this);
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        AE2OptionalBridge.destroyNode(this);
        super.onChunkUnload();
    }

    @Override
    @Optional.Method(modid = AE2_MOD_ID)
    public IGridNode getGridNode(AEPartLocation dir) {
        return (IGridNode) AE2OptionalBridge.getGridNode(this);
    }

    @Override
    @Optional.Method(modid = AE2_MOD_ID)
    public AECableType getCableConnectionType(AEPartLocation dir) {
        return (AECableType) AE2OptionalBridge.getCableConnectionType(dir);
    }

    @Override
    @Optional.Method(modid = AE2_MOD_ID)
    public void securityBreak() {
        if (world != null) {
            world.destroyBlock(pos, true);
        }
    }

    @Override
    @Optional.Method(modid = AE2_MOD_ID)
    public IGridNode getActionableNode() {
        return (IGridNode) AE2OptionalBridge.getActionableNode(this);
    }

    private static boolean isAe2Loaded() { return Loader.isModLoaded(AE2_MOD_ID); }
}
