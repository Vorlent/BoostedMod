package org.boosted.mixin.getServer;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    /*
        INVOKEVIRTUAL
        INVOKEVIRTUAL
     */
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
        Entity entity = (Entity) (Object) this;
        ServerWorld serverWorld = (ServerWorld)entity.world;
        serverWorld.getSynchronizedServer().write(minecraftServer -> {
            int i = entity.getMaxNetherPortalTime();
            ServerWorld serverWorld2 = minecraftServer.getWorld(entity.world.getRegistryKey() == World.NETHER ? World.OVERWORLD : World.NETHER);
            if (serverWorld2 != null && minecraftServer.isNetherAllowed() && !entity.hasVehicle() && entity.netherPortalTime++ >= i) {
                entity.world.getProfiler().push("portal");
                entity.netherPortalTime = i;
                entity.resetPortalCooldown();
                entity.moveToWorld(serverWorld2);
                entity.world.getProfiler().pop();
            }
        });
    }

    @Inject(method = "getServer()Lnet/minecraft/server/MinecraftServer;",
        at = @At(value = "HEAD"))
    public void blockGetServer(CallbackInfoReturnable<MinecraftServer> cir) {
        // TODO mixins for Entity.getServer()
        throw new UnsupportedOperationException();
    }



    /** unfixable
    public ServerCommandSource getCommandSource() {
        return new ServerCommandSource(this, this.getPos(), this.getRotationClient(), this.world instanceof ServerWorld ? (ServerWorld)this.world : null, this.getPermissionLevel(), this.getName().getString(), this.getDisplayName(), this.world.getServer(), this);
    }*/
}
