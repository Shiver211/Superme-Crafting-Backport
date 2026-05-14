package com.shiver.supermecrafting.furnace;

/**
 * Which Region slot a hatch reads/writes. Routes item handler traffic:
 * INPUT accepts inserts and feeds slot 0; FUEL accepts only burnables
 * and feeds slot 1; OUTPUT only allows extraction from slot 2.
 */
public enum HatchRole {
    INPUT(Region.SLOT_INPUT),
    FUEL(Region.SLOT_FUEL),
    OUTPUT(Region.SLOT_OUTPUT);

    private final int slotIndex;

    HatchRole(int slotIndex) {
        this.slotIndex = slotIndex;
    }

    public int slotIndex() {
        return slotIndex;
    }
}
