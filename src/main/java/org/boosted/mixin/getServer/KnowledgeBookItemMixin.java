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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Mixin(KnowledgeBookItem.class)
public class KnowledgeBookItemMixin {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String RECIPES_KEY = "Recipes"; // copied

    @Redirect(method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;",
        at = @At(value = "INVOKE", target = "net/minecraft/nbt/NbtCompound.getList (Ljava/lang/String;I)Lnet/minecraft/nbt/NbtList;"))
    private NbtList skipGetList(NbtCompound instance, String key, int type) {
        return new NbtList(); // return empty list to skip the for loop
    }

    @Redirect(method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;",
            at = @At(value = "INVOKE", target = "net/minecraft/world/World.getServer ()Lnet/minecraft/server/MinecraftServer;"))
    private MinecraftServer skipGetServer(World instance) {
        return null;
    }

    @Redirect(method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;",
            at = @At(value = "INVOKE", target = "net/minecraft/server/MinecraftServer.getRecipeManager ()Lnet/minecraft/recipe/RecipeManager;"))
    private RecipeManager skipGetRecipeManager(MinecraftServer instance) {
        return null;
    }

    @Redirect(method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;",
            at = @At(value = "INVOKE", target = "net/minecraft/entity/player/PlayerEntity.unlockRecipes (Ljava/util/Collection;)I"))
    private int skipUnlockRecipes(PlayerEntity instance, Collection<Recipe<?>> recipes) {
        return 0;
    }

    @Inject(method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;",
        cancellable = true,
        at = @At(value = "INVOKE", target = "net/minecraft/entity/player/PlayerEntity.incrementStat (Lnet/minecraft/stat/Stat;)V",
        shift = At.Shift.BEFORE))
    private void redirectUnlockRecipes(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ArrayList<Recipe<?>> list = Lists.newArrayList();
        ItemStack itemStack = user.getStackInHand(hand);
        NbtCompound nbtCompound = itemStack.getNbt();
        NbtList nbtList = nbtCompound.getList(RECIPES_KEY, NbtElement.STRING_TYPE);
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
            cir.setReturnValue(failure);
        } else {
            user.unlockRecipes(list);
        }
    }
}
