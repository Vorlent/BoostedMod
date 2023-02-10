package org.boosted.mixin.getServer;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.KnowledgeBookItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.ArrayList;
import java.util.Optional;

@Mixin(KnowledgeBookItem.class)
public class KnowledgeBookItemMixin {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String RECIPES_KEY = "Recipes"; // copied

    /**
     * @author Vorlent
     * @reason recipe manager needs exclusive read access to server
     */
    @Overwrite
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        NbtCompound nbtCompound = itemStack.getNbt();
        if (!user.getAbilities().creativeMode) {
            user.setStackInHand(hand, ItemStack.EMPTY);
        }
        if (nbtCompound == null || !nbtCompound.contains(RECIPES_KEY, NbtElement.LIST_TYPE)) {
            LOGGER.error("Tag not valid: {}", (Object)nbtCompound);
            return TypedActionResult.fail(itemStack);
        }
        if (!world.isClient) {
            NbtList nbtList = nbtCompound.getList(RECIPES_KEY, NbtElement.STRING_TYPE);
            ArrayList<Recipe<?>> list = Lists.newArrayList();
            /* PATCH BEGIN */
            ServerWorld serverWorld = (ServerWorld)world;

            TypedActionResult<ItemStack> failure = serverWorld.getSynchronizedServer().readExp(server -> {
                RecipeManager recipeManager = server.getRecipeManager();
                for (int i = 0; i < nbtList.size(); ++i) {
                    String string = nbtList.getString(i);
                    Optional<? extends Recipe<?>> optional = recipeManager.get(new Identifier(string));
                    if (!optional.isPresent()) {
                        LOGGER.error("Invalid recipe: {}", (Object) string);
                        return TypedActionResult.fail(itemStack);
                    }
                    list.add(optional.get());
                }
                return null;
            });
            if (failure != null) {
                return failure;
            }
            /* PATCH END */

            user.unlockRecipes(list);
            user.incrementStat(Stats.USED.getOrCreateStat((KnowledgeBookItem)(Object)this));
        }
        return TypedActionResult.success(itemStack, world.isClient());
    }
}
