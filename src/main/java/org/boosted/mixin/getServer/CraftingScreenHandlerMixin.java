package org.boosted.mixin.getServer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(CraftingScreenHandler.class)
public class CraftingScreenHandlerMixin {

    @Redirect(method = "updateResult(Lnet/minecraft/screen/ScreenHandler;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/inventory/CraftingInventory;Lnet/minecraft/inventory/CraftingResultInventory;)V",
        at = @At(value = "INVOKE", target = "net/minecraft/world/World.getServer ()Lnet/minecraft/server/MinecraftServer;"))
    private static MinecraftServer redirectGetServer(World world) {
        return null;
    }

    @Redirect(method = "updateResult(Lnet/minecraft/screen/ScreenHandler;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/inventory/CraftingInventory;Lnet/minecraft/inventory/CraftingResultInventory;)V",
        at = @At(value = "INVOKE", target = "net/minecraft/server/MinecraftServer.getRecipeManager ()Lnet/minecraft/recipe/RecipeManager;"))
    private static RecipeManager redirectGetRecipeManager(MinecraftServer minecraftServer) {
        return null;
    }

    @Redirect(method = "updateResult(Lnet/minecraft/screen/ScreenHandler;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/inventory/CraftingInventory;Lnet/minecraft/inventory/CraftingResultInventory;)V",
        at = @At(value = "INVOKE", target = "net/minecraft/recipe/RecipeManager.getFirstMatch (Lnet/minecraft/recipe/RecipeType;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/world/World;)Ljava/util/Optional;"))
    private static <C extends Inventory, T extends Recipe<C>> Optional<T> redirectGetFirstMatch(RecipeManager instance, RecipeType<T> type, C craftingInventory, World world) {
        if (world instanceof ServerWorld serverWorld) {
            return serverWorld.getSynchronizedServer().readExp(server -> server.getRecipeManager().getFirstMatch(type, craftingInventory, world));
        } else {
            return world.getServer().getRecipeManager().getFirstMatch(type, craftingInventory, world);
        }
    }
}
