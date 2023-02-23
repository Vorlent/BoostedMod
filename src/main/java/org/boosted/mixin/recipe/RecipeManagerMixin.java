package org.boosted.mixin.recipe;

import com.mojang.datafixers.util.Pair;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(targets = "net/minecraft/recipe/RecipeManager$1")
public abstract class RecipeManagerMixin {

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getRecipeManager()Lnet/minecraft/recipe/RecipeManager;"),
			method = "getFirstMatch")
	public RecipeManager skip$getFirstMatch$getRecipeManager(World instance) {
		return null;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeManager;getFirstMatch(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/world/World;Lnet/minecraft/util/Identifier;)Ljava/util/Optional;"),
			method = "getFirstMatch")
	public <C extends Inventory, T extends Recipe<C>> Optional<Pair<Identifier, T>>
		redirect$getFirstMatch$getFirstMatch(RecipeManager instance, RecipeType<T> type, C inventory, World world, @Nullable Identifier id) {

		if (world instanceof ServerWorld serverWorld) {
			return serverWorld.getSynchronizedServer().readExp(server -> server.getRecipeManager().getFirstMatch(type, inventory, world, id));
		} else {
			return world.getRecipeManager().getFirstMatch(type, inventory, world, id);
		}
	}
}
