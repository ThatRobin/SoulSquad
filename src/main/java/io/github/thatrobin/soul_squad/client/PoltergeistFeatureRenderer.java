package io.github.thatrobin.soul_squad.client;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.thatrobin.soul_squad.SoulSquad;
import io.github.thatrobin.soul_squad.powers.BlockPossession;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public class PoltergeistFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {
    private static final Identifier SKIN = SoulSquad.poltergeist("textures/entity/poltergeist/poltergeist.png");
    private final PoltergeistModel<T> poltergeistModel;

    public PoltergeistFeatureRenderer(FeatureRendererContext<T, M> context, EntityModelLoader loader) {
        super(context);
        this.poltergeistModel = new PoltergeistModel<>(loader.getModelPart(EntityModelLayers.SLIME));
    }

    @Override
    public void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, T livingEntity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if(PowerHolderComponent.hasPower(livingEntity, BlockPossession.class)) {
            BlockPossession power = PowerHolderComponent.getPowers(livingEntity, BlockPossession.class).get(0);
            BlockState blockState = power.getPossessedBlock();

            if (blockState == null) {
                matrixStack.push();
                matrixStack.translate(0.0D, -0.025D, 0.0D);
                VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityTranslucent(SKIN));
                this.getContextModel().copyStateTo(this.poltergeistModel);
                this.poltergeistModel.setAngles(livingEntity, limbAngle, limbDistance, animationProgress, headYaw, headPitch);
                this.poltergeistModel.render(matrixStack, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 0.5F);
                matrixStack.pop();
            }
        }
    }
}
