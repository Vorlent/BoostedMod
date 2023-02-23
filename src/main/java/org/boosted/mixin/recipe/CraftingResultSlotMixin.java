package org.boosted.mixin.recipe;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CraftingResultSlot.class)
public abstract class CraftingResultSlotMixin {

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getRecipeManager()Lnet/minecraft/recipe/RecipeManager;"),
		method = "onTakeItem")
	public RecipeManager skip$updateInput$getRecipeManager(World instance) {
		return null;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/recipe/RecipeManager;getRemainingStacks(Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/world/World;)Lnet/minecraft/util/collection/DefaultedList;"),
		method = "onTakeItem")
	public <C extends Inventory, T extends Recipe<C>> DefaultedList<ItemStack>
		redirect$updateInput$getAllMatches(RecipeManager instance, RecipeType<T> type, C inventory, World world) {

		if (world instanceof ServerWorld serverWorld) {
			return serverWorld.getSynchronizedServer().readExp(server -> server.getRecipeManager().getRemainingStacks(type, inventory, world));
		} else {
			return world.getRecipeManager().getRemainingStacks(type, inventory, world);
		}
	}
}
