package org.boosted.mixin.getServer;

import com.google.common.collect.Sets;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(LootContext.Builder.class)
public abstract class LootContextBuilderMixin {

    @Shadow @Final public Map<Identifier, LootContext.Dropper> drops;
    @Shadow @Final public Map<LootContextParameter<?>, Object> parameters;
    @Shadow public float luck;
    @Shadow public Random random;
    @Shadow public abstract ServerWorld getWorld();

    @Inject(method = "build(Lnet/minecraft/loot/context/LootContextType;)Lnet/minecraft/loot/context/LootContext;", cancellable = true,
     at = @At(value = "FIELD", target = "net/minecraft/loot/context/LootContext$Builder.world : Lnet/minecraft/server/world/ServerWorld;", ordinal = 0))
    public void build(LootContextType type, CallbackInfoReturnable<LootContext> cir) {
        Random random = this.random == null ? Random.create() : this.random;
        LootContext returnValue = getWorld().getSynchronizedServer().readExp(minecraftServer -> {
            // this is indirectly leaking references to getServer
            // by passing in getLootManager() and getPredicateManager()
            // which is then resynchronized in LootContextMixin
            LootContext lootContext = new LootContext(random, this.luck, this.getWorld(),
                minecraftServer.getLootManager()::getTable, minecraftServer.getPredicateManager()::get,
                this.parameters, this.drops);
            return lootContext;
        });
        cir.setReturnValue(returnValue);
    }

    /*
        public LootTable getTable(Identifier id) {
        return this.tableGetter.apply(id);
    }

    @Nullable
    public LootCondition getCondition(Identifier id) {
        return this.conditionGetter.apply(id);
    }
     */
}
