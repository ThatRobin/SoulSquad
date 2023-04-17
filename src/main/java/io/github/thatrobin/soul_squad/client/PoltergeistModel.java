package io.github.thatrobin.soul_squad.client;

import net.minecraft.client.model.*;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.entity.LivingEntity;

public class PoltergeistModel<T extends LivingEntity> extends SinglePartEntityModel<T> {

    private final ModelPart root;

    public PoltergeistModel(ModelPart root) {
        this.root = root;
    }

    @Override
    public ModelPart getPart() {
        return this.root;
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {

    }
}
