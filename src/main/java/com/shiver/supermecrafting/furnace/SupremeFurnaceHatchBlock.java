package com.shiver.supermecrafting.furnace;

/**
 * Casing variant that exposes a HatchRole-filtered item handler to automation.
 * Extends SupremeFurnaceCasingBlock so it inherits form/disassemble/menu-open behavior.
 */
public class SupremeFurnaceHatchBlock extends SupremeFurnaceCasingBlock {
    private final HatchRole role;

    public SupremeFurnaceHatchBlock(String name, HatchRole role) {
        super(name);
        this.role = role;
    }

    public HatchRole getRole() {
        return role;
    }
}
