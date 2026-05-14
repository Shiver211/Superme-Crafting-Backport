package com.shiver.supermecrafting.client;

/**
 * Supreme Table 屏幕平移/缩放变换的纯逻辑辅助类。
 * 没有 Minecraft / OpenGL 依赖，可由单元测试覆盖。
 */
public final class CanvasMath {
    private CanvasMath() {}

    /** 将值钳制到 [min, max]。 */
    public static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    public static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    /**
     * 钳制平移偏移量，使网格大致保持在画布内。
     * 允许 {@code edgePad} 像素的超调，以便用户可以看到网格边缘之外的画布背景。
     *
     * @param panOffset      当前平移偏移量（像素）。0 = 网格 (0,0) 在画布左侧。
     * @param canvasSize     画布宽度或高度（像素）。
     * @param cellSize       一个网格单元的大小（像素）。
     * @param gridSizeCells  此轴上的单元格数量。
     * @param edgePad        每个网格边缘可见的画布背景像素。
     * @return 钳制后的平移偏移量。
     */
    public static double clampPan(double panOffset, double canvasSize, double cellSize, int gridSizeCells, double edgePad) {
        double gridPx = gridSizeCells * cellSize;
        if (gridPx <= canvasSize) {
            return (canvasSize - gridPx) / 2.0;
        }
        double minPan = canvasSize - gridPx - edgePad;
        double maxPan = edgePad;
        return clamp(panOffset, minPan, maxPan);
    }

    /** 向后兼容的重载（无边缘填充）。 */
    public static double clampPan(double panOffset, double canvasSize, double cellSize, int gridSizeCells) {
        return clampPan(panOffset, canvasSize, cellSize, gridSizeCells, 0.0);
    }

    /**
     * 计算当 {@code cellSize} 从 {@code oldCellSize} 变为 {@code newCellSize} 时，
     * 保持光标下的网格坐标固定的平移偏移量。
     *
     * @param oldPan       之前的平移偏移量。
     * @param oldCellSize  之前的单元格大小。
     * @param newCellSize  新的单元格大小。
     * @param cursorRelToCanvas  光标相对于画布左上角的位置。
     */
    public static double zoomCenteredPan(double oldPan, double oldCellSize, double newCellSize,
                                         double cursorRelToCanvas) {
        double gridUnderCursor = (cursorRelToCanvas - oldPan) / oldCellSize;
        return cursorRelToCanvas - gridUnderCursor * newCellSize;
    }
}
