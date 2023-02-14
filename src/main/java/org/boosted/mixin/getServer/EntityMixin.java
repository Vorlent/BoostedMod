package org.boosted.mixin.getServer;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.boosted.ThreadCoordinator;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow public int netherPortalTime;

    @Shadow public abstract void resetPortalCooldown();

    @Shadow public abstract @Nullable Entity moveToWorld(ServerWorld destination);

    @Shadow public abstract World getWorld();

    @Shadow public abstract int getMaxNetherPortalTime();

    @Shadow public abstract boolean hasVehicle();

    @Shadow public abstract Vec3d getPos();

    @Shadow public abstract Vec2f getRotationClient();

    @Shadow protected abstract int getPermissionLevel();

    @Shadow public abstract Text getName();

    @Shadow public abstract Text getDisplayName();

    @Redirect(method = "tickPortal()V",
            at = @At(value = "INVOKE", target = "net/minecraft/server/world/ServerWorld.getServer ()Lnet/minecraft/server/MinecraftServer;"))
    public MinecraftServer skipGetServer(ServerWorld instance) {
        return null;
    }

    @Redirect(method = "tickPortal()V",
            at = @At(value = "INVOKE", target = "net/minecraft/server/MinecraftServer.getWorld (Lnet/minecraft/util/registry/RegistryKey;)Lnet/minecraft/server/world/ServerWorld;"))
    public @Nullable ServerWorld skipGetWorld(MinecraftServer instance, RegistryKey<World> key) {
        return null;
    }

    @Inject(method = "tickPortal()V",
        at = @At(value = "FIELD", target = "net/minecraft/entity/Entity.inNetherPortal : Z", ordinal = 1, shift = At.Shift.BEFORE))
    public void injectTickPortal(CallbackInfo ci) {
        ServerWorld serverWorld = (ServerWorld)this.getWorld();
        serverWorld.getSynchronizedServer().write(minecraftServer -> {
            int i = this.getMaxNetherPortalTime();
            //TODO getWorld leaks references to other worlds
            ServerWorld serverWorld2 = minecraftServer.getWorld(serverWorld.getRegistryKey() == World.NETHER ? World.OVERWORLD : World.NETHER);
            if (serverWorld2 != null && minecraftServer.isNetherAllowed() && !this.hasVehicle() && this.netherPortalTime++ >= i) {
                serverWorld.getProfiler().push("portal");
                this.netherPortalTime = i;
                this.resetPortalCooldown();
                ThreadCoordinator.getInstance().getBoostedContext().postTick().execute(() ->
                    this.moveToWorld(serverWorld2)
                );
                serverWorld.getProfiler().pop();
            }
        });
    }

    @Inject(method = "getServer()Lnet/minecraft/server/MinecraftServer;",
        at = @At(value = "HEAD"))
    public void blockGetServer(CallbackInfoReturnable<MinecraftServer> cir) {
        throw new UnsupportedOperationException();
    }

    /**
     * @author Vorlent
     * @reason I could @Redirect getServer() instead
     */
    @Overwrite
    public ServerCommandSource getCommandSource() {
        return new ServerCommandSource((Entity)(Object)this, this.getPos(), this.getRotationClient(),
                this.getWorld() instanceof ServerWorld ? (ServerWorld)this.getWorld() : null,
                this.getPermissionLevel(), this.getName().getString(),
                this.getDisplayName(),
                this.getWorld() instanceof ServerWorld ? ((ServerWorld)this.getWorld()).getUnsynchronizedServer() : null,
                (Entity)(Object)this);
    }
}
