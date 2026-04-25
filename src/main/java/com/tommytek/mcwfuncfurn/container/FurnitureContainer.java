package com.tommytek.mcwfuncfurn.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * Container for a furniture storage block.
 *
 * <p>Slot layout:
 * <ul>
 *   <li>0 .. {@code rows*9 - 1} — furniture inventory</li>
 *   <li>{@code rows*9} .. +26 — player main inventory (rows 1–3)</li>
 *   <li>{@code rows*9 + 27} .. +8 — player hotbar (row 0)</li>
 * </ul>
 */
public class FurnitureContainer extends Container {

    private final BlockPos blockPos;
    private final int rows;
    private final int furnitureSlots;

    public FurnitureContainer(InventoryPlayer playerInv, ItemStackHandler inv,
                               BlockPos blockPos, int rows) {
        this.blockPos      = blockPos;
        this.rows          = rows;
        this.furnitureSlots = rows * 9;

        // ── Furniture slots ───────────────────────────────────────────────
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new SlotItemHandler(inv,
                    col + row * 9,
                    8 + col * 18, 18 + row * 18));
            }
        }

        // ── Player main inventory (slots 9–35) ────────────────────────────
        // Vanilla ContainerChest uses i = (numRows - 4) * 18 so the player
        // inventory tracks the chest height correctly (e.g. 3-row: i = -18).
        int i = (rows - 4) * 18;
        int playerInvTop = 103 + i;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInv,
                    col + row * 9 + 9,
                    8 + col * 18, playerInvTop + row * 18));
            }
        }

        // ── Player hotbar (slots 0–8) ─────────────────────────────────────
        int hotbarTop = 161 + i;
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, hotbarTop));
        }
    }

    public int getRows() {
        return rows;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return player.getDistanceSq(
            blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            result = slotStack.copy();
            if (index < furnitureSlots) {
                // Furniture → player inventory
                if (!mergeItemStack(slotStack, furnitureSlots, inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Player inventory → furniture
                if (!mergeItemStack(slotStack, 0, furnitureSlots, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (slotStack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }
        return result;
    }
}
