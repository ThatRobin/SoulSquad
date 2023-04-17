package io.github.thatrobin.poltergeist.powers;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.PowerFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.thatrobin.poltergeist.component.BlockDurabilityComponent;
import net.fabricmc.yarn.constants.MiningLevels;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.state.property.Properties;
import io.github.thatrobin.poltergeist.Poltergeist;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

public class BlockPossession extends Power {

    private BlockState possessedBlock;
    private final int interval;

    public int miningLevel;

    private Integer initialTicks = null;

    public BlockPossession(PowerType<?> type, LivingEntity entity) {
        super(type, entity);
        interval = 20;
        this.setTicking();
    }

    public void setPossessedBlock(BlockState blockState, boolean removeFromWorld, boolean consumeHandItem) {
        if(consumeHandItem) {
            if(entity instanceof PlayerEntity player) {
                if(possessedBlock != null) {
                    player.getMainHandStack().decrement(1);
                    player.giveItemStack(possessedBlock.getBlock().asItem().getDefaultStack());
                    possessedBlock = blockState.contains(Properties.WATERLOGGED) ? blockState.with(Properties.WATERLOGGED, false) : blockState;
                }
            }
        }
        if (removeFromWorld) {
            entity.world.breakBlock(entity.getBlockPos(), false);
        }
        if (possessedBlock == null && !blockState.isIn(Poltergeist.UNPOSSESSABLE_BLOCKS)) {
            possessedBlock = blockState.contains(Properties.WATERLOGGED) ? blockState.with(Properties.WATERLOGGED, false) : blockState;
        }
        if(possessedBlock != null) {
            BlockDurabilityComponent component = BlockDurabilityComponent.KEY.get(entity);
            if(!component.hasBlock(possessedBlock.getBlock())) {
                component.addBlock(possessedBlock.getBlock());
            }
            miningLevel = getMiningLevel(possessedBlock.getBlock().asItem().getDefaultStack(), entity.world);
            component.sync();
        }
        PowerHolderComponent.sync(entity);
    }

    public void resetPossessedBlock() {
        if(this.possessedBlock != null) {
            this.possessedBlock = null;
            PowerHolderComponent.sync(entity);
        }
    }

    private int getMiningLevel(ItemStack inputStack, WorldView world) {
        World rWorld = (World)world;
        if (!inputStack.isEmpty()) {
            for (Recipe<?> recipe : rWorld.getRecipeManager().values()) {
                if (recipe instanceof CraftingRecipe rec && matches(inputStack, rec.getOutput())) {
                    for (Ingredient ingredient : rec.getIngredients()) {
                        for (ItemStack matchingStack : ingredient.getMatchingStacks()) {
                            if(matches(matchingStack, new ItemStack(Items.DIAMOND))) {
                                return MiningLevels.DIAMOND;
                            } else if(matches(matchingStack, new ItemStack(Items.IRON_INGOT))) {
                                return MiningLevels.IRON;
                            } else if(matches(matchingStack, new ItemStack(Items.COBBLESTONE))) {
                                return MiningLevels.STONE;
                            } else if(matchingStack.isIn(ItemTags.PLANKS)) {
                                return MiningLevels.WOOD;
                            } else {
                                return MiningLevels.HAND;
                            }
                        }

                    }

                }
            }
        }
        return MiningLevels.HAND;
    }

    private static boolean matches(ItemStack input, ItemStack output) {
        return input.getItem() == output.getItem() && input.getCount() >= output.getCount();
    }

    @Override
    public void tick() {
        if (initialTicks == null) {
            initialTicks = entity.age % interval;
        } else if (entity.age % interval == initialTicks) {
            if(entity instanceof PlayerEntity player && !player.isCreative()) {
                if (this.getPossessedBlock() != null) {
                    BlockDurabilityComponent component = BlockDurabilityComponent.KEY.get(entity);
                    Block block = this.getPossessedBlock().getBlock();
                    if (component.hasBlock(block)) {
                        if (this.getPossessedBlock().isIn(BlockTags.NEEDS_DIAMOND_TOOL)) {
                            component.degradeBlock(block, 1);
                        } else if (this.getPossessedBlock().isIn(BlockTags.NEEDS_IRON_TOOL)) {
                            component.degradeBlock(block, 2);
                        } else if (this.getPossessedBlock().isIn(BlockTags.NEEDS_STONE_TOOL)) {
                            component.degradeBlock(block, 3);
                        } else {
                            component.degradeBlock(block, 4);
                        }
                        if (!component.hasBlock(block)) {
                            resetPossessedBlock();
                        }
                    }
                    component.sync();
                }
            }
        }
    }

    public BlockState getPossessedBlock() {
        return possessedBlock;
    }

    public void setBoundingBox() {
        entity.setBoundingBox(calculateBoundingBox());
    }

    protected Box calculateBoundingBox() {
        return calculateBoundingBox(Direction.DOWN, 0f).offset(entity.getX(), entity.getY(), entity.getZ());
    }

    public static Box calculateBoundingBox(Direction direction, float extraLength) {
        return calculateBoundingBox(direction, -1.0F, extraLength);
    }

    public static Box calculateBoundingBox(Direction direction, float prevExtraLength, float extraLength) {
        double d = (double)Math.max(prevExtraLength, extraLength);
        double e = (double)Math.min(prevExtraLength, extraLength);
        return (new Box(BlockPos.ORIGIN)).stretch((double)direction.getOffsetX() * d, (double)direction.getOffsetY() * d, (double)direction.getOffsetZ() * d).shrink((double)(-direction.getOffsetX()) * (1.0D + e), (double)(-direction.getOffsetY()) * (1.0D + e), (double)(-direction.getOffsetZ()) * (1.0D + e));
    }

    public static PowerFactory<?> createFactory() {
        return new PowerFactory<>(Poltergeist.identifier("block_possession"),
                new SerializableData(),
                data ->
                        BlockPossession::new)
                .allowCondition();
    }

    @Override
    public NbtElement toTag() {
        NbtCompound compound = new NbtCompound();
        if(this.possessedBlock != null) {
            compound.put("blockState", NbtHelper.fromBlockState(this.possessedBlock));
        }
        return compound;
    }

    @Override
    public void fromTag(NbtElement tag) {
        if(tag instanceof NbtCompound compound) {
            if(compound.contains("blockState")) {
                if (compound.get("blockState") != null && compound.get("blockState") instanceof NbtCompound blockStateCompound) {
                    this.possessedBlock = NbtHelper.toBlockState(blockStateCompound);
                }
            }
        }
    }
}
