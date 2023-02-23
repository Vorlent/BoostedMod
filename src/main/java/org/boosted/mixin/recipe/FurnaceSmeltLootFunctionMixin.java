package org.boosted.mixin.recipe;

import net.minecraft.inventory.Inventory;
import net.minecraft.loot.function.FurnaceSmeltLootFunction;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(FurnaceSmeltLootFunction.class)
public abstract class FurnaceSmeltLootFunctionMixin {

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getRecipeManager()Lnet/minecraft/recipe/RecipeManager;"),
			method = "process")
	public RecipeManager skip$process$getRecipeManager(ServerWorld instance) {
		return null;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeManager;getFirstMatch(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/world/World;)Ljava/util/Optional;"),
			method = "process")
	public <C extends Inventory, T extends Recipe<C>> Optional<? extends Recipe<?>>
		redirect$process$getFirstMatch(RecipeManager instance, RecipeType<T> type, C inventory, World world) {

		if (world instanceof ServerWorld serverWorld) {
			return serverWorld.getSynchronizedServer().readExp(server -> server.getRecipeManager().getFirstMatch(type, inventory, world));
		} else {
			return world.getRecipeManager().getFirstMatch(type, inventory, world);
		}
	}
}
