package io.github.thatrobin.soul_squad.component;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import io.github.thatrobin.soul_squad.SoulSquad;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.UUID;

public interface BodyHolderComponent extends AutoSyncedComponent {

    ComponentKey<BodyHolderComponent> KEY = ComponentRegistry.getOrCreate(SoulSquad.hivemind("body_indexes"), BodyHolderComponent.class);

    void setBody(int index, UUID uuid);

    PlayerEntity getBody(int index);

    boolean hasBody(int index);

    int size();

    boolean hasIcon(int index);

    ItemStack getIcon(int index);

    void setIcon(int index, ItemStack itemStack);

    void sync();

    static void sync(Entity entity) {
        KEY.sync(entity);
    }

}
