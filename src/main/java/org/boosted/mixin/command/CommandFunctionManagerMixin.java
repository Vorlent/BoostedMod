package org.boosted.mixin.command;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.FunctionCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CommandFunctionManager.class)
public class CommandFunctionManagerMixin {

    @Redirect(method = "executeAll",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/function/CommandFunctionManager;execute(Lnet/minecraft/server/function/CommandFunction;Lnet/minecraft/server/command/ServerCommandSource;)I"))
    private int redirect$executeAll$execute(CommandFunctionManager instance, CommandFunction function, ServerCommandSource source) {
        return source.getServer().getSynchronizedServer()
            .writeExp(server -> instance.execute(function, source));
    }
}
