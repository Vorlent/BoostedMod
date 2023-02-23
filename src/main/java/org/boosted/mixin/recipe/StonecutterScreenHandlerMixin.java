package org.boosted.mixin.recipe;

import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.StonecutterScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.Optional;

@Mixin(StonecutterScreenHandler.class)
public abstract class StonecutterScreenHandlerMixin {

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getRecipeManager()Lnet/minecraft/recipe/RecipeManager;"),
		method = "updateInput")
	public RecipeManager skip$updateInput$getRecipeManager(World instance) {
		return null;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeManager;getAllMatches(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/world/World;)Ljava/util/List;"),
		method = "updateInput")
	public <C extends Inventory, T extends Recipe<C>> List<T>
		redirect$updateInput$getAllMatches(RecipeManager instance, RecipeType<T> type, C inventory, World world) {

		if (world instanceof ServerWorld serverWorld) {
			return serverWorld.getSynchronizedServer().readExp(server -> server.getRecipeManager().getAllMatches(type, inventory, world));
		} else {
			return world.getRecipeManager().getAllMatches(type, inventory, world);
		}
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getRecipeManager()Lnet/minecraft/recipe/RecipeManager;"),
			method = "transferSlot")
	public RecipeManager skip$transferSlot$getRecipeManager(World instance) {
		return null;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeManager;getFirstMatch(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/world/World;)Ljava/util/Optional;"),
			method = "transferSlot")
	public <C extends Inventory, T extends Recipe<C>> Optional<T>
		redirect$transferSlot$getFirstMatch(RecipeManager instance, RecipeType<T> type, C inventory, World world) {

		if (world instanceof ServerWorld serverWorld) {
			return serverWorld.getSynchronizedServer().readExp(server -> server.getRecipeManager().getFirstMatch(type, inventory, world));
		} else {
			return world.getRecipeManager().getFirstMatch(type, inventory, world);
		}
	}
}
