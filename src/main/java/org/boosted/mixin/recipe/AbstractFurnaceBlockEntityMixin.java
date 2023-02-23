package org.boosted.mixin.recipe;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityMixin {

	private final ThreadLocal<ServerWorld> serverWorldThreadLocal = new ThreadLocal<>();

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getRecipeManager()Lnet/minecraft/recipe/RecipeManager;"),
			method = "getRecipesUsedAndDropExperience")
	public RecipeManager skip$getRecipesUsedAndDropExperience$getRecipeManager(ServerWorld instance) {
		serverWorldThreadLocal.set(instance);
		return null;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeManager;get(Lnet/minecraft/util/Identifier;)Ljava/util/Optional;"),
			method = "getRecipesUsedAndDropExperience")
	public Optional<? extends Recipe<?>> skip$getRecipesUsedAndDropExperience$get(RecipeManager instance, Identifier id) {
		ServerWorld serverWorld = serverWorldThreadLocal.get();
		serverWorldThreadLocal.remove();
		return serverWorld.getSynchronizedServer().readExp(server -> server.getRecipeManager().get(id));
	}
}
