package com.tommytek.mcwfuncfurn;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

/**
 * Server-side proxy. {@link #getClientGuiElement} is intentionally a no-op to
 * prevent client-only classes from loading on a dedicated server.
 */
public class CommonProxy {

    public Object getClientGuiElement(int id, EntityPlayer player,
                                       World world, int x, int y, int z) {
        return null;
    }
}
