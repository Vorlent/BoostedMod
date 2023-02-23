package org.boosted.mixin.recipe;

import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.SmithingScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(SmithingScreenHandler.class)
public abstract class SmithingScreenHandlerMixin {

	@Shadow @Final private World world;

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getRecipeManager()Lnet/minecraft/recipe/RecipeManager;"),
		method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V")
	public RecipeManager skip$init$getRecipeManager(World instance) {
		return null;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeManager;listAllOfType(Lnet/minecraft/recipe/RecipeType;)Ljava/util/List;"),
		method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V")
	public <C extends Inventory, T extends Recipe<C>> List<T>
		redirect$init$listAllOfType(RecipeManager instance, RecipeType<T> type) {

		if (world instanceof ServerWorld serverWorld) {
			return serverWorld.getSynchronizedServer().readExp(server -> server.getRecipeManager().listAllOfType(type));
		} else {
			return world.getRecipeManager().listAllOfType(type);
		}
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getRecipeManager()Lnet/minecraft/recipe/RecipeManager;"),
			method = "updateResult")
	public RecipeManager skip$updateResult$getRecipeManager(World instance) {
		return null;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeManager;getAllMatches(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/world/World;)Ljava/util/List;"),
			method = "updateResult")
	public <C extends Inventory, T extends Recipe<C>> List<T>
		redirect$updateResult$getAllMatches(RecipeManager instance, RecipeType<T> type, C inventory, World world) {

		if (world instanceof ServerWorld serverWorld) {
			return serverWorld.getSynchronizedServer().readExp(server -> server.getRecipeManager().getAllMatches(type, inventory, world));
		} else {
			return world.getRecipeManager().getAllMatches(type, inventory, world);
		}
	}
}
