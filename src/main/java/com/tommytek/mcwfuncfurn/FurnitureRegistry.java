package com.tommytek.mcwfuncfurn;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps Macaw furniture block registry paths to inventory row counts.
 *
 * <p>Each row holds 9 slots (maximum 6 rows / 54 slots, matching the vanilla
 * double-chest texture {@code generic_54.png}).
 *
 * <p>All six wood prefixes are registered for every base block name.
 *
 * <p>Also classifies each block by visual {@link Shape} so the multiblock
 * placement logic (see {@code FurnitureBaseMixin}) knows where to spawn
 * invisible filler blocks for the parts of the model that hang outside the
 * master cell.
 */
public final class FurnitureRegistry {

    public static final String MACAW_NAMESPACE = "mcwfurnitures";

    /**
     * Visual footprint of a piece of furniture, used to compute filler-block
     * positions.  Coordinates are in world space, derived from the master
     * block's {@link EnumFacing}.
     */
    public enum Shape {
        /** Single 1×1×1 block — no fillers required. */
        NONE,
        /** 2 wide along the axis perpendicular to FACING (e.g. dressers, desks). */
        DRESSER,
        /** 1 wide × 2 tall (e.g. cupboards). */
        CUPBOARD,
        /** 2 wide × 2 tall (e.g. furniture_1 … furniture_9 wardrobes). */
        WARDROBE;

        /**
         * Returns the offsets (relative to the master block's position) at
         * which filler blocks should be placed for a master that faces
         * {@code facing}.  The {@code facing} parameter is the value the
         * Macaw block stores in its {@code FACING} property — this is the
         * direction the front of the furniture points.
         */
        public BlockPos[] fillerOffsets(EnumFacing facing) {
            // Axis perpendicular to FACING (left/right of the block).
            // For NORTH/SOUTH FACING the overhang runs along ±X.
            // For EAST/WEST  FACING the overhang runs along ±Z.
            boolean axisX = facing.getAxis() == EnumFacing.Axis.Z;
            int dxL = axisX ? -1 : 0;
            int dzL = axisX ?  0 : -1;
            int dxR = -dxL;
            int dzR = -dzL;

            switch (this) {
                case DRESSER:
                    return new BlockPos[] {
                        new BlockPos(dxL, 0, dzL),
                        new BlockPos(dxR, 0, dzR)
                    };
                case CUPBOARD:
                    return new BlockPos[] {
                        new BlockPos(0, 1, 0)
                    };
                case WARDROBE:
                    return new BlockPos[] {
                        new BlockPos(0,   1, 0),
                        new BlockPos(dxL, 0, dzL),
                        new BlockPos(dxR, 0, dzR),
                        new BlockPos(dxL, 1, dzL),
                        new BlockPos(dxR, 1, dzR)
                    };
                case NONE:
                default:
                    return new BlockPos[0];
            }
        }
    }

    private static final String[] WOOD_PREFIXES = {
        "", "spruce_", "birch_", "jungle_", "acacia_", "dark_oak_"
    };

    private static final Map<String, Integer> ROWS = new HashMap<>();
    private static final Map<String, Shape> SHAPES = new HashMap<>();
    private static final Map<String, String> NAMES = new HashMap<>();

