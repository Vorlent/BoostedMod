package org.boosted.mixin.command;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.FunctionCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.CommandFunctionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FunctionCommand.class)
public class FunctionCommandMixin {

    @Redirect(method = "execute",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/command/ServerCommandSource;getServer()Lnet/minecraft/server/MinecraftServer;"))
    private static MinecraftServer redirectCommandExecute(ServerCommandSource instance) {
        return null;
    }

    @Redirect(method = "execute",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getCommandFunctionManager()Lnet/minecraft/server/function/CommandFunctionManager;"))
    private static CommandFunctionManager redirect$execute$getCommandFunctionManager(MinecraftServer instance) {
        return null;
    }

    @Redirect(method = "execute",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/function/CommandFunctionManager;execute(Lnet/minecraft/server/function/CommandFunction;Lnet/minecraft/server/command/ServerCommandSource;)I"))
    private static int redirect$execute$execute(CommandFunctionManager instance, CommandFunction function, ServerCommandSource source) {
        return source.getServer().getSynchronizedServer()
            .writeExp(server -> source.getServer().getCommandFunctionManager().execute(function, source));
    }
}
