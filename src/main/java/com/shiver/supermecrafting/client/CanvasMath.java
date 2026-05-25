package com.shiver.supermecrafting.client;

public final class CanvasMath {
    private CanvasMath() {
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double zoomCenteredPan(double pan, double oldCell, double newCell, double cursor) {
        double grid = (cursor - pan) / oldCell;
        return cursor - grid * newCell;
    }

    public static double clampPan(double pan, int viewport, double cell, int cells, double edgePad) {
        double grid = cells * cell;
        double min = Math.min(edgePad, viewport - grid - edgePad);
        double max = edgePad;
        return clamp(pan, min, max);
    }
}
