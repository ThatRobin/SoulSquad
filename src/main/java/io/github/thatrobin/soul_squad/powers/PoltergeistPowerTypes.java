package io.github.thatrobin.poltergeist.powers;

import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.minecraft.util.registry.Registry;

public class PoltergeistPowerTypes {

    public static void register() {
        register(BlockPossession.createFactory());
        register(BlockScalePower.createFactory());
    }

    private static void register(PowerFactory<?> powerFactory) {
        Registry.register(ApoliRegistries.POWER_FACTORY, powerFactory.getSerializerId(), powerFactory);
    }

}
