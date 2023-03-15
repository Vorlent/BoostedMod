package org.boosted.mixin.getServer;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.command.EntitySelectorOptions;
import net.minecraft.entity.Entity;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.predicate.NumberRange;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Predicate;

@Mixin(EntitySelectorOptions.class)
public abstract class EntitySelectorOptionsMixin {

    @Inject(method = "method_9937(Ljava/util/Map;Lnet/minecraft/entity/Entity;)Z", cancellable = true,
            at = @At(value = "INVOKE", target = "net/minecraft/entity/Entity.getServer ()Lnet/minecraft/server/MinecraftServer;", shift = At.Shift.BEFORE))
    private static void injectScores(Map<String, NumberRange.IntRange> map, Entity entity, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
        Boolean result = serverPlayerEntity.getWorld().getSynchronizedServer().readExp(server -> {
            ServerScoreboard scoreboard = server.getScoreboard();
            String string = entity.getEntityName();
            for (Map.Entry entry : map.entrySet()) {
                ScoreboardObjective scoreboardObjective = scoreboard.getNullableObjective((String)entry.getKey());
                if (scoreboardObjective == null) {
                    return false;
                }
                if (!scoreboard.playerHasObjective(string, scoreboardObjective)) {
                    return false;
                }
                ScoreboardPlayerScore scoreboardPlayerScore = scoreboard.getPlayerScore(string, scoreboardObjective);
                int i = scoreboardPlayerScore.getScore();
                if (((NumberRange.IntRange)entry.getValue()).test(i)) continue;
                return false;
            }
            return true;
        });
        cir.setReturnValue(result);
    }

    @Inject(method = "method_9958(Ljava/util/Map;Lnet/minecraft/entity/Entity;)Z", cancellable = true,
        at = @At(value = "INVOKE", target = "net/minecraft/server/network/ServerPlayerEntity.getAdvancementTracker ()Lnet/minecraft/advancement/PlayerAdvancementTracker;", shift = At.Shift.BEFORE))
    private static void injectAdvancements(Map<String, NumberRange.IntRange> map, Entity entity, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
        Boolean result = serverPlayerEntity.getWorld().getSynchronizedServer().readExp(server -> {
            PlayerAdvancementTracker playerAdvancementTracker = serverPlayerEntity.getAdvancementTracker();
            ServerAdvancementLoader serverAdvancementLoader = server.getAdvancementLoader();
            for (Map.Entry entry : map.entrySet()) {
                Advancement advancement = serverAdvancementLoader.get((Identifier)entry.getKey());
                if (advancement != null && ((Predicate<AdvancementProgress>)entry.getValue())
                    .test(playerAdvancementTracker.getProgress(advancement))) continue;
                return false;
            }
            return true;
        });
        cir.setReturnValue(result);
    }

    @Inject(method = "method_22823(Lnet/minecraft/util/Identifier;ZLnet/minecraft/entity/Entity;)Z", cancellable = true,
        at = @At(value = "INVOKE", target = "net/minecraft/server/world/ServerWorld.getServer ()Lnet/minecraft/server/MinecraftServer;", shift = At.Shift.BEFORE))
    private static void injectPredicate(Identifier identifier, boolean bl, Entity entity, CallbackInfoReturnable<Boolean> cir) {
        ServerWorld serverWorld = (ServerWorld)entity.world;
        Boolean result = serverWorld.getSynchronizedServer().readExp(server -> {
            LootCondition lootCondition = server.getPredicateManager().get(identifier);
            if (lootCondition == null) {
                return false;
            }
            LootContext lootContext = new LootContext.Builder(serverWorld)
                    .parameter(LootContextParameters.THIS_ENTITY, entity)
                    .parameter(LootContextParameters.ORIGIN, entity.getPos())
                    .build(LootContextTypes.SELECTOR);
            return bl ^ lootCondition.test(lootContext);
        });
        cir.setReturnValue(result);
    }
}
