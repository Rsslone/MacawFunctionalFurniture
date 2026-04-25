package com.tommytek.mcwfuncfurn;

import com.tommytek.mcwfuncfurn.container.FurnitureContainer;
import com.tommytek.mcwfuncfurn.gui.FurnitureGui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Client-side proxy that constructs the GUI screen.
 *
 * <p>The slot handler is intentionally empty here — Minecraft's container sync
 * sends {@code SPacketWindowItems} from the server after the window opens, so
 * the client never needs to read {@link
 * com.tommytek.mcwfuncfurn.storage.FurnitureInventoryData} directly.
 * Attempting to do so on the client would NPE because
 * {@code WorldClient.getPerWorldStorage()} returns {@code null}.
 */
public class ClientProxy extends CommonProxy {

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player,
                                       World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        ResourceLocation regName = world.getBlockState(pos).getBlock().getRegistryName();
        if (regName == null) return null;

        int rows = FurnitureRegistry.getRows(regName.getPath());
        if (rows <= 0) return null;

        ItemStackHandler handler = new ItemStackHandler(rows * 9);
        return new FurnitureGui(new FurnitureContainer(player.inventory, handler, pos, rows));
    }
}
