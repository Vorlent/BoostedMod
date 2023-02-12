package org.boosted.mixin.getServer;

import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.provider.nbt.StorageLootNbtProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StorageLootNbtProvider.class)
public class StorageLootNbtProviderMixin {

    @Shadow @Final public Identifier source;

    @Inject(method = "getNbt(Lnet/minecraft/loot/context/LootContext;)Lnet/minecraft/nbt/NbtElement;", cancellable = true,
        at = @At("HEAD"))
    public void getNbt(LootContext context, CallbackInfoReturnable<NbtElement> cir) {
        NbtCompound nbtCompound = context.getWorld().getSynchronizedServer().readExp(server ->
                server.getDataCommandStorage().get(this.source));
        cir.setReturnValue(nbtCompound);
    }
}
