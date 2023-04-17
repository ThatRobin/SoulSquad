package io.github.thatrobin.soul_squad.powers.factories;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.apoli.util.Comparison;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.thatrobin.soul_squad.SoulSquad;
import io.github.thatrobin.soul_squad.component.BodyOwnerComponent;
import io.github.thatrobin.soul_squad.mixin.CBPAccessorMixin;
import io.github.thatrobin.soul_squad.powers.BlockPossession;
import io.github.thatrobin.soul_squad.powers.BodyManagementPower;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.util.Pair;

import java.util.stream.Collectors;

public class ItemConditions {

    public static void register() {
        register(new ConditionFactory<>(SoulSquad.poltergeist("is_block"),
                new SerializableData(),
                (data, stack) -> stack.getItem() instanceof BlockItem));
    }

    private static void register(ConditionFactory<ItemStack> actionFactory) {
        Registry.register(ApoliRegistries.ITEM_CONDITION, actionFactory.getSerializerId(), actionFactory);
    }
}
