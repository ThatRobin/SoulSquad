package io.github.thatrobin.soul_squad.powers.factories;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.thatrobin.soul_squad.SoulSquad;
import io.github.thatrobin.soul_squad.powers.BlockPossession;
import io.github.thatrobin.soul_squad.powers.BodyManagementPower;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registry;

public class EntityActions {

    public static void register() {
        register(new ActionFactory<>(SoulSquad.poltergeist("set_possessed_block_from_world"), new SerializableData(),
                (data, entity) -> {
                    PowerHolderComponent.getPowers(entity, BlockPossession.class).forEach((power) -> {
                        power.setPossessedBlock(entity.world.getBlockState(entity.getBlockPos()), true, false);
                    });
                }));
        register(new ActionFactory<>(SoulSquad.poltergeist("set_possessed_block_from_hand"), new SerializableData(),
                (data, entity) -> {
                    PowerHolderComponent.getPowers(entity, BlockPossession.class).forEach((power) -> {
                        if(entity instanceof PlayerEntity player) {
                            if(player.getMainHandStack().getItem() instanceof BlockItem blockItem) {
                                BlockState blockState = blockItem.getBlock().getDefaultState();
                                if(!blockState.isIn(SoulSquad.UNPOSSESSABLE_BLOCKS)) {
                                    power.setPossessedBlock(blockState, false, true);
                                }
                            }
                        }
                    });
                }));
        register(new ActionFactory<>(SoulSquad.poltergeist("reset_possessed_block"), new SerializableData()
                .add("place_block", SerializableDataTypes.BOOLEAN, true),
                (data, entity) -> {
                    PowerHolderComponent.getPowers(entity, BlockPossession.class).forEach((power) -> {
                        if(power.getPossessedBlock() != null && data.getBoolean("place_block")) {
                            entity.world.setBlockState(entity.getBlockPos(), power.getPossessedBlock());
                        }
                        power.resetPossessedBlock();
                    });
                }));
        register(new ActionFactory<>(SoulSquad.hivemind("create_body"), new SerializableData(),
                (data, entity) -> {
                    PowerHolderComponent.getPowers(entity, BodyManagementPower.class).forEach(BodyManagementPower::createBody);
                }));
    }

    private static void register(ActionFactory<Entity> actionFactory) {
        Registry.register(ApoliRegistries.ENTITY_ACTION, actionFactory.getSerializerId(), actionFactory);
    }
}
