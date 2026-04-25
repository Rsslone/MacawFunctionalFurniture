package com.tommytek.mcwfuncfurn.storage;

import com.tommytek.mcwfuncfurn.Tags;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Persists furniture inventories in world save data.
 *
 * <p>A single {@link ItemStackHandler} is stored per {@link BlockPos}, sized
 * to {@code rows * 9} slots. Obtain the instance via {@link #get(World)};
 * always call server-side only.
 */
public class FurnitureInventoryData extends WorldSavedData {

    private static final String DATA_NAME = Tags.MOD_ID + "_inventories";

    private final Map<Long, ItemStackHandler> inventories = new HashMap<>();

    public FurnitureInventoryData(String name) {
        super(name);
    }

    // ── Static accessor ───────────────────────────────────────────────────

    public static FurnitureInventoryData get(World world) {
        MapStorage storage = world.getPerWorldStorage();
        FurnitureInventoryData data = (FurnitureInventoryData)
            storage.getOrLoadData(FurnitureInventoryData.class, DATA_NAME);
        if (data == null) {
            data = new FurnitureInventoryData(DATA_NAME);
            storage.setData(DATA_NAME, data);
        }
        return data;
    }

    // ── Inventory access ──────────────────────────────────────────────────

    /**
     * Returns the handler for {@code pos}, creating it with {@code slotCount}
     * slots if it does not yet exist.
     */
    public ItemStackHandler getOrCreate(BlockPos pos, int slotCount) {
        long key = pos.toLong();
        ItemStackHandler existing = inventories.get(key);
        if (existing != null) return existing;

        ItemStackHandler handler = new ItemStackHandler(slotCount);
        inventories.put(key, handler);
        markDirty();
        return handler;
    }

    /**
     * Returns the handler if it already exists, or {@code null} otherwise.
     * Used when dropping items on block break to avoid forcing creation.
     */
    @Nullable
    public ItemStackHandler getIfPresent(BlockPos pos) {
        return inventories.get(pos.toLong());
    }

    /**
     * Removes inventory data for {@code pos}. Call when the block is broken.
     */
    public void remove(BlockPos pos) {
        if (inventories.remove(pos.toLong()) != null) {
            markDirty();
        }
    }

    // ── NBT ───────────────────────────────────────────────────────────────

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        inventories.clear();
        NBTTagList entries = nbt.getTagList("entries", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < entries.tagCount(); i++) {
            NBTTagCompound entry = entries.getCompoundTagAt(i);
            long key = entry.getLong("pos");
            int slots = entry.getInteger("slots");
            ItemStackHandler handler = new ItemStackHandler(slots);
            handler.deserializeNBT(entry.getCompoundTag("inv"));
            inventories.put(key, handler);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        NBTTagList entries = new NBTTagList();
        for (Map.Entry<Long, ItemStackHandler> e : inventories.entrySet()) {
            NBTTagCompound entry = new NBTTagCompound();
            entry.setLong("pos", e.getKey());
            entry.setInteger("slots", e.getValue().getSlots());
            entry.setTag("inv", e.getValue().serializeNBT());
            entries.appendTag(entry);
        }
        nbt.setTag("entries", entries);
        return nbt;
    }
}
