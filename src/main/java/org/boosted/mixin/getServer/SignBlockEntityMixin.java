package org.boosted.mixin.getServer;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SignBlockEntity.class)
public abstract class SignBlockEntityMixin {

    @Shadow protected abstract Text[] getTexts(boolean filtered);

    @Shadow public abstract ServerCommandSource getCommandSource(@Nullable ServerPlayerEntity player);

    private ServerWorld serverWorld; // ugly, check if it is thread safe...

    /** Unfortunately not possible yet
   public ServerCommandSource getCommandSource(@Nullable ServerPlayerEntity player) {
        String string = player == null ? "Sign" : player.getName().getString();
        Text text = player == null ? Text.literal("Sign") : player.getDisplayName();
        return new ServerCommandSource(CommandOutput.DUMMY, Vec3d.ofCenter(this.pos), Vec2f.ZERO, (ServerWorld)this.world, 2, string, text, this.world.getServer(), player);
   }*/

    @Redirect(method = "onActivate(Lnet/minecraft/server/network/ServerPlayerEntity;)Z",
            at = @At(value = "INVOKE", target = "net/minecraft/server/network/ServerPlayerEntity.getServer ()Lnet/minecraft/server/MinecraftServer;"))
    public MinecraftServer skipGetServer(ServerPlayerEntity instance) {
        return null;
    }

    @Redirect(method = "onActivate(Lnet/minecraft/server/network/ServerPlayerEntity;)Z",
            at = @At(value = "INVOKE", target = "net/minecraft/server/MinecraftServer.getCommandManager ()Lnet/minecraft/server/command/CommandManager;"))
    public CommandManager skipGetCommandManager(MinecraftServer instance) {
        return null;
    }

    @Redirect(method = "onActivate(Lnet/minecraft/server/network/ServerPlayerEntity;)Z",
            at = @At(value = "INVOKE", target = "net/minecraft/server/command/CommandManager.executeWithPrefix (Lnet/minecraft/server/command/ServerCommandSource;Ljava/lang/String;)I"))
    public int redirectExecuteWithPrefix(CommandManager instance, ServerCommandSource source, String command) {
        SignBlockEntity signBlockEntity = (SignBlockEntity) (Object) this;
        World world = signBlockEntity.getWorld();
        if (world instanceof ServerWorld serverWorld) {
            return serverWorld.getSynchronizedServer().writeExp(server ->
                server.getCommandManager().executeWithPrefix(source, command));
        }
        return instance.executeWithPrefix(source, command);
    }
}
