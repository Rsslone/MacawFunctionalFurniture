package com.tommytek.mcwfuncfurn.gui;

import com.tommytek.mcwfuncfurn.container.FurnitureContainer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

/**
 * Client-side GUI for a furniture storage block.
 *
 * <p>Reuses the vanilla {@code generic_54.png} chest texture, split into:
 * <ol>
 *   <li>Top section — header + furniture slot rows.</li>
 *   <li>Bottom section — player inventory + hotbar (always 96 px tall).</li>
 * </ol>
 */
public class FurnitureGui extends GuiContainer {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation("textures/gui/container/generic_54.png");

    private final int rows;

    public FurnitureGui(FurnitureContainer container) {
        super(container);
        this.rows = container.getRows();
        this.ySize = 114 + rows * 18;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);
        int left = (width  - xSize) / 2;
        int top  = (height - ySize) / 2;

        // Top section: header row + furniture slot rows
        drawTexturedModalRect(left, top,
            0, 0,
            xSize, rows * 18 + 17);

        // Bottom section: player inventory + hotbar
        drawTexturedModalRect(left, top + rows * 18 + 17,
            0, 126,
            xSize, 96);
    }
}
