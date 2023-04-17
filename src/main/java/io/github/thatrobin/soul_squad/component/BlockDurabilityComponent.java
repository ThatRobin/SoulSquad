package io.github.thatrobin.soul_squad.component;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import io.github.thatrobin.soul_squad.SoulSquad;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;

public interface BlockDurabilityComponent extends AutoSyncedComponent {

    ComponentKey<BlockDurabilityComponent> KEY = ComponentRegistry.getOrCreate(SoulSquad.poltergeist("durabilities"), BlockDurabilityComponent.class);

    void removeBlock(Block block);

    void addBlock(Block block);

    boolean hasBlock(Block block);

    void degradeBlock(Block block, int amount);

    int getDurability(Block block);

    void sync();

    static void sync(Entity entity) {
        KEY.sync(entity);
    }

}
