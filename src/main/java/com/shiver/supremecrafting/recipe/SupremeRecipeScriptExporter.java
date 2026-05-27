package com.shiver.supremecrafting.recipe;

import com.shiver.supremecrafting.table.SupremeTableInventory;
import com.shiver.supremecrafting.table.TileSupremeTable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class SupremeRecipeScriptExporter {
    private static final String FILE_NAME = "supreme_crafting_generated.zs";
    private static final String EMPTY_TOKEN = "___";
    private static final char[] TOKEN_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    private SupremeRecipeScriptExporter() {
    }

    public static String export(TileSupremeTable table, ItemStack output) {
        SupremeTableInventory inventory = table.supremeInventory();
        SupremeCraftingMatcher.Bounds bounds = SupremeCraftingMatcher.bounds(inventory);
        if (bounds == null) {
            throw new ExportException("supreme_crafting.message.script_export.empty_table");
        }
        File file = scriptFile();
        String name = nextName(file);
        append(file, recipe(name, output, inventory, bounds));
        return "scripts/" + FILE_NAME;
    }

    private static String recipe(String name, ItemStack output, SupremeTableInventory inventory,
                                 SupremeCraftingMatcher.Bounds bounds) {
        StringBuilder out = new StringBuilder();
        if (!scriptFile().isFile()) {
            out.append("import mods.supreme_crafting.SupremeCrafting;\n\n");
        }
        out.append("SupremeCrafting.addShaped(\"").append(name).append("\", ")
                .append(stack(output, true)).append(", ")
                .append(bounds.minX).append(", ").append(bounds.minY).append(", [\n");
        List<ItemStack> inputs = uniqueInputs(inventory, bounds);
        for (int y = bounds.minY; y <= bounds.maxY; y++) {
            out.append("    \"");
            for (int x = bounds.minX; x <= bounds.maxX; x++) {
                if (x > bounds.minX) {
                    out.append(" ");
                }
                ItemStack input = inventory.get(SupremeTableInventory.indexOf(x, y));
                out.append(input.isEmpty() ? EMPTY_TOKEN : token(indexOf(inputs, input)));
            }
            out.append("\"");
            if (y < bounds.maxY) {
                out.append(",");
            }
            out.append("\n");
        }
        out.append("], [\n");
        for (int i = 0; i < inputs.size(); i++) {
            out.append("    \"").append(token(i)).append("\"");
            if (i < inputs.size() - 1) {
                out.append(",");
            }
            out.append("\n");
        }
        out.append("], [\n");
        for (int i = 0; i < inputs.size(); i++) {
            out.append("    ").append(stack(inputs.get(i), false));
            if (i < inputs.size() - 1) {
                out.append(",");
            }
            out.append("\n");
        }
        out.append("]);\n\n");
        return out.toString();
    }

    private static List<ItemStack> uniqueInputs(SupremeTableInventory inventory, SupremeCraftingMatcher.Bounds bounds) {
        List<ItemStack> inputs = new ArrayList<>();
        for (int y = bounds.minY; y <= bounds.maxY; y++) {
            for (int x = bounds.minX; x <= bounds.maxX; x++) {
                ItemStack input = inventory.get(SupremeTableInventory.indexOf(x, y));
                if (!input.isEmpty() && indexOf(inputs, input) < 0) {
                    inputs.add(input.copy());
                }
            }
        }
        return inputs;
    }

    private static int indexOf(List<ItemStack> inputs, ItemStack stack) {
        for (int i = 0; i < inputs.size(); i++) {
            if (sameStack(inputs.get(i), stack)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean sameStack(ItemStack a, ItemStack b) {
        return ItemStack.areItemsEqual(a, b) && ItemStack.areItemStackTagsEqual(a, b);
    }

    private static String token(int index) {
        if (index < 0 || index >= TOKEN_CHARS.length * TOKEN_CHARS.length * TOKEN_CHARS.length) {
            throw new ExportException("supreme_crafting.message.script_export.too_many_ingredients");
        }
        int first = index / (TOKEN_CHARS.length * TOKEN_CHARS.length);
        int second = (index / TOKEN_CHARS.length) % TOKEN_CHARS.length;
        int third = index % TOKEN_CHARS.length;
        return "" + TOKEN_CHARS[first] + TOKEN_CHARS[second] + TOKEN_CHARS[third];
    }

    private static String stack(ItemStack stack, boolean includeCount) {
        ResourceLocation id = stack.getItem().getRegistryName();
        if (id == null) {
            throw new ExportException("supreme_crafting.message.script_export.no_registry_name");
        }
        String out = "<" + id + ">";
        int meta = stack.getMetadata();
        if (meta != 0) {
            out = "<" + id + ":" + meta + ">";
        }
        if (stack.hasTagCompound()) {
            out += ".withTag(" + stack.getTagCompound() + ")";
        }
        if (includeCount && stack.getCount() > 1) {
            out += " * " + stack.getCount();
        }
        return out;
    }

    private static File scriptFile() {
        File scripts = new File(Loader.instance().getConfigDir().getParentFile(), "scripts");
        return new File(scripts, FILE_NAME);
    }

    private static String nextName(File file) {
        int index = 1;
        if (file.isFile()) {
            try {
                String text = new String(java.nio.file.Files.readAllBytes(file.toPath()), java.nio.charset.StandardCharsets.UTF_8);
                while (text.contains("\"generated_" + index + "\"")) {
                    index++;
                }
            } catch (IOException e) {
                throw new ExportException("supreme_crafting.message.script_export.read_failed");
            }
        }
        return "generated_" + index;
    }

    private static void append(File file, String text) {
        File parent = file.getParentFile();
        if (!parent.isDirectory() && !parent.mkdirs()) {
            throw new ExportException("supreme_crafting.message.script_export.mkdir_failed");
        }
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write(text);
        } catch (IOException e) {
            throw new ExportException("supreme_crafting.message.script_export.write_failed");
        }
    }

    public static class ExportException extends RuntimeException {
        private final String key;
        private final Object[] args;

        private ExportException(String key, Object... args) {
            this.key = key;
            this.args = args;
        }

        public String key() {
            return key;
        }

        public Object[] args() {
            return args;
        }
    }
}
