package com.shiver.supremecrafting.block;

import com.shiver.supremecrafting.furnace.HatchRole;
import com.shiver.supremecrafting.furnace.TileFurnaceHatch;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockSupremeFurnaceHatch extends BlockSupremeFurnaceCasing implements ITileEntityProvider {
    private final HatchRole role;

    public BlockSupremeFurnaceHatch(String name, HatchRole role) {
        super(name);
        this.role = role;
    }

    public HatchRole getRole() {
        return role;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        TileFurnaceHatch tile = new TileFurnaceHatch();
        tile.setRole(role);
        return tile;
    }
}
