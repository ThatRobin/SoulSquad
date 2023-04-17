package io.github.thatrobin.poltergeist.component;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.component.PowerHolderComponentImpl;
import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import io.github.thatrobin.poltergeist.Poltergeist;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BlockDurabilityComponentImpl implements BlockDurabilityComponent {

    private final LivingEntity owner;
    public ConcurrentHashMap<Block, Integer> durabilities = new ConcurrentHashMap<>();

    public BlockDurabilityComponentImpl(LivingEntity owner) {
        this.owner = owner;
    }


    @Override
    public void removeBlock(Block block) {
        durabilities.remove(block);
    }

    @Override
    public void addBlock(Block block) {
        durabilities.put(block, 1000);
    }

    @Override
    public boolean hasBlock(Block block) {
        return durabilities.containsKey(block);
    }

    @Override
    public void degradeBlock(Block block, int amount) {
        if(hasBlock(block)) {
            if (getDurability(block) - amount <= 0) {
                removeBlock(block);
            } else {
                durabilities.put(block, durabilities.get(block) - amount);
            }
        }
    }

    @Override
    public int getDurability(Block block) {
        return durabilities.get(block);
    }

    @Override
    public void sync() {
        BlockDurabilityComponent.sync(this.owner);
    }

    @Override
    public void readFromNbt(NbtCompound compoundTag) {
        if (owner == null) {
            Poltergeist.LOGGER.error("Owner was null in BlockDurabilityComponent#readFromNbt!");
        }
        durabilities.clear();

        if(compoundTag != null && !compoundTag.isEmpty()) {
            for (String key : compoundTag.getKeys()) {
                int durability = compoundTag.getInt(key);
                Identifier identifier = Identifier.tryParse(key);
                Block block = Registry.BLOCK.get(identifier);
                durabilities.put(block, durability);
            }
        }
    }

    @Override
    public void writeToNbt(NbtCompound compoundTag) {
        for(Map.Entry<Block, Integer> powerEntry : durabilities.entrySet()) {
            Identifier identifier = Registry.BLOCK.getId(powerEntry.getKey());
            String key = identifier.toString();
            compoundTag.putInt(key, powerEntry.getValue());
        }
    }
}
