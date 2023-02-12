package org.boosted.mixin.getServer;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LootableContainerBlockEntity.class)
public class LootableContainerBlockEntityMixin {

    @Shadow @Nullable public Identifier lootTableId;

    @Shadow public long lootTableSeed;

    /**
     * @author Vorlent
     * @reason We need to wrap the entire method, because all of it depends on exclusive access on MinecraftServer
     */
    @Overwrite
    public void checkLootInteraction(@Nullable PlayerEntity player) {
        LootableContainerBlockEntity entity = (LootableContainerBlockEntity)(Object)this;
        World world = entity.getWorld();
        if (this.lootTableId != null && world instanceof ServerWorld serverWorld) {
            serverWorld.getSynchronizedServer().write(server -> {
                LootTable lootTable = server.getLootManager().getTable(this.lootTableId);
                if (player instanceof ServerPlayerEntity) {
                    Criteria.PLAYER_GENERATES_CONTAINER_LOOT.trigger((ServerPlayerEntity)player, this.lootTableId);
                }
                this.lootTableId = null;
                LootContext.Builder builder = new LootContext.Builder((ServerWorld) world)
                    .parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(entity.getPos()))
                        .random(this.lootTableSeed);
                if (player != null) {
                    builder.luck(player.getLuck()).parameter(LootContextParameters.THIS_ENTITY, player);
                }
                lootTable.supplyInventory(entity, builder.build(LootContextTypes.CHEST));
            });
        }
    }
}
