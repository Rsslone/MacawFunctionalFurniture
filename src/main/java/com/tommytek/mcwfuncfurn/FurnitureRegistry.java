package com.tommytek.mcwfuncfurn;

import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps Macaw furniture block registry paths to inventory row counts.
 *
 * <p>Each row holds 9 slots (maximum 6 rows / 54 slots, matching the vanilla
 * double-chest texture {@code generic_54.png}).
 *
 * <p>All six wood prefixes are registered for every base block name.
 */
public final class FurnitureRegistry {

    public static final String MACAW_NAMESPACE = "mcwfurnitures";

    private static final String[] WOOD_PREFIXES = {
        "", "spruce_", "birch_", "jungle_", "acacia_", "dark_oak_"
    };

    private static final Map<String, Integer> ROWS = new HashMap<>();

    static {
        // ── pult (bookcase / display) ──────────────────────────────────────
        // base "pult" omitted — purely decorative, no storage
        put(3, "pult_1", "pult_2");
        put(2, "pult_3");
        put(1, "pult_4");

        // ── boxes ──────────────────────────────────────────────────────────
        put(3, "box", "box_2");

        // ── nightstands ────────────────────────────────────────────────────
        put(1, "nightstand");
        put(2, "nightstand_2", "nightstand_3");
        put(3, "nightstand_4", "nightstand_5", "nightstand_6",
              "nightstand_7", "nightstand_8");

        // ── dressers ───────────────────────────────────────────────────────
        put(6, "dresser", "dresser_box",
              "dresser_3", "dresser_4", "dresser_5", "dresser_6",
              "dresser_7", "dresser_8", "dresser_9", "dresser_10", "dresser_11");
        put(4, "dresser_12");
        put(2, "dresser_13", "dresser_17", "dresser_18");
        put(3, "dresser_15", "dresser_16");
        // dresser_14 omitted

        // ── desks ──────────────────────────────────────────────────────────
        put(2, "desk", "desk_6");
        put(3, "desk_2", "desk_5");

        // ── cupboards ──────────────────────────────────────────────────────
        put(6, "cupboard", "cupboard_2", "cupboard_4", "cupboard_5",
              "cupboard_6", "cupboard_7", "cupboard_9");
        put(4, "cupboard_3", "cupboard_8");

        // ── furniture (wardrobes / cabinets) ───────────────────────────────
        put(6, "furniture_1", "furniture_2", "furniture_3", "furniture_4",
              "furniture_5", "furniture_6", "furniture_7", "furniture_8",
              "furniture_9");
    }

    private FurnitureRegistry() {}

    private static void put(int rows, String... baseNames) {
        for (String base : baseNames) {
            for (String prefix : WOOD_PREFIXES) {
                ROWS.put(prefix + base, rows);
            }
        }
    }

    /** @return {@code true} if the block belongs to Macaw and has a row mapping. */
    public static boolean isHandled(ResourceLocation regName) {
        return regName != null
            && MACAW_NAMESPACE.equals(regName.getNamespace())
            && ROWS.containsKey(regName.getPath());
    }

    /** @return row count (1–6) for the given path, or {@code 0} if not handled. */
    public static int getRows(String blockPath) {
        Integer rows = ROWS.get(blockPath);
        return rows == null ? 0 : rows;
    }
}
