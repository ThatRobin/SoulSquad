package io.github.thatrobin.hivemind.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(PlayerEntity.class)
public interface PlayerEntityAccessorMixin {

    @Accessor("inventory")
    void setInventory(PlayerInventory inventory);

}
