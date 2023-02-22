package org.boosted.mixin.serverplayerentity;

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
    private ThreadLocal<MinecraftServer> minecraftServerThreadLocal = new ThreadLocal<>();
    private ThreadLocal<Identifier> identifierThreadLocal = new ThreadLocal<>();

    @Redirect(method = "apply",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getLootManager()Lnet/minecraft/loot/LootManager;"))
    private LootManager redirect$apply$getLootManager(MinecraftServer instance) {
        minecraftServerThreadLocal.set(instance);
        return null;
    }

    @Redirect(method = "apply",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/LootManager;getTable(Lnet/minecraft/util/Identifier;)Lnet/minecraft/loot/LootTable;"))
    private LootTable redirect$apply$getTable(LootManager instance, Identifier id) {
        identifierThreadLocal.set(id);
        return null;
    }

    @Redirect(method = "apply",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/LootTable;generateLoot(Lnet/minecraft/loot/context/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"))
    private ObjectArrayList<ItemStack> redirect$apply$generateLoot(LootTable instance, LootContext context) {
        MinecraftServer minecraftServer = minecraftServerThreadLocal.get();
        minecraftServerThreadLocal.remove();
        Identifier identifier = identifierThreadLocal.get();
        identifierThreadLocal.remove();

        return minecraftServer.getSynchronizedServer()
            .writeExp(server -> server.getLootManager().getTable(identifier).generateLoot(context));
    }

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
