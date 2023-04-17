package io.github.thatrobin.soul_squad.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.thatrobin.soul_squad.component.BodyOwnerComponent;
import io.github.thatrobin.soul_squad.entity.HivemindBodyEntity;
import io.github.thatrobin.soul_squad.powers.BodyManagementPower;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void onDeath(DamageSource damageSource, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        BodyOwnerComponent component = BodyOwnerComponent.KEY.get(player);
        Entity entity = player.world.getEntityLookup().get(component.getOwner());
        if (entity instanceof LivingEntity livingEntity) {
            PowerHolderComponent powerHolderComponent = PowerHolderComponent.KEY.get(livingEntity);
            for (BodyManagementPower power : powerHolderComponent.getPowers(BodyManagementPower.class)) {
                power.removeBody((HivemindBodyEntity) player);
            }
            powerHolderComponent.sync();
        }
    }

}
