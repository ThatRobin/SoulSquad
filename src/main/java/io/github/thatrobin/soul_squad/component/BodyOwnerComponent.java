package io.github.thatrobin.hivemind.component;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import io.github.thatrobin.hivemind.Hivemind;
import net.minecraft.entity.player.PlayerEntity;

import java.util.UUID;

public interface BodyOwnerComponent extends AutoSyncedComponent {

    ComponentKey<BodyOwnerComponent> KEY = ComponentRegistry.getOrCreate(Hivemind.identifier("hive_bodies"), BodyOwnerComponent.class);

    void setOwner(UUID uuid);

    UUID getOwner();

    PlayerEntity getEntityOwner();

    void sync();

    static void sync(Entity entity) {
        KEY.sync(entity);
    }

}
