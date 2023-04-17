package io.github.thatrobin.poltergeist.powers.factories;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import io.github.thatrobin.poltergeist.Poltergeist;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BlockConditions {

    public static void register() {
        register(new ConditionFactory<>(Poltergeist.identifier("has_item_in_recipe"), new SerializableData()
                .add("item_condition", ApoliDataTypes.ITEM_CONDITION, null),
                (data, block) -> {
                    BlockState state = block.getBlockState();
                    ItemStack itemStack = state.getBlock().asItem().getDefaultStack();
                    ConditionFactory<ItemStack>.Instance condition = data.get("item_condition");
                    boolean found = false;
                    if(condition != null) {
                        try {
                            CraftingRecipe[] recipes = BlockConditions.getRecipesFor(itemStack, block.getWorld(), condition);
                            if(recipes.length > 0) {
                                found = true;
                            }
                        } catch (ClassCastException exception) {
                            Poltergeist.LOGGER.error(exception.getMessage());
                        }
                    } else {
                        found = true;
                    }
                    return found;
                }));
    }

    public static CraftingRecipe[] getRecipesFor(ItemStack inputStack, WorldView world, ConditionFactory<ItemStack>.Instance condition) {
        World rWorld = (World)world;
        List<CraftingRecipe> recipes = new ArrayList<>();

        if (!inputStack.isEmpty()) {
            for (Recipe<?> recipe : rWorld.getRecipeManager().values()) {
                if (recipe instanceof CraftingRecipe rec && matches(inputStack, rec.getOutput())) {
                    for (Ingredient ingredient : rec.getIngredients()) {
                        for (ItemStack matchingStack : ingredient.getMatchingStacks()) {
                            if(condition == null || condition.test(matchingStack)) {
                                if(!recipes.contains(rec)) {
                                    recipes.add(rec);
                                }
                            }
                        }

                    }

                }
            }
        }

        return recipes.toArray(new CraftingRecipe[0]);
    }

    private static boolean matches(ItemStack input, ItemStack output) {
        return input.getItem() == output.getItem() && input.getCount() >= output.getCount();
    }

    private static void register(ConditionFactory<CachedBlockPosition> conditionFactory) {
        Registry.register(ApoliRegistries.BLOCK_CONDITION, conditionFactory.getSerializerId(), conditionFactory);
    }
}
