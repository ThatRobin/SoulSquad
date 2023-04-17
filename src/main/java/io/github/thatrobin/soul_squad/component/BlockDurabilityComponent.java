package io.github.thatrobin.poltergeist.component;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import io.github.thatrobin.poltergeist.Poltergeist;

public interface BlockDurabilityComponent extends AutoSyncedComponent {

    ComponentKey<BlockDurabilityComponent> KEY = ComponentRegistry.getOrCreate(Poltergeist.identifier("durabilities"), BlockDurabilityComponent.class);

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
