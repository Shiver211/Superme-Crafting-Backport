package com.shiver.supermecrafting.recipe;

import com.shiver.supermecrafting.table.SupremeTableInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;

import java.util.List;
import java.util.Map;

/**
 * Pattern parser and matcher for Supreme Shaped recipes.
 * Like vanilla ShapedRecipe but sized for the 81x81 Supreme Table.
 * Strict-size matching: matches() returns true only when input bbox equals width x height.
 */
public final class SupremeShapedPattern {
    public static final int MAX_SIZE = SupremeTableInventory.WIDTH; // 81

    private final int width;
    private final int height;
    private final NonNullList<Ingredient> ingredients;
    private final int ingredientCount;
    private final boolean symmetrical;

    public SupremeShapedPattern(int width, int height, NonNullList<Ingredient> ingredients) {
        this.width = width;
        this.height = height;
        this.ingredients = ingredients;
        int count = 0;
        for (Ingredient i : ingredients) {
            if (!testIngredientEmpty(i)) count++;
        }
        this.ingredientCount = count;
        this.symmetrical = checkSymmetrical();
    }

    private static boolean testIngredientEmpty(Ingredient ing) {
        if (ing == Ingredient.EMPTY) return true;
        return ing.getMatchingStacks().length == 0;
    }

    /** 检查图案是否等于其 180 度旋转（对称则跳过镜像检查）。 */
    private boolean checkSymmetrical() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Ingredient a = ingredients.get(x + y * width);
                Ingredient b = ingredients.get((width - 1 - x) + (height - 1 - y) * width);
                if (!testIngredientEmpty(a) != !testIngredientEmpty(b)) return false;
                if (!testIngredientEmpty(a) && !a.isSimple() && !b.isSimple()) {
                    // 复杂配料需要比较匹配栈
                    if (!ingredientEquals(a, b)) return false;
                }
            }
        }
        return true;
    }

    private static boolean ingredientEquals(Ingredient a, Ingredient b) {
        ItemStack[] aStacks = a.getMatchingStacks();
        ItemStack[] bStacks = b.getMatchingStacks();
        if (aStacks.length != bStacks.length) return false;
        for (int i = 0; i < aStacks.length; i++) {
            if (!ItemStack.areItemsEqual(aStacks[i], bStacks[i])) return false;
        }
        return true;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public NonNullList<Ingredient> getIngredients() { return ingredients; }

    public Ingredient getIngredientAt(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return Ingredient.EMPTY;
        return ingredients.get(x + y * width);
    }

    /**
     * Matches an InventoryCrafting that was built from a Supreme Table.
     * The inventory must be exactly width x height.
     * Counts non-empty cells and compares with ingredientCount, then checks each cell.
     */
    public boolean matches(net.minecraft.inventory.InventoryCrafting inv) {
        if (inv.getWidth() != width || inv.getHeight() != height) return false;

        int inputCount = 0;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            if (!inv.getStackInSlot(i).isEmpty()) inputCount++;
        }
        if (inputCount != ingredientCount) return false;

        // 尝试正常方向
        if (matchOriented(inv, false)) return true;
        // 对称图案跳过镜像检查
        if (symmetrical) return false;
        // 尝试镜像方向
        return matchOriented(inv, true);
    }

    private boolean matchOriented(net.minecraft.inventory.InventoryCrafting inv, boolean mirrored) {
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Ingredient ingredient = mirrored
                        ? ingredients.get((width - col - 1) + row * width)
                        : ingredients.get(col + row * width);
                ItemStack inputStack = inv.getStackInSlot(col + row * inv.getWidth());
                if (!ingredient.apply(inputStack)) return false;
            }
        }
        return true;
    }

    /**
     * Parse a pattern from JSON data (key map + pattern strings).
     * Strips leading/trailing whitespace rows/columns.
     */
    public static SupremeShapedPattern parse(Map<String, Ingredient> key, List<String> pattern) {
        String[] rows = shrink(pattern);
        if (rows.length == 0) {
            throw new IllegalArgumentException("Pattern is empty after trim");
        }
        int w = rows[0].length();
        int h = rows.length;
        if (w > MAX_SIZE || h > MAX_SIZE) {
            throw new IllegalArgumentException("Pattern exceeds " + MAX_SIZE + "x" + MAX_SIZE);
        }

        NonNullList<Ingredient> ingredients = NonNullList.withSize(w * h, Ingredient.EMPTY);
        for (int r = 0; r < rows.length; r++) {
            String row = rows[r];
            for (int c = 0; c < row.length(); c++) {
                char ch = row.charAt(c);
                if (ch == ' ' || ch == '.') continue;
                Ingredient ing = key.get(String.valueOf(ch));
                if (ing == null) {
                    throw new IllegalArgumentException("Pattern references symbol '" + ch + "' but it's not in the key");
                }
                ingredients.set(c + r * w, ing);
            }
        }
        return new SupremeShapedPattern(w, h, ingredients);
    }

    /** Strip leading/trailing all-space rows and columns. Same algorithm as vanilla. */
    static String[] shrink(List<String> list) {
        int firstCol = Integer.MAX_VALUE;
        int lastCol = 0;
        int leadingEmpty = 0;
        int trailingEmpty = 0;

        for (int idx = 0; idx < list.size(); idx++) {
            String row = list.get(idx);
            firstCol = Math.min(firstCol, firstNonSpace(row));
            int last = lastNonSpace(row);
            lastCol = Math.max(lastCol, last);
            if (last < 0) {
                if (leadingEmpty == idx) leadingEmpty++;
                trailingEmpty++;
            } else {
                trailingEmpty = 0;
            }
        }

        if (list.size() == trailingEmpty) return new String[0];
        String[] out = new String[list.size() - trailingEmpty - leadingEmpty];
        for (int i = 0; i < out.length; i++) {
            out[i] = list.get(i + leadingEmpty).substring(firstCol, lastCol + 1);
        }
        return out;
    }

    private static int firstNonSpace(String s) {
        int i = 0;
        while (i < s.length() && s.charAt(i) == ' ') i++;
        return i;
    }

    private static int lastNonSpace(String s) {
        int i = s.length() - 1;
        while (i >= 0 && s.charAt(i) == ' ') i--;
        return i;
    }
}
