package com.tommytek.mcwfuncfurn.mixins;

import com.tommytek.mcwfuncfurn.FurnitureRegistry;
import com.tommytek.mcwfuncfurn.MacawFunctionalFurnitureMod;
import com.tommytek.mcwfuncfurn.block.FurnitureBlocks;
import com.tommytek.mcwfuncfurn.block.FurnitureFillerBlock;
import com.tommytek.mcwfuncfurn.storage.FurnitureInventoryData;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;

// FurnitureBase does not override onBlockActivated/breakBlock/onBlockPlacedBy,
// so @Inject won't work.  Plain method merging overrides Block's inherited
// implementations in all four Macaw subclasses (Block, Dresser, Cupboard,
// Wardrobe).
@Mixin(targets = "com.mcwfurnitures.kikoz.objects.blocks.FurnitureBase", remap = false)
public abstract class FurnitureBaseMixin {

    public boolean func_180639_a(World world, BlockPos pos, IBlockState state,
                                  EntityPlayer player, EnumHand hand,
                                  EnumFacing facing,
                                  float hitX, float hitY, float hitZ) {
        if (hand != EnumHand.MAIN_HAND) {
            return true;
        }
        ResourceLocation name = state.getBlock().getRegistryName();
        if (name == null || !FurnitureRegistry.isHandled(name)) {
            return false;
        }
        if (!world.isRemote) {
            player.openGui(MacawFunctionalFurnitureMod.INSTANCE,
                0, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    /**
     * onBlockPlacedBy — runs immediately after the master block is placed.
     * Spawns invisible {@link FurnitureFillerBlock fillers} in the cells the
     * model visually occupies but the master cannot reach (e.g. side cells of
     * a dresser, the upper cell of a cupboard).
     *
     * <p>Each filler stores the offset back to its master, so the master can
     * be located in O(1) and ownership is unambiguous.  When a neighbouring
     * multiblock already owns the cell we leave it alone — the second piece
     * simply has one less interaction cell, which is preferable to silently
     * stealing the first piece's filler.
     */
    public void func_180633_a(World world, BlockPos pos, IBlockState state,
                               EntityLivingBase placer, ItemStack stack) {
        if (world.isRemote) return;

        ResourceLocation name = state.getBlock().getRegistryName();
        FurnitureRegistry.Shape shape = FurnitureRegistry.getShape(name);
        if (shape == FurnitureRegistry.Shape.NONE) return;

        EnumFacing facing = readFacing(state);
        if (facing == null) return;

        for (BlockPos off : shape.fillerOffsets(facing)) {
            BlockPos target = pos.add(off);
            IBlockState existing = world.getBlockState(target);
            // Only place into truly replaceable cells.  Existing fillers
            // belong to another multiblock — leave them alone.
            if (!existing.getBlock().isReplaceable(world, target)) continue;

            // Offset filler→master is the inverse of master→filler.
            FurnitureFillerBlock.MasterOffset mo = FurnitureFillerBlock.MasterOffset.of(
                -off.getX(), -off.getY(), -off.getZ());
            if (mo == null) continue; // shape declared an unknown offset; skip

            world.setBlockState(target,
                FurnitureBlocks.FILLER.getDefaultState()
                    .withProperty(FurnitureFillerBlock.MASTER_OFFSET, mo),
                3);
        }
    }

    public void func_180663_b(World world, BlockPos pos, IBlockState state) {
        if (world.isRemote) return;

        ResourceLocation name = state.getBlock().getRegistryName();
        if (name == null || !FurnitureRegistry.isHandled(name)) return;

        // Drop stored inventory contents.
        FurnitureInventoryData data = FurnitureInventoryData.get(world);
        ItemStackHandler handler = data.getIfPresent(pos);
        if (handler != null) {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    InventoryHelper.spawnItemStack(world,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                }
            }
            data.remove(pos);
        }

        // Tear down the multiblock fillers — guarded so the cascading
        // filler.breakBlock → master cleanup loop terminates.
        FurnitureRegistry.Shape shape = FurnitureRegistry.getShape(name);
        if (shape == FurnitureRegistry.Shape.NONE) return;

        EnumFacing facing = readFacing(state);
        if (facing == null) return;

        boolean owned = !FurnitureFillerBlock.BREAKING.get();
        if (owned) FurnitureFillerBlock.BREAKING.set(Boolean.TRUE);
        try {
            for (BlockPos off : shape.fillerOffsets(facing)) {
                BlockPos fillerPos = pos.add(off);
                IBlockState fs = world.getBlockState(fillerPos);
                if (fs.getBlock() != FurnitureBlocks.FILLER) continue;
                // Verify ownership: only clear fillers whose stored offset
                // points back at THIS master.  Prevents tearing down a
                // neighbouring multiblock's fillers.
                FurnitureFillerBlock.MasterOffset mo =
                    fs.getValue(FurnitureFillerBlock.MASTER_OFFSET);
                if (fillerPos.add(mo.dx, mo.dy, mo.dz).equals(pos)) {
                    world.setBlockToAir(fillerPos);
                }
            }
        } finally {
            if (owned) FurnitureFillerBlock.BREAKING.set(Boolean.FALSE);
        }
    }

    /**
     * Read the master's {@code FACING} property without depending on a
     * specific subclass.  Macaw stores it as a {@code PropertyDirection} on
     * each block.
     */
    private static EnumFacing readFacing(IBlockState state) {
        for (IProperty<?> p : state.getPropertyKeys()) {
            if ("facing".equals(p.getName()) && p.getValueClass() == EnumFacing.class) {
                @SuppressWarnings("unchecked")
                Comparable<?> v = state.getValue((IProperty<EnumFacing>) p);
                if (v instanceof EnumFacing) return (EnumFacing) v;
            }
        }
        return null;
    }
}
