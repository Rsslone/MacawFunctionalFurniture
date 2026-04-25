package com.tommytek.mcwfuncfurn;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION,
        dependencies = "required-after:mixinbooter;required-after:mcwfurnitures")
public class MacawFunctionalFurnitureMod {

    @Mod.Instance(Tags.MOD_ID)
    public static MacawFunctionalFurnitureMod INSTANCE;

    @SidedProxy(
        clientSide = "com.tommytek.mcwfuncfurn.ClientProxy",
        serverSide = "com.tommytek.mcwfuncfurn.CommonProxy"
    )
    public static CommonProxy PROXY;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
    }
}
