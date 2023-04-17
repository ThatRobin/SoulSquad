package io.github.thatrobin.hivemind.powers.factories;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.thatrobin.hivemind.Hivemind;
import io.github.thatrobin.hivemind.powers.BodyManagementPower;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registry;

import java.util.Optional;

public class EntityActions {

    public static void register() {
        register(new ActionFactory<>(Hivemind.identifier("create_body"), new SerializableData(),
                (data, entity) -> {
                    PowerHolderComponent.getPowers(entity, BodyManagementPower.class).forEach(BodyManagementPower::createBody);
                }));
    }

    private static void register(ActionFactory<Entity> actionFactory) {
        Registry.register(ApoliRegistries.ENTITY_ACTION, actionFactory.getSerializerId(), actionFactory);
    }
}
