package com.shiver.supermecrafting.block;

import net.minecraftforge.common.property.IUnlistedProperty;

public class FurnaceRenderRegionProperty implements IUnlistedProperty<FurnaceRenderRegion> {
    @Override
    public String getName() {
        return "render_region";
    }

    @Override
    public boolean isValid(FurnaceRenderRegion value) {
        return true;
    }

    @Override
    public Class<FurnaceRenderRegion> getType() {
        return FurnaceRenderRegion.class;
    }

    @Override
    public String valueToString(FurnaceRenderRegion value) {
        return value == null ? "null" : value.min() + ":" + value.max() + ":" + value.pos() + ":" + value.front() + ":" + value.lit();
    }
}
