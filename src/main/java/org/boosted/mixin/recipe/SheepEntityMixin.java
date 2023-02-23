package org.boosted.mixin.recipe;

import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(SheepEntity.class)
public abstract class SheepEntityMixin {

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getRecipeManager()Lnet/minecraft/recipe/RecipeManager;"),
			method = "getChildColor")
	public RecipeManager skip$getChildColor$getRecipeManager(World instance) {
		return null;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeManager;getFirstMatch(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/world/World;)Ljava/util/Optional;"),
			method = "getChildColor")
	public <C extends Inventory, T extends Recipe<C>> Optional<? extends Recipe<?>>
		redirect$getChildColor$getFirstMatch(RecipeManager instance, RecipeType<T> type, C inventory, World world) {

		if (world instanceof ServerWorld serverWorld) {
			return serverWorld.getSynchronizedServer().readExp(server -> server.getRecipeManager().getFirstMatch(type, inventory, world));
		} else {
			return world.getRecipeManager().getFirstMatch(type, inventory, world);
		}
	}
}