    static {
        // ── pult (bookcase / display) ──────────────────────────────────────
        // base "pult" omitted — purely decorative, no storage
        put(3, Shape.NONE, "Cupboard Counter",       "pult_1", "pult_2");
        put(2, Shape.NONE, "Double Drawer Counter",  "pult_3");
        put(1, Shape.NONE, "Drawer Counter",         "pult_4");

        // ── boxes ──────────────────────────────────────────────────────────
        put(3, Shape.NONE, "Kitchen Cabinet",        "box", "box_2");

        // ── nightstands ────────────────────────────────────────────────────
        put(1, Shape.NONE, "Drawer Nightstand",      "nightstand");
        put(2, Shape.NONE, "Double Drawer Nightstand", "nightstand_2");
        put(2, Shape.NONE, "Bookcase Nightstand",    "nightstand_3");
        put(3, Shape.NONE, "Double Kitchen Cabinet", "nightstand_4");
        put(3, Shape.NONE, "Drawer Cupboard",        "nightstand_5");
        put(3, Shape.NONE, "Kitchen Cabinet",        "nightstand_6");
        put(3, Shape.NONE, "Glass Cabinet",          "nightstand_7", "nightstand_8");

        // ── dressers (FurnitureDresser: 2-wide along the X/Z perpendicular axis) ──
        put(6, Shape.DRESSER, "Classic Dresser",         "dresser");
        put(6, Shape.DRESSER, "Cupboard Dresser",        "dresser_box");
        put(6, Shape.DRESSER, "Double Drawer Dresser",   "dresser_3");
        put(6, Shape.DRESSER, "Triple Drawer Dresser",   "dresser_4", "dresser_5");
        put(6, Shape.DRESSER, "Bookcase Dresser",        "dresser_6", "dresser_11");
        put(6, Shape.DRESSER, "Complex Dresser",         "dresser_7", "dresser_8");
        put(6, Shape.DRESSER, "Bookshelf Dresser",       "dresser_9", "dresser_10");
        put(4, Shape.DRESSER, "Complex Shelving Unit",   "dresser_12");
        put(2, Shape.DRESSER, "Drawer Shelving Unit",    "dresser_13");
        put(3, Shape.DRESSER, "Sapling Cabinet Dresser", "dresser_15", "dresser_16");
        put(2, Shape.DRESSER, "Sapling Drawer Dresser",  "dresser_17", "dresser_18");
        // dresser_14 omitted

        // ── desks (also FurnitureDresser shape) ───────────────────────────
        put(2, Shape.DRESSER, "Drawer Desk",   "desk", "desk_6");
        put(3, Shape.DRESSER, "Cupboard Desk", "desk_2", "desk_5");

        // ── cupboards (FurnitureCupboard: 2-tall) ─────────────────────────
        put(6, Shape.CUPBOARD, "Cupboard",         "cupboard", "cupboard_6");
        put(6, Shape.CUPBOARD, "Modern Cupboard",  "cupboard_2", "cupboard_7");
        put(6, Shape.CUPBOARD, "Complex Cupboard", "cupboard_4", "cupboard_9");
        put(6, Shape.CUPBOARD, "Double Cupboard",  "cupboard_5");
        put(4, Shape.CUPBOARD, "Bookshelf",        "cupboard_3");
        put(4, Shape.CUPBOARD, "Tall Bookshelf",   "cupboard_8");

        // ── wardrobes / cabinets (FurnitureWardrobe: 2-wide × 2-tall) ─────
        put(6, Shape.WARDROBE, "Wardrobe",         "furniture_1");
        put(6, Shape.WARDROBE, "Modern Wardrobe",  "furniture_2");
        put(6, Shape.WARDROBE, "Complex Wardrobe", "furniture_3", "furniture_9");
        put(6, Shape.WARDROBE, "Display Wardrobe", "furniture_4", "furniture_8");
        put(6, Shape.WARDROBE, "Shelf Wardrobe",   "furniture_5");
        put(6, Shape.WARDROBE, "Cupboard Wardrobe","furniture_6");
        put(6, Shape.WARDROBE, "Tall Bookshelf",   "furniture_7");
    }

    private FurnitureRegistry() {}

    private static void put(int rows, Shape shape, String name, String... baseNames) {
        for (String base : baseNames) {
            for (String prefix : WOOD_PREFIXES) {
                String path = prefix + base;
                ROWS.put(path, rows);
                SHAPES.put(path, shape);
                NAMES.put(path, name);
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

    /** @return display name for the given path, or {@code "Furniture"} if not found. */
    public static String getName(String blockPath) {
        String name = NAMES.get(blockPath);
        return name != null ? name : "Furniture";
    }

    /**
     * @return the {@link Shape} classification for the given Macaw block, or
     *         {@link Shape#NONE} if the block is not handled (or has no
     *         overhang).  Used by the multiblock placement logic.
     */
    public static Shape getShape(ResourceLocation regName) {
        if (regName == null || !MACAW_NAMESPACE.equals(regName.getNamespace())) {
            return Shape.NONE;
        }
        Shape s = SHAPES.get(regName.getPath());
        return s == null ? Shape.NONE : s;
    }
}
