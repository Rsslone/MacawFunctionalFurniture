package com.tommytek.mcwfuncfurn.block;

import com.tommytek.mcwfuncfurn.FurnitureRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Invisible "filler" block placed in the cells that a Macaw furniture model
 * visually occupies.
 *
 * <p>Interactions are forwarded to the master block, whose position is encoded
 * directly in this block's state via {@link MasterOffset} — making lookups O(1)
 *
 * <p>When the master breaks it tears down its fillers, and when a filler breaks
 * it tears down the master (which then drops the inventory and the master's ItemBlock).
 * A {@link ThreadLocal} guard prevents recursion between the two paths.
 */
public class FurnitureFillerBlock extends Block {

    /**
     * Offset from this filler's position to its master's position.  Encodes
     * every distinct filler→master vector across all furniture shapes:
     * <ul>
     *   <li>side fillers of a dresser/desk/wardrobe → cardinal horizontal</li>
     *   <li>top filler of a cupboard/wardrobe → DOWN</li>
     *   <li>upper-corner fillers of a wardrobe → DOWN + cardinal horizontal</li>
     * </ul>
     */
    public enum MasterOffset implements IStringSerializable {
        NORTH     ("north",       0,  0, -1),
        SOUTH     ("south",       0,  0,  1),
        EAST      ("east",        1,  0,  0),
        WEST      ("west",       -1,  0,  0),
        DOWN      ("down",        0, -1,  0),
        DOWN_NORTH("down_north",  0, -1, -1),
        DOWN_SOUTH("down_south",  0, -1,  1),
        DOWN_EAST ("down_east",   1, -1,  0),
        DOWN_WEST ("down_west",  -1, -1,  0);

        public final int dx, dy, dz;
        private final String name;

        MasterOffset(String n, int dx, int dy, int dz) {
            this.name = n;
            this.dx = dx;
            this.dy = dy;
            this.dz = dz;
        }

        @Override public String getName() { return name; }

        /** @return matching enum constant, or {@code null} if no exact match. */
        @Nullable
        public static MasterOffset of(int dx, int dy, int dz) {
            for (MasterOffset mo : values()) {
                if (mo.dx == dx && mo.dy == dy && mo.dz == dz) return mo;
            }
            return null;
        }
    }

    public static final PropertyEnum<MasterOffset> MASTER_OFFSET =
        PropertyEnum.create("master_offset", MasterOffset.class);

    /**
     * Recursion guard: re-entered during cascading destruction (master ↔ fillers).
     */
    public static final ThreadLocal<Boolean> BREAKING =
        ThreadLocal.withInitial(() -> Boolean.FALSE);

    public FurnitureFillerBlock() {
        super(Material.WOOD);
        setHardness(1.0F);
        setResistance(50.0F);
        setSoundType(SoundType.WOOD);
        setHarvestLevel("axe", 0);
        setDefaultState(blockState.getBaseState().withProperty(MASTER_OFFSET, MasterOffset.NORTH));
    }

    // ────────────────────────────────────────────────────────────────────
    // State / rendering — block is logically present but visually absent.
    // ────────────────────────────────────────────────────────────────────

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, MASTER_OFFSET);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        MasterOffset[] all = MasterOffset.values();
        int idx = meta & 0x0F;
        if (idx >= all.length) idx = 0;
        return getDefaultState().withProperty(MASTER_OFFSET, all[idx]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(MASTER_OFFSET).ordinal();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    // Suppress break / dig particles — the filler is invisible, so we let
    // the master block's own particles handle visual feedback.
    @Override
    @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
    public boolean addDestroyEffects(World world, BlockPos pos,
                                      net.minecraft.client.particle.ParticleManager manager) {
        return true; // return true = "handled", vanilla spawns nothing
    }

    @Override
    @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
    public boolean addHitEffects(IBlockState state, World world,
                                  net.minecraft.util.math.RayTraceResult target,
                                  net.minecraft.client.particle.ParticleManager manager) {
        // Forward mining-progress particles to the master so the wood texture
        // and position are correct instead of spawning on the invisible filler.
        BlockPos master = findMaster(world, target.getBlockPos());
        if (master != null) {
            manager.addBlockHitEffects(master, target.sideHit);
        }
        return true; // always handled — suppress any fallback filler particles
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        // No collision — players walk through the visual overhang as before.
        return NULL_AABB;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        // Selection AABB matches the slice of the master's bounding box that
        // actually lies within this filler cell.  Macaw masters always extend
        // exactly 0.5 outward along the axis perpendicular to FACING and a
        // full block upward (cupboard / wardrobe top), so the slice is simply
        // a half-cube on the side facing the master, full height.
        MasterOffset mo = state.getValue(MASTER_OFFSET);
        // filler-local sign of master direction (== -mo) tells us which side
        // of this cell the master AABB occupies.
        int dx = -mo.dx;
        int dz = -mo.dz;

        double minX = dx < 0 ? 0.5 : 0.0;
        double maxX = dx > 0 ? 0.5 : 1.0;
        double minZ = dz < 0 ? 0.5 : 0.0;
        double maxZ = dz > 0 ? 0.5 : 1.0;

        return new AxisAlignedBB(minX, 0.0, minZ, maxX, 1.0, maxZ);
    }

    /**
     * Hover-outline highlight: delegate to the master block so that the same
     * full-model AABB is drawn regardless of which cell (master or filler) the
     * cursor is aimed at.
     */
    @Override
    @SuppressWarnings("deprecation")
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
        BlockPos master = findMaster(world, pos);
        if (master != null) {
            IBlockState masterState = world.getBlockState(master);
            return masterState.getSelectedBoundingBox(world, master);
        }
        return super.getSelectedBoundingBox(state, world, pos);
    }

    // ────────────────────────────────────────────────────────────────────
    // Item drop behaviour: filler is invisible / unobtainable.  The master
    // owns the dresser ItemBlock; filler.removedByPlayer routes drops there.
    // ────────────────────────────────────────────────────────────────────

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos,
                         IBlockState state, int fortune) {
        // intentionally empty — master drops on its own breakBlock
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, net.minecraft.util.math.RayTraceResult target,
                                   World world, BlockPos pos, EntityPlayer player) {
        BlockPos master = findMaster(world, pos);
        if (master != null) {
            IBlockState m = world.getBlockState(master);
            return m.getBlock().getPickBlock(m, target, world, master, player);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos,
                                     net.minecraft.entity.Entity entity) {
        // Match the master's resistance — preventing endermen / mobs from
        // griefing only the filler half of a dresser.
        return false;
    }

    // ────────────────────────────────────────────────────────────────────
    // Interaction forwarding.
    // ────────────────────────────────────────────────────────────────────

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
                                     EntityPlayer player, EnumHand hand,
                                     EnumFacing facing, float hitX, float hitY, float hitZ) {
        BlockPos master = findMaster(world, pos);
        if (master == null) return false;
        IBlockState m = world.getBlockState(master);
        return m.getBlock().onBlockActivated(world, master, m, player, hand, facing,
            hitX, hitY, hitZ);
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos,
                                    EntityPlayer player, boolean willHarvest) {
        if (!world.isRemote && !BREAKING.get()) {
            BlockPos master = findMaster(world, pos);
            if (master != null) {
                IBlockState m = world.getBlockState(master);
                ResourceLocation name = m.getBlock().getRegistryName();
                if (FurnitureRegistry.isHandled(name)) {
                    BREAKING.set(Boolean.TRUE);
                    try {
                        // destroyBlock drops the master's ItemBlock and triggers
                        // master.breakBlock (which clears the rest of the fillers).
                        world.destroyBlock(master, !player.capabilities.isCreativeMode);
                    } finally {
                        BREAKING.set(Boolean.FALSE);
                    }
                }
            }
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        // Triggered when the chunk replaces this filler with another block
        // (explosion, world edit, master cleanup, etc.).  If the master is
        // still standing, take it down with us so the multiblock stays coherent
        if (!world.isRemote && !BREAKING.get()) {
            BlockPos master = findMaster(world, pos);
            if (master != null) {
                IBlockState m = world.getBlockState(master);
                if (FurnitureRegistry.isHandled(m.getBlock().getRegistryName())) {
                    BREAKING.set(Boolean.TRUE);
                    try {
                        world.setBlockToAir(master);
                    } finally {
                        BREAKING.set(Boolean.FALSE);
                    }
                }
            }
        }
        super.breakBlock(world, pos, state);
    }

    // ────────────────────────────────────────────────────────────────────
    // Master lookup: O(1) — read the encoded offset and verify the cell it
    // points at actually holds a Macaw furniture block.
    // ────────────────────────────────────────────────────────────────────

    @Nullable
    public static BlockPos findMaster(IBlockAccess world, BlockPos fillerPos) {
        IBlockState state = world.getBlockState(fillerPos);
        if (!(state.getBlock() instanceof FurnitureFillerBlock)) return null;
        MasterOffset mo = state.getValue(MASTER_OFFSET);
        BlockPos master = fillerPos.add(mo.dx, mo.dy, mo.dz);
        IBlockState ms = world.getBlockState(master);
        if (FurnitureRegistry.isHandled(ms.getBlock().getRegistryName())) {
            return master;
        }
        return null;
    }
}
