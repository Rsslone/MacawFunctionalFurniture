package com.tommytek.mcwfuncfurn.mixins;

import com.tommytek.mcwfuncfurn.FurnitureRegistry;
import com.tommytek.mcwfuncfurn.MacawFunctionalFurnitureMod;
import com.tommytek.mcwfuncfurn.storage.FurnitureInventoryData;
import net.minecraft.block.state.IBlockState;
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

// FurnitureBase does not override onBlockActivated/breakBlock, so @Inject won't work.
// Plain method merging overrides Block's inherited implementations in all four subclasses.
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

    public void func_180663_b(World world, BlockPos pos, IBlockState state) {
        if (world.isRemote) return;

        ResourceLocation name = state.getBlock().getRegistryName();
        if (name == null || !FurnitureRegistry.isHandled(name)) return;

        FurnitureInventoryData data = FurnitureInventoryData.get(world);
        ItemStackHandler handler = data.getIfPresent(pos);
        if (handler == null) return;

        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                InventoryHelper.spawnItemStack(world,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
            }
        }
        data.remove(pos);
    }
}
