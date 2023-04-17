package io.github.thatrobin.soul_squad.powers;

import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.thatrobin.soul_squad.SoulSquad;
import net.minecraft.entity.LivingEntity;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

public class BlockScalePower extends Power {

    ScaleData playerHeight;
    ScaleData playerModelHeight;

    public BlockScalePower(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
    }

    @Override
    public void onAdded() {
        playerModelHeight = ScaleTypes.MODEL_HEIGHT.getScaleData(entity);
        playerHeight = ScaleTypes.HEIGHT.getScaleData(entity);
        if(playerHeight.getScale() != 0.45f) {
            playerHeight.setTargetScale(0.45f);
        }
        if(playerModelHeight.getScale() != 2.225f) {
            playerModelHeight.setTargetScale(2.225f);
        }
    }

    @Override
    public void onRemoved() {
        playerModelHeight = ScaleTypes.MODEL_HEIGHT.getScaleData(entity);
        playerHeight = ScaleTypes.HEIGHT.getScaleData(entity);
        if(playerHeight.getScale() != 1) {
            playerHeight.setTargetScale(1);
        }
        if(playerModelHeight.getScale() != 1f) {
            playerModelHeight.setTargetScale(1f);
        }
    }

    public static PowerFactory<?> createFactory() {
        return new PowerFactory<>(SoulSquad.poltergeist("block_scale"),
                new SerializableData(),
                data ->
                        (type, entity) -> new BlockScalePower(type, entity)).allowCondition();
    }
}
