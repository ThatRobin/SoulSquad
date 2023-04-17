package io.github.thatrobin.soul_squad.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.CachedBlockPosition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CachedBlockPosition.class)
public interface CBPAccessorMixin {

    @Accessor("state")
    void setBlockState(BlockState state);

}
