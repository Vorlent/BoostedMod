package org.boosted.mixin.serverplayerentity;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AdvancementRewards.class)
public class AdvancementRewardsMixin {

    @Redirect(method = "apply",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getLootManager()Lnet/minecraft/loot/LootManager;"))
    private LootManager redirect$apply$getLootManager(MinecraftServer instance) {
        return null;
    }

    @Redirect(method = "apply",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/LootManager;getTable(Lnet/minecraft/util/Identifier;)Lnet/minecraft/loot/LootTable;"))
    private LootTable redirect$apply$getTable(LootManager instance, Identifier id) {
        return null;
    }

    @Redirect(method = "apply",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/LootTable;generateLoot(Lnet/minecraft/loot/context/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"))
    private ObjectArrayList<ItemStack> redirect$apply$generateLoot(LootTable instance, LootContext context) {
        return this.player.server.getSynchronizedServer()
            .write(server -> server.get);
    }
}
