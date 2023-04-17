package io.github.thatrobin.hivemind.component;

import io.github.thatrobin.hivemind.Hivemind;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class BodyHolderComponentImpl implements BodyHolderComponent {

    private final LivingEntity owner;
    private final Map<Integer, UUID> entityMap = new HashMap<>();
    private final Map<Integer, ItemStack> iconMap = new HashMap<>();

    public BodyHolderComponentImpl(LivingEntity owner) {
        this.owner = owner;
        iconMap.put(0, new ItemStack(Items.RED_BED));
        iconMap.put(1, new ItemStack(Items.IRON_PICKAXE));
        iconMap.put(2, new ItemStack(Items.WHEAT));
    }

    @Override
    public void setBody(int index, UUID uuid) {
        entityMap.put(index, uuid);
    }

    @Override
    public PlayerEntity getBody(int index) {
        return (PlayerEntity) this.owner.world.getEntityLookup().get(entityMap.get(index));
    }

    @Override
    public boolean hasBody(int index) {
        return this.owner.world.getEntityLookup().get(entityMap.get(index)) != null;
    }

    @Override
    public int size() {
        return entityMap.size();
    }

    @Override
    public boolean hasIcon(int index) {
        return iconMap.containsKey(index);
    }

    @Override
    public ItemStack getIcon(int index) {
        if(iconMap.containsKey(index) && !iconMap.get(index).isEmpty()) {
            return iconMap.get(index);
        }
        return new ItemStack(Items.IRON_PICKAXE);
    }

    @Override
    public void setIcon(int index, ItemStack itemStack) {
        iconMap.put(index, itemStack);
    }

    @Override
    public void sync() {
        BodyOwnerComponent.sync(this.owner);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound compoundTag) {
        if (owner == null) {
            Hivemind.LOGGER.error("Owner was null in BodyHolderComponentImpl#readFromNbt!");
        }
        if(compoundTag.contains("Bodies")) {
            NbtList list = compoundTag.getList("Bodies", NbtElement.COMPOUND_TYPE);
            for (NbtElement nbtElement : list) {
                NbtCompound compound = (NbtCompound) nbtElement;
                int index = compound.getInt("Index");
                if (this.owner != null) {
                    this.entityMap.put(index, compound.getUuid("UUID"));
                }

            }
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound compoundTag) {
        NbtList list = new NbtList();
        for (Map.Entry<Integer, UUID> integerPlayerEntityEntry : this.entityMap.entrySet()) {
            int index = integerPlayerEntityEntry.getKey();
            UUID uuid = integerPlayerEntityEntry.getValue();
            if(uuid != null) {
                NbtCompound compound = new NbtCompound();
                compound.putInt("Index", index);
                compound.putUuid("UUID", uuid);
                list.add(compound);
            }
        }
        compoundTag.put("Bodies", list);
    }
}
