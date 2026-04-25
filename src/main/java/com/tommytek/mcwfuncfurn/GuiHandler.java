package com.tommytek.mcwfuncfurn;

import com.tommytek.mcwfuncfurn.container.FurnitureContainer;
import com.tommytek.mcwfuncfurn.storage.FurnitureInventoryData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Bridges Forge's GUI system to {@link FurnitureContainer}.
 *
 * <p>The {@code id} parameter is always 0 — one inventory per block position.
 */
public class GuiHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player,
                                       World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        ResourceLocation regName = world.getBlockState(pos).getBlock().getRegistryName();
        if (regName == null) return null;

        int rows = FurnitureRegistry.getRows(regName.getPath());
        if (rows <= 0) return null;

        FurnitureInventoryData data = FurnitureInventoryData.get(world);
        ItemStackHandler handler = data.getOrCreate(pos, rows * 9);
        return new FurnitureContainer(player.inventory, handler, pos, rows);
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player,
                                       World world, int x, int y, int z) {
        return MacawFunctionalFurnitureMod.PROXY.getClientGuiElement(id, player, world, x, y, z);
    }
}
