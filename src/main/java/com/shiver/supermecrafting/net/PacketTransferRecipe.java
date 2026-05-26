package com.shiver.supermecrafting.net;

import com.shiver.supermecrafting.ae2.AE2OptionalBridge;
import com.shiver.supermecrafting.recipe.SupremeRecipe;
import com.shiver.supermecrafting.table.ContainerSupremeTable;
import com.shiver.supermecrafting.table.SupremeTableInventory;
import com.shiver.supermecrafting.table.TileSupremeTable;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class PacketTransferRecipe implements IMessage {
    private static final EnumFacing[] STORAGE_FACES = {
            EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST
    };

    private ResourceLocation recipeId;
    private boolean maxTransfer;

    public PacketTransferRecipe() {
    }

    public PacketTransferRecipe(ResourceLocation recipeId, boolean maxTransfer) {
        this.recipeId = recipeId;
        this.maxTransfer = maxTransfer;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        maxTransfer = buf.readBoolean();
        recipeId = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(maxTransfer);
        ByteBufUtils.writeUTF8String(buf, recipeId.toString());
    }

    public static class Handler implements IMessageHandler<PacketTransferRecipe, IMessage> {
        @Override
        public IMessage onMessage(PacketTransferRecipe message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            FMLCommonHandler.instance().getMinecraftServerInstance()
                    .addScheduledTask(() -> transfer(player, message.recipeId, message.maxTransfer));
            return null;
        }

        private static void transfer(EntityPlayerMP player, ResourceLocation recipeId, boolean maxTransfer) {
            if (AE2OptionalBridge.loaded() && transferToPatternTerminal(player, recipeId)) {
                return;
            }
            if (!(player.openContainer instanceof ContainerSupremeTable)) return;
            Map<Integer, List<ItemStack>> targets = targets(recipeId);
            if (targets.isEmpty()) return;
            ContainerSupremeTable container = (ContainerSupremeTable) player.openContainer;
            if (maxTransfer) {
                while (true) {
                    if (!canTransferRound(container, player, targets)) {
                        break;
                    }
                    for (Map.Entry<Integer, List<ItemStack>> entry : targets.entrySet()) {
                        if (!canAccept(container, entry.getKey(), entry.getValue(), true)) {
                            continue;
                        }
                        if (!transferOne(container, player, entry.getKey(), entry.getValue(), true)) {
                            container.detectAndSendChanges();
                            return;
                        }
                    }
                }
            } else {
                for (Map.Entry<Integer, List<ItemStack>> entry : targets.entrySet()) {
                    transferOne(container, player, entry.getKey(), entry.getValue(), false);
                }
            }
            container.detectAndSendChanges();
        }

        private static boolean transferToPatternTerminal(EntityPlayerMP player, ResourceLocation recipeId) {
            try {
                Class<?> bridge = Class.forName("com.shiver.supermecrafting.ae2.AE2PatternTerminalTransferBridge");
                Method method = bridge.getMethod("transfer", EntityPlayerMP.class, ResourceLocation.class);
                Object result = method.invoke(null, player, recipeId);
                return result instanceof Boolean && (Boolean) result;
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to transfer recipe to AE2 pattern terminal", e);
            }
        }

        private static Map<Integer, List<ItemStack>> targets(ResourceLocation recipeId) {
            Map<Integer, List<ItemStack>> targets = new HashMap<>();
            IRecipe recipe = ForgeRegistries.RECIPES.getValue(recipeId);
            if (!(recipe instanceof SupremeRecipe)) {
                return targets;
            }
            SupremeRecipe supremeRecipe = (SupremeRecipe) recipe;
            int width = Math.max(1, supremeRecipe.getWidth());
            int height = Math.max(1, supremeRecipe.getHeight());
            List<Ingredient> ingredients = supremeRecipe.getSupremeIngredients();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int ingredientIndex = x + y * width;
                    if (ingredientIndex >= ingredients.size()) {
                        continue;
                    }
                    List<ItemStack> candidates = candidates(ingredients.get(ingredientIndex));
                    if (!candidates.isEmpty()) {
                        targets.put(SupremeTableInventory.indexOf(x, y), candidates);
                    }
                }
            }
            return targets;
        }

        private static List<ItemStack> candidates(Ingredient ingredient) {
            List<ItemStack> candidates = new ArrayList<>();
            for (ItemStack stack : ingredient.getMatchingStacks()) {
                if (!stack.isEmpty()) {
                    candidates.add(stack);
                }
            }
            return candidates;
        }

        private static boolean transferOne(ContainerSupremeTable container, EntityPlayerMP player, int slot,
                                           List<ItemStack> candidates, boolean fillExisting) {
            if (slot < 0 || slot >= SupremeTableInventory.SIZE || candidates.isEmpty()) {
                return false;
            }
            ItemStack existing = container.table().getStackInSlot(slot);
            if (!existing.isEmpty()) {
                if (!fillExisting || !matchesAny(existing, candidates) || existing.getCount() >= slotLimit(container, existing)) {
                    return false;
                }
            }
            ItemStack found = takeOne(player, container.table().getPos(), candidates, existing);
            if (found.isEmpty()) {
                return false;
            }
            if (existing.isEmpty()) {
                container.table().setInventorySlotContents(slot, found);
            } else {
                existing.grow(found.getCount());
                container.table().markDirty();
            }
            return true;
        }

        private static boolean canTransferRound(ContainerSupremeTable container, EntityPlayerMP player,
                                                Map<Integer, List<ItemStack>> targets) {
            List<ItemStack> available = collectAvailable(player, container.table().getPos());
            boolean neededAny = false;
            for (Map.Entry<Integer, List<ItemStack>> entry : targets.entrySet()) {
                int slot = entry.getKey();
                List<ItemStack> candidates = entry.getValue();
                if (!canAccept(container, slot, candidates, true)) {
                    continue;
                }
                neededAny = true;
                ItemStack existing = container.table().getStackInSlot(slot);
                if (!reserveOne(available, candidates, existing)) {
                    return false;
                }
            }
            return neededAny;
        }

        private static List<ItemStack> collectAvailable(EntityPlayerMP player, BlockPos tablePos) {
            List<ItemStack> stacks = new ArrayList<>();
            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                addAvailable(stacks, player.inventory.getStackInSlot(i));
            }
            for (EnumFacing face : STORAGE_FACES) {
                TileEntity tile = player.world.getTileEntity(tablePos.offset(face));
                if (tile == null) {
                    continue;
                }
                EnumFacing accessFace = face.getOpposite();
                IItemHandler handler = itemHandler(tile, accessFace);
                if (handler != null) {
                    for (int slot = 0; slot < handler.getSlots(); slot++) {
                        addAvailable(stacks, handler.getStackInSlot(slot));
                    }
                } else if (tile instanceof ISidedInventory) {
                    ISidedInventory sided = (ISidedInventory) tile;
                    for (int slot : sided.getSlotsForFace(accessFace)) {
                        ItemStack stack = sided.getStackInSlot(slot);
                        if (!stack.isEmpty() && sided.canExtractItem(slot, stack, accessFace)) {
                            addAvailable(stacks, stack);
                        }
                    }
                } else if (tile instanceof IInventory) {
                    IInventory inventory = (IInventory) tile;
                    for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
                        addAvailable(stacks, inventory.getStackInSlot(slot));
                    }
                }
            }
            TileSupremeTable table = supremeTable(player, tablePos);
            if (table != null && AE2OptionalBridge.loaded()) {
                AE2OptionalBridge.addAvailable(table, stacks);
            }
            return stacks;
        }

        private static void addAvailable(List<ItemStack> stacks, ItemStack stack) {
            if (!stack.isEmpty()) {
                stacks.add(stack.copy());
            }
        }

        private static boolean reserveOne(List<ItemStack> available, List<ItemStack> candidates, ItemStack stackToFill) {
            for (ItemStack stack : available) {
                if (canTake(stack, candidates, stackToFill)) {
                    stack.shrink(1);
                    return true;
                }
            }
            return false;
        }

        private static boolean canAccept(ContainerSupremeTable container, int slot, List<ItemStack> candidates,
                                         boolean fillExisting) {
            if (slot < 0 || slot >= SupremeTableInventory.SIZE || candidates.isEmpty()) {
                return false;
            }
            ItemStack existing = container.table().getStackInSlot(slot);
            return existing.isEmpty()
                    || (fillExisting && matchesAny(existing, candidates)
                    && existing.getCount() < slotLimit(container, existing));
        }

        private static int slotLimit(ContainerSupremeTable container, ItemStack stack) {
            return Math.min(container.table().getInventoryStackLimit(), stack.getMaxStackSize());
        }

        private static ItemStack takeOne(EntityPlayerMP player, BlockPos tablePos, List<ItemStack> candidates,
                                         ItemStack stackToFill) {
            ItemStack fromPlayer = takeOneFromPlayer(player, candidates, stackToFill);
            if (!fromPlayer.isEmpty()) {
                return fromPlayer;
            }
            for (EnumFacing face : STORAGE_FACES) {
                TileEntity tile = player.world.getTileEntity(tablePos.offset(face));
                if (tile == null) {
                    continue;
                }
                EnumFacing accessFace = face.getOpposite();
                ItemStack fromItemHandler = takeOneFromItemHandler(tile, accessFace, candidates, stackToFill);
                if (!fromItemHandler.isEmpty()) {
                    return fromItemHandler;
                }
                ItemStack fromInventory = takeOneFromInventory(tile, accessFace, candidates, stackToFill);
                if (!fromInventory.isEmpty()) {
                    return fromInventory;
                }
            }
            TileSupremeTable table = supremeTable(player, tablePos);
            if (table != null && AE2OptionalBridge.loaded()) {
                return AE2OptionalBridge.takeOne(player, table, candidates, stackToFill);
            }
            return ItemStack.EMPTY;
        }

        private static ItemStack takeOneFromPlayer(EntityPlayerMP player, List<ItemStack> candidates,
                                                   ItemStack stackToFill) {
            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                ItemStack stack = player.inventory.getStackInSlot(i);
                if (canTake(stack, candidates, stackToFill)) {
                    ItemStack out = stack.copy();
                    out.setCount(1);
                    stack.shrink(1);
                    player.inventory.markDirty();
                    return out;
                }
            }
            return ItemStack.EMPTY;
        }

        private static ItemStack takeOneFromItemHandler(TileEntity tile, EnumFacing side, List<ItemStack> candidates,
                                                        ItemStack stackToFill) {
            IItemHandler handler = itemHandler(tile, side);
            if (handler == null) {
                return ItemStack.EMPTY;
            }
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.getStackInSlot(slot);
                if (canTake(stack, candidates, stackToFill)) {
                    ItemStack extracted = handler.extractItem(slot, 1, false);
                    if (!extracted.isEmpty()) {
                        extracted.setCount(1);
                        return extracted;
                    }
                }
            }
            return ItemStack.EMPTY;
        }

        private static IItemHandler itemHandler(TileEntity tile, EnumFacing side) {
            if (tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)) {
                return tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
            }
            if (tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                return tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            }
            return null;
        }

        private static ItemStack takeOneFromInventory(TileEntity tile, EnumFacing side, List<ItemStack> candidates,
                                                      ItemStack stackToFill) {
            if (!(tile instanceof IInventory)) {
                return ItemStack.EMPTY;
            }
            IInventory inventory = (IInventory) tile;
            if (inventory instanceof ISidedInventory) {
                ISidedInventory sided = (ISidedInventory) inventory;
                for (int slot : sided.getSlotsForFace(side)) {
                    ItemStack stack = inventory.getStackInSlot(slot);
                    if (canTake(stack, candidates, stackToFill) && sided.canExtractItem(slot, stack, side)) {
                        return takeFromInventorySlot(inventory, slot);
                    }
                }
                return ItemStack.EMPTY;
            }
            for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (canTake(stack, candidates, stackToFill)) {
                    return takeFromInventorySlot(inventory, slot);
                }
            }
            return ItemStack.EMPTY;
        }

        private static ItemStack takeFromInventorySlot(IInventory inventory, int slot) {
            ItemStack taken = inventory.decrStackSize(slot, 1);
            if (!taken.isEmpty()) {
                taken.setCount(1);
                inventory.markDirty();
            }
            return taken;
        }

        private static boolean canTake(ItemStack stack, List<ItemStack> candidates, ItemStack stackToFill) {
            return !stack.isEmpty()
                    && (stackToFill.isEmpty() || ItemHandlerHelper.canItemStacksStack(stack, stackToFill))
                    && matchesAny(stack, candidates);
        }

        private static boolean matchesAny(ItemStack stack, List<ItemStack> candidates) {
            for (ItemStack candidate : candidates) {
                if (OreDictionary.itemMatches(candidate, stack, false)) {
                    return true;
                }
            }
            return false;
        }

        private static TileSupremeTable supremeTable(EntityPlayerMP player, BlockPos tablePos) {
            TileEntity tile = player.world.getTileEntity(tablePos);
            return tile instanceof TileSupremeTable ? (TileSupremeTable) tile : null;
        }

    }
}
