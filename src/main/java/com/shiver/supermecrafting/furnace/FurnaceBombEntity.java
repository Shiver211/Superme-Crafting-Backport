package com.shiver.supermecrafting.furnace;

import com.shiver.supermecrafting.Tags;
import com.shiver.supermecrafting.registry.ModBlocks;
import com.shiver.supermecrafting.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.registry.EntityRegistry;

/**
 * Thrown Furnace Bomb. On hit it destroys and voids every block in a size-cube
 * volume, then places casings and forms the furnace.
 */
public class FurnaceBombEntity extends EntityThrowable {
    /** Flag set: client sync, no neighbour updates, no drops. */
    private static final int FORCE_FLAGS = 2 | 16 | 4; // UPDATE_CLIENTS | UPDATE_KNOWN_SHAPE | NO_RERENDER

    private int bombSize = 32;

    public FurnaceBombEntity(World world) {
        super(world);
    }

    public FurnaceBombEntity(World world, EntityLivingBase thrower) {
        super(world, thrower);
    }

    public void setBombSize(int size) {
        this.bombSize = size;
    }

    public int getBombSize() {
        return bombSize;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
    }

    @Override
    public void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("BombSize")) {
            this.bombSize = compound.getInteger("BombSize");
        }
    }

    @Override
    public void writeEntityToNBT(net.minecraft.nbt.NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("BombSize", this.bombSize);
    }

    @Override
    protected void onImpact(net.minecraft.util.math.RayTraceResult result) {
        if (world.isRemote) return;
        if (!(world instanceof WorldServer)) return;

        WorldServer server = (WorldServer) world;
        BlockPos landing = getPosition();
        placeShellAndForm(server, landing, getBombSize());
        setDead();
    }

    private void placeShellAndForm(WorldServer level, BlockPos landing, int size) {
        int halfSize = size / 2;
        int minX = landing.getX() - halfSize;
        int maxX = minX + size - 1;
        int minY = landing.getY();
        int maxY = minY + size - 1;
        int minZ = landing.getZ() - halfSize;
        int maxZ = minZ + size - 1;

        IBlockState casing = ModBlocks.SUPREME_FURNACE_CASING.getDefaultState();
        IBlockState air = Blocks.AIR.getDefaultState();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();

        // 1. Shell — force casing on every cell of the six faces
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                forceSet(level, m.setPos(x, minY, z), casing);
                forceSet(level, m.setPos(x, maxY, z), casing);
            }
        }
        for (int y = minY + 1; y < maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                forceSet(level, m.setPos(x, y, minZ), casing);
                forceSet(level, m.setPos(x, y, maxZ), casing);
            }
            for (int z = minZ + 1; z < maxZ; z++) {
                forceSet(level, m.setPos(minX, y, z), casing);
                forceSet(level, m.setPos(maxX, y, z), casing);
            }
        }

        // 2. Interior — void anything that isn't already air
        for (int x = minX + 1; x < maxX; x++) {
            for (int y = minY + 1; y < maxY; y++) {
                for (int z = minZ + 1; z < maxZ; z++) {
                    m.setPos(x, y, z);
                    if (level.isAirBlock(m)) continue;
                    forceSet(level, m, air);
                }
            }
        }

        // 3. Form
        EntityPlayer owner = thrower instanceof EntityPlayer ? (EntityPlayer) thrower : null;
        FurnaceFormation.Result result = FurnaceFormation.tryForm(level, landing, owner);
        if (owner != null) {
            if (result instanceof FurnaceFormation.Result.Success) {
                FurnaceFormation.Result.Success s = (FurnaceFormation.Result.Success) result;
                owner.sendMessage(new TextComponentString("Supreme Furnace formed: "
                        + s.region().size() + "x" + s.region().size() + "x" + s.region().size()
                        + " front=" + s.region().getFront()));
            } else if (result instanceof FurnaceFormation.Result.Failure) {
                FurnaceFormation.Result.Failure f = (FurnaceFormation.Result.Failure) result;
                owner.sendMessage(new TextComponentString("Furnace Bomb form failed: " + f.reason()));
            }
        }
    }

    private static void forceSet(WorldServer level, BlockPos pos, IBlockState target) {
        TileEntity te = level.getTileEntity(pos);
        if (te instanceof IInventory) {
            ((IInventory) te).clear();
        }
        level.setBlockState(pos, target, FORCE_FLAGS);
    }

    /**
     * Register the entity type. Call during preInit.
     */
    public static void registerEntity() {
        EntityRegistry.registerModEntity(
                new net.minecraft.util.ResourceLocation(Tags.MOD_ID, "furnace_bomb"),
                FurnaceBombEntity.class,
                "furnace_bomb",
                0,
                Tags.MOD_ID,
                64,  // tracking range
                10,  // update frequency
                true // sends velocity updates
        );
    }
}
