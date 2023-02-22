package org.boosted.mixin.command;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.DebugCommand;
import net.minecraft.server.command.FunctionCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.world.timer.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(DebugCommand.class)
public abstract class DebugCommandMixin {

    private static final ThreadLocal<Integer> callDepth = ThreadLocal.withInitial(()->0);

    @Inject(method = "executeFunction",
            cancellable = true, at = @At(value = "HEAD"))
    private static void redirect$executeFunction(ServerCommandSource source, Collection<CommandFunction> functions, CallbackInfoReturnable<Integer> cir) {
        if (callDepth.get() == 0) {
            callDepth.set(callDepth.get() + 1);
            int i = source.getServer().getSynchronizedServer()
                    .writeExp(server -> DebugCommand.executeFunction(source, functions));
            callDepth.set(callDepth.get() - 1);
            cir.setReturnValue(i);
        }
    }
}
