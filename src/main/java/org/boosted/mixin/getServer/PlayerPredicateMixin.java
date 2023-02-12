package org.boosted.mixin.getServer;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.entity.Entity;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.PlayerPredicate;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stat;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Mixin(PlayerPredicate.class)
public class PlayerPredicateMixin {

    @Shadow @Final private Map<Identifier, PlayerPredicate.AdvancementPredicate> advancements;

    @Redirect(method = "test(Lnet/minecraft/entity/Entity;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/Vec3d;)Z",
        at = @At(value = "INVOKE", target = "java/util/Map.entrySet ()Ljava/util/Set;", ordinal = 1))
    public <K,V> Set<Map.Entry<K, V>> skipForLoop(Map instance) {
        return Collections.emptySet();
    }

    @Redirect(method = "test(Lnet/minecraft/entity/Entity;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/Vec3d;)Z",
        at = @At(value = "INVOKE", target = "net/minecraft/server/network/ServerPlayerEntity.getServer ()Lnet/minecraft/server/MinecraftServer;"))
    public MinecraftServer skipGetServer(ServerPlayerEntity instance) {
        return null;
    }

    @Redirect(method = "test(Lnet/minecraft/entity/Entity;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/Vec3d;)Z",
            at = @At(value = "INVOKE", target = "net/minecraft/server/MinecraftServer.getAdvancementLoader ()Lnet/minecraft/server/ServerAdvancementLoader;"))
    public ServerAdvancementLoader skipGetAdvancementLoader(MinecraftServer instance) {
        return null;
    }

    @Inject(method = "test(Lnet/minecraft/entity/Entity;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/Vec3d;)Z", cancellable = true,
        at = @At(value = "INVOKE", target = "net/minecraft/server/MinecraftServer.getAdvancementLoader ()Lnet/minecraft/server/ServerAdvancementLoader;",
        shift = At.Shift.AFTER))
    public void injectTest(Entity entity, ServerWorld world, Vec3d pos, CallbackInfoReturnable<Boolean> cir) {
        if (!(entity instanceof ServerPlayerEntity)) {
            return;
        }
        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
        PlayerAdvancementTracker playerAdvancementTracker = serverPlayerEntity.getAdvancementTracker();

        Boolean result = serverPlayerEntity.getWorld().getSynchronizedServer().readExp(server -> {
            ServerAdvancementLoader serverAdvancementLoader = server.getAdvancementLoader();
            for (Map.Entry<Identifier, PlayerPredicate.AdvancementPredicate> entry3 : this.advancements.entrySet()) {
                Advancement advancement = serverAdvancementLoader.get(entry3.getKey());
                if (advancement != null && entry3.getValue().test(playerAdvancementTracker.getProgress(advancement)))
                    continue;
                return false;
            }
            return true;
        });
        if (!result) {
           cir.setReturnValue(result);
        }
    }
}
