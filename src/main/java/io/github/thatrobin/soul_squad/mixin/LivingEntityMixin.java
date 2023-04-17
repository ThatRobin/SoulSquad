package io.github.thatrobin.poltergeist.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.thatrobin.poltergeist.component.BlockDurabilityComponent;
import io.github.thatrobin.poltergeist.powers.BlockPossession;
import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.BlockTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float modifyDamageTaken(float originalValue, DamageSource source, float amount) {
        if(((LivingEntity) (Object) this) instanceof PlayerEntity player && !player.isCreative()) {
            if(PowerHolderComponent.hasPower((LivingEntity) (Object) this, BlockPossession.class)) {
                BlockPossession blockPossession = PowerHolderComponent.getPowers((LivingEntity) (Object) this, BlockPossession.class).get(0);
                if (blockPossession.getPossessedBlock() != null) {
                    BlockDurabilityComponent component = BlockDurabilityComponent.KEY.get((LivingEntity) (Object) this);
                    Block block = blockPossession.getPossessedBlock().getBlock();
                    if (component.hasBlock(block)) {
                        if (blockPossession.getPossessedBlock().isIn(BlockTags.NEEDS_DIAMOND_TOOL)) {
                            component.degradeBlock(block, 4);
                        } else if (blockPossession.getPossessedBlock().isIn(BlockTags.NEEDS_IRON_TOOL)) {
                            component.degradeBlock(block, 8);
                        } else if (blockPossession.getPossessedBlock().isIn(BlockTags.NEEDS_STONE_TOOL)) {
                            component.degradeBlock(block, 12);
                        } else {
                            component.degradeBlock(block, 16);
                        }
                        if (!component.hasBlock(block)) {
                            blockPossession.resetPossessedBlock();
                        }

                    }
                    component.sync();
                    return 0;
                }
            }
        }
        return originalValue;
    }
}
