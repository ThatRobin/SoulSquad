package io.github.thatrobin.soul_squad.component;

import io.github.thatrobin.soul_squad.SoulSquad;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BodyOwnerComponentImpl implements BodyOwnerComponent {

    private final LivingEntity owner;
    private UUID ownerUUID;

    public BodyOwnerComponentImpl(LivingEntity owner) {
        this.owner = owner;
    }


    @Override
    public void setOwner(UUID uuid) {
        this.ownerUUID = uuid;
    }

    @Override
    public UUID getOwner() {
        return this.ownerUUID;
    }

    @Override
    public PlayerEntity getEntityOwner() {
        return (PlayerEntity) owner.world.getEntityLookup().get(this.ownerUUID);
    }

    @Override
    public void sync() {
        BodyOwnerComponent.sync(this.owner);
    }

    @Override
    public void readFromNbt(@NotNull NbtCompound compoundTag) {
        if (owner == null) {
            SoulSquad.LOGGER.error("Owner was null in BodyOwnerComponentImpl#readFromNbt!");
        }
        if(compoundTag.contains("Owner")) {
            this.ownerUUID = compoundTag.getUuid("Owner");
        }
    }

    @Override
    public void writeToNbt(@NotNull NbtCompound compoundTag) {
        if (this.ownerUUID != null) {
            compoundTag.putUuid("Owner", this.ownerUUID);
        }
    }
}
