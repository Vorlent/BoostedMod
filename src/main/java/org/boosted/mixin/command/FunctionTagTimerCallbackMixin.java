package org.boosted.mixin.command;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.timer.FunctionTagTimerCallback;
import net.minecraft.world.timer.FunctionTimerCallback;
import net.minecraft.world.timer.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FunctionTagTimerCallback.class)
public abstract class FunctionTagTimerCallbackMixin {

    private final ThreadLocal<Integer> callDepth = ThreadLocal.withInitial(()->0);

    @Shadow public abstract void call(MinecraftServer minecraftServer, Timer<MinecraftServer> timer, long l);

    @Inject(method = "call(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/world/timer/Timer;J)V",
        cancellable = true, at = @At(value = "HEAD"))
    private void redirectCall(MinecraftServer minecraftServer, Timer<MinecraftServer> timer, long l, CallbackInfo ci) {
        if (callDepth.get() == 0) {
            callDepth.set(callDepth.get() + 1);
            minecraftServer.getSynchronizedServer()
                .write(server -> this.call(server, timer, l));
            callDepth.set(callDepth.get() - 1);
            ci.cancel();
        }
    }
}
