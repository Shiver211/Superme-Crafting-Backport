package com.shiver.supermecrafting.recipe;

import com.shiver.supermecrafting.table.SupremeTableInventory;
import com.shiver.supermecrafting.table.TileSupremeTable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class SupremeRecipeScriptExporter {
    private static final String FILE_NAME = "supreme_crafting_generated.zs";

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
        for (int y = bounds.minY; y <= bounds.maxY; y++) {
            out.append("    [");
            for (int x = bounds.minX; x <= bounds.maxX; x++) {
                if (x > bounds.minX) {
                    out.append(", ");
                }
                ItemStack input = inventory.get(SupremeTableInventory.indexOf(x, y));
                out.append(input.isEmpty() ? "null" : stack(input, false));
            }
            out.append("]");
            if (y < bounds.maxY) {
                out.append(",");
            }
            out.append("\n");
        }
        out.append("]);\n\n");
        return out.toString();
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
