package org.boosted.mixin.getServer;

import com.google.common.collect.Sets;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LootContext.Builder.class)
public class LootContextBuilderMixin {

    @Inject(method = "build(Lnet/minecraft/loot/context/LootContextType;)Lnet/minecraft/loot/context/LootContext;", cancellable = true,
     at = @At(value = "FIELD", target = "net/minecraft/loot/context/LootContext$Builder.world : Lnet/minecraft/server/world/ServerWorld;", ordinal = 0))
    public void build(LootContextType type, CallbackInfoReturnable<LootContext> cir) {
        LootContext.Builder thisContext = (LootContext.Builder)(Object)this;
        Random random = thisContext.random == null ? Random.create() : thisContext.random;
        LootContext returnValue = thisContext.getWorld().getSynchronizedServer().writeExp(minecraftServer -> {
            // TODO this is indirectly leaking references to getServer by passing in
            LootContext lootContext = new LootContext(random, thisContext.luck, thisContext.getWorld(),
                    minecraftServer.getLootManager()::getTable, minecraftServer.getPredicateManager()::get,
                    thisContext.parameters, thisContext.drops);
            return lootContext;
        });
        cir.setReturnValue(returnValue);
    }
}
