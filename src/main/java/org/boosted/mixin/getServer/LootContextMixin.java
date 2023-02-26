package org.boosted.mixin.getServer;

import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.timer.Timer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(LootContext.class)
public abstract class LootContextMixin {

    @Shadow public abstract ServerWorld getWorld();

    @Shadow public abstract LootTable getTable(Identifier id);

    @Shadow public abstract @Nullable LootCondition getCondition(Identifier id);

    private final ThreadLocal<Integer> callDepthGetTable = ThreadLocal.withInitial(()->0);
    private final ThreadLocal<Integer> callDepthGetCondition = ThreadLocal.withInitial(()->0);


    @Inject(method = "getTable", cancellable = true,
        at = @At(value = "HEAD"))
    public void wrapGetTable(Identifier id, CallbackInfoReturnable<LootTable> cir) {
        if (callDepthGetTable.get() == 0) {
            callDepthGetTable.set(callDepthGetTable.get() + 1);
            LootTable returnValue = getWorld().getSynchronizedServer()
                .readExp(minecraftServer -> this.getTable(id));
            callDepthGetTable.set(callDepthGetTable.get() - 1);
            cir.setReturnValue(returnValue);
        }
    }

    @Inject(method = "getCondition", cancellable = true,
        at = @At(value = "HEAD"))
    public void wrapGetCondition(Identifier id, CallbackInfoReturnable<LootCondition> cir) {
        if (callDepthGetCondition.get() == 0) {
            callDepthGetCondition.set(callDepthGetCondition.get() + 1);
            LootCondition returnValue = getWorld().getSynchronizedServer()
                    .readExp(minecraftServer -> this.getCondition(id));
            callDepthGetCondition.set(callDepthGetCondition.get() - 1);
            cir.setReturnValue(returnValue);
        }
    }
}
