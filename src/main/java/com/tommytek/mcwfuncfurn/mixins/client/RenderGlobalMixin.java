package com.tommytek.mcwfuncfurn.mixins.client;

import com.tommytek.mcwfuncfurn.block.FurnitureFillerBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Redirects the block-break overlay from a {@link FurnitureFillerBlock} to the master
 * block, so the damage texture appears on the visible furniture model.
 */
//@Mixin(RenderGlobal.class)
// public abstract class RenderGlobalMixin {

//     @ModifyVariable(
//         method = "sendBlockBreakProgress(ILnet/minecraft/util/math/BlockPos;I)V",
//         at = @At("HEAD"),
//         argsOnly = true
//     )
//     private BlockPos mcwfuncfurn$redirectFillerToMaster(BlockPos pos) {
//         World world = Minecraft.getMinecraft().world;
//         IBlockState state = world.getBlockState(pos);
//         if (state.getBlock() instanceof FurnitureFillerBlock) {
//             BlockPos master = FurnitureFillerBlock.findMaster(world, pos);
//             if (master != null) return master;
//         }
//         return pos;
//     }
// }
