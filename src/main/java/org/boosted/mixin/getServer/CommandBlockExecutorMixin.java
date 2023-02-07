package org.boosted.mixin.getServer;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.world.CommandBlockExecutor;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CommandBlockExecutor.class)
public class CommandBlockExecutorMixin {

    @Redirect(method="execute(Lnet/minecraft/world/World;)Z",
        at = @At(value ="INVOKE", target="net/minecraft/server/world/ServerWorld.getServer ()Lnet/minecraft/server/MinecraftServer;"))
    public MinecraftServer skipGetServer(ServerWorld instance) {
        return null;
    }

    @Redirect(method="execute(Lnet/minecraft/world/World;)Z",
            at = @At(value ="INVOKE", target="net/minecraft/server/MinecraftServer.areCommandBlocksEnabled ()Z"))
    public boolean readLockAreCommandBlocksEnabled(MinecraftServer instance) {
        return ((CommandBlockExecutor)(Object)this).getWorld().getSynchronizedServer().readExp(server -> server.areCommandBlocksEnabled());
    }

    @Redirect(method="execute(Lnet/minecraft/world/World;)Z",
            at = @At(value ="INVOKE", target="net/minecraft/server/MinecraftServer.getCommandManager ()Lnet/minecraft/server/command/CommandManager;"))
    public CommandManager skipGetCommandManager(MinecraftServer instance) {
        return null;
    }

    @Redirect(method="execute(Lnet/minecraft/world/World;)Z",
            at = @At(value ="INVOKE", target="net/minecraft/server/command/CommandManager.executeWithPrefix (Lnet/minecraft/server/command/ServerCommandSource;Ljava/lang/String;)I"))
    public int skipExecuteWithPrefix(CommandManager instance, ServerCommandSource serverCommandSource, String command) {
        CommandBlockExecutor commandBlockExecutor = (CommandBlockExecutor) (Object) this;
        return commandBlockExecutor.getWorld().getSynchronizedServer().writeExp(server ->
                server.getCommandManager().executeWithPrefix(serverCommandSource, commandBlockExecutor.getCommand()));
    }
}
