package org.boosted.mixin.getServer;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @Inject(method = "writeNbtToBlockEntity(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/item/ItemStack;)Z",
        cancellable = true, at = @At(value = "HEAD"))
    private static void writeNbtToBlockEntity(World world, PlayerEntity player, BlockPos pos, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (world instanceof ServerWorld serverWorld) {
            if (serverWorld.getSynchronizedServer().readExp(server -> server == null)) {
                cir.setReturnValue(false);
            }
        } else { // skip ClientWorld
            cir.setReturnValue(false);
        }
    }

    @Redirect(method = "writeNbtToBlockEntity(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/item/ItemStack;)Z",
            at = @At(value = "INVOKE", target = "net/minecraft/world/World.getServer ()Lnet/minecraft/server/MinecraftServer;"))
    private static MinecraftServer skipIf(World instance) {
        // this reference does not leak
        // it just bypasses the if statement, which has been reimplemented above
        // also it is guaranteed to be of type ServerWorld as the above Inject returns if it is not
        return ((ServerWorld)instance).getUnsynchronizedServer();
    }
}
