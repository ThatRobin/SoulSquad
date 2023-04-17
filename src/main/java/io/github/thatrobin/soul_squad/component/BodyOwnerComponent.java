package io.github.thatrobin.soul_squad.component;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import io.github.thatrobin.soul_squad.SoulSquad;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.UUID;

public interface BodyOwnerComponent extends AutoSyncedComponent {

    ComponentKey<BodyOwnerComponent> KEY = ComponentRegistry.getOrCreate(SoulSquad.hivemind("hive_bodies"), BodyOwnerComponent.class);

    void setOwner(UUID uuid);

    UUID getOwner();

    PlayerEntity getEntityOwner();

    void sync();

    static void sync(Entity entity) {
        KEY.sync(entity);
    }

}
