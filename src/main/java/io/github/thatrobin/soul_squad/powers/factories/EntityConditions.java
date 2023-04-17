package io.github.thatrobin.hivemind.powers.factories;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.thatrobin.hivemind.Hivemind;
import io.github.thatrobin.hivemind.component.BodyOwnerComponent;
import io.github.thatrobin.hivemind.powers.BodyManagementPower;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registry;
import net.minecraft.util.Pair;

public class EntityConditions {

    public static void register() {
        register(new ConditionFactory<>(Hivemind.identifier("body_count"),
                new SerializableData()
                        .add("comparison", ApoliDataTypes.COMPARISON)
                        .add("compare_to", SerializableDataTypes.INT),
                (data, entity) -> {
                    if(entity == null) return false;
                    PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
                    int bodyAmount = 1;
                    for (BodyManagementPower power : component.getPowers(BodyManagementPower.class)) {
                        if(power.entityOne != null) bodyAmount++;
                        if(power.entityTwo != null) bodyAmount++;
                    }
                    return ((Comparison)data.get("comparison")).compare(bodyAmount, data.getInt("compare_to"));
                }));
        register(new ConditionFactory<>(Hivemind.identifier("in_body"),
                new SerializableData()
                        .add("body_number", SerializableDataTypes.INT),
                (data, entity) -> {
                    PowerHolderComponent component = PowerHolderComponent.KEY.get(entity);
                    boolean inBody = false;
                    for (BodyManagementPower power : component.getPowers(BodyManagementPower.class)) {
                        if(power.currentBodyIndex == data.getInt("body_number")) {
                            inBody = true;
                        }
                    }
                    return inBody;
                }));
        register(new ConditionFactory<>(Hivemind.identifier("body_owner_condition"),
                new SerializableData()
                        .add("bientity_condition", ApoliDataTypes.BIENTITY_CONDITION),
                (data, entity) -> {
                    BodyOwnerComponent component = BodyOwnerComponent.KEY.get(entity);
                    ConditionFactory<Pair<Entity,Entity>>.Instance bientityCondition = data.get("bientity_condition");
                    return bientityCondition.test(new Pair<>(entity, component.getEntityOwner()));
                }));
    }

    private static void register(ConditionFactory<Entity> actionFactory) {
        Registry.register(ApoliRegistries.ENTITY_CONDITION, actionFactory.getSerializerId(), actionFactory);
    }
}
