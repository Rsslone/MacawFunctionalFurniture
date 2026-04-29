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

    static {
        // ── pult (bookcase / display) ──────────────────────────────────────
        // base "pult" omitted — purely decorative, no storage
        put(3, Shape.NONE, "pult_1", "pult_2");
        put(2, Shape.NONE, "pult_3");
        put(1, Shape.NONE, "pult_4");

        // ── boxes ──────────────────────────────────────────────────────────
        put(3, Shape.NONE, "box", "box_2");

        // ── nightstands ────────────────────────────────────────────────────
        put(1, Shape.NONE, "nightstand");
        put(2, Shape.NONE, "nightstand_2", "nightstand_3");
        put(3, Shape.NONE, "nightstand_4", "nightstand_5", "nightstand_6",
              "nightstand_7", "nightstand_8");

        // ── dressers (FurnitureDresser: 2-wide along the X/Z perpendicular axis) ──
        put(6, Shape.DRESSER, "dresser", "dresser_box",
              "dresser_3", "dresser_4", "dresser_5", "dresser_6",
              "dresser_7", "dresser_8", "dresser_9", "dresser_10", "dresser_11");
        put(4, Shape.DRESSER, "dresser_12");
        put(2, Shape.DRESSER, "dresser_13", "dresser_17", "dresser_18");
        put(3, Shape.DRESSER, "dresser_15", "dresser_16");
        // dresser_14 omitted

        // ── desks (also FurnitureDresser shape) ───────────────────────────
        put(2, Shape.DRESSER, "desk", "desk_6");
        put(3, Shape.DRESSER, "desk_2", "desk_5");

        // ── cupboards (FurnitureCupboard: 2-tall) ─────────────────────────
        put(6, Shape.CUPBOARD, "cupboard", "cupboard_2", "cupboard_4", "cupboard_5",
              "cupboard_6", "cupboard_7", "cupboard_9");
        put(4, Shape.CUPBOARD, "cupboard_3", "cupboard_8");

        // ── wardrobes / cabinets (FurnitureWardrobe: 2-wide × 2-tall) ─────
        put(6, Shape.WARDROBE, "furniture_1", "furniture_2", "furniture_3", "furniture_4",
              "furniture_5", "furniture_6", "furniture_7", "furniture_8",
              "furniture_9");
    }

    private FurnitureRegistry() {}

    private static void put(int rows, Shape shape, String... baseNames) {
        for (String base : baseNames) {
            for (String prefix : WOOD_PREFIXES) {
                String path = prefix + base;
                ROWS.put(path, rows);
                SHAPES.put(path, shape);
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
