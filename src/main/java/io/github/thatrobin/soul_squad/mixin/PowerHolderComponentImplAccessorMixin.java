package io.github.thatrobin.soul_squad.mixin;

import io.github.apace100.apoli.component.PowerHolderComponentImpl;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(value = PowerHolderComponentImpl.class, remap = false)
public interface PowerHolderComponentImplAccessorMixin {

    @Accessor("powerSources")
    @Mutable
    ConcurrentHashMap<PowerType<?>, List<Identifier>> getPowerSources();

    @Accessor("powers")
    @Mutable
    ConcurrentHashMap<PowerType<?>, Power> getPowers();

    @Accessor("powerSources")
    @Mutable
    void setPowerSources(ConcurrentHashMap<PowerType<?>, List<Identifier>> powerSources);

    @Accessor("powers")
    @Mutable
    void setPowers(ConcurrentHashMap<PowerType<?>, Power> powers);
}
