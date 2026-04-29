package com.tommytek.mcwfuncfurn.block;

import com.tommytek.mcwfuncfurn.Tags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Holds the singleton {@link FurnitureFillerBlock} instance and registers it
 * via Forge's {@code RegistryEvent.Register&lt;Block&gt;} event.
 *
 * <p>The filler is registered without an {@code ItemBlock} — it is never
 * obtainable, only spawned by the multiblock placement logic.
 */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class FurnitureBlocks {

    public static final FurnitureFillerBlock FILLER = createFiller();

    private static FurnitureFillerBlock createFiller() {
        FurnitureFillerBlock b = new FurnitureFillerBlock();
        b.setRegistryName(new ResourceLocation(Tags.MOD_ID, "furniture_filler"));
        b.setTranslationKey(Tags.MOD_ID + ".furniture_filler");
        return b;
    }

    private FurnitureBlocks() {}

    @SubscribeEvent
    public static void onRegisterBlocks(RegistryEvent.Register<net.minecraft.block.Block> event) {
        event.getRegistry().register(FILLER);
    }
}
