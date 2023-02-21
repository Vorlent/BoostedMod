package org.boosted.mixin.getServer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin {

    @Redirect(method = "use(Lnet/minecraft/item/ItemStack;)I",
        at = @At(value = "INVOKE", target = "net/minecraft/world/World.getServer ()Lnet/minecraft/server/MinecraftServer;"))
    public MinecraftServer skipGetServer(World instance) {
        return null;
    }

    @Redirect(method = "use(Lnet/minecraft/item/ItemStack;)I",
        at = @At(value = "INVOKE", target = "net/minecraft/server/MinecraftServer.getLootManager ()Lnet/minecraft/loot/LootManager;"))
    public LootManager skipGetLootManager(MinecraftServer instance) {
        return null;
    }

    @Redirect(method = "use(Lnet/minecraft/item/ItemStack;)I",
        at = @At(value = "INVOKE", target = "net/minecraft/loot/LootManager.getTable (Lnet/minecraft/util/Identifier;)Lnet/minecraft/loot/LootTable;"))
    public LootTable skipGetTable(LootManager instance, Identifier id) {
        return null;
    }

    @Redirect(method = "use(Lnet/minecraft/item/ItemStack;)I",
        at = @At(value = "INVOKE", target = "net/minecraft/loot/LootTable.generateLoot (Lnet/minecraft/loot/context/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"))
    public ObjectArrayList<ItemStack> redirectGenerateLoot(LootTable instance, LootContext context) {
        FishingBobberEntity entity = (FishingBobberEntity)(Object)this;
        ServerWorld serverWorld = (ServerWorld) entity.world;
        return serverWorld.getSynchronizedServer().readExp(server -> {
            LootTable lootTable = server.getLootManager().getTable(LootTables.FISHING_GAMEPLAY);
            return lootTable.generateLoot(context);
        });
    }
}
