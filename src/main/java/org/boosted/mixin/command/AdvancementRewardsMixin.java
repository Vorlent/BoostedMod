package org.boosted.mixin.command;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AdvancementRewards.class)
public class AdvancementRewardsMixin {

    @Shadow @Final private CommandFunction.LazyContainer function;

    @Inject(method = "apply", cancellable = true,
        at = @At(value = "FIELD", target = "net/minecraft/server/network/ServerPlayerEntity.server : Lnet/minecraft/server/MinecraftServer;",
        ordinal = 1))
    public void redirectCommandExecute(ServerPlayerEntity player, CallbackInfo ci) {
        MinecraftServer minecraftServer = player.server;
        minecraftServer.getSynchronizedServer().write(server -> {
            this.function.get(minecraftServer.getCommandFunctionManager())
                .ifPresent(function -> minecraftServer.getCommandFunctionManager()
                    .execute((CommandFunction)function, player.getCommandSource().withSilent().withLevel(2)));
        });
        ci.cancel();
    }
}
