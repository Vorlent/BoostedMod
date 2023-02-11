package org.boosted.mixin.getServer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Optional;
import java.util.function.BiPredicate;

import static net.minecraft.entity.passive.VillagerEntity.POINTS_OF_INTEREST;

@Mixin(VillagerEntity.class)
public class VillagerEntityMixin {
    /**
     * @author Vorlent
     * @reason this method needs exclusive write access to MinecraftServer
     */
    @Overwrite
    public void releaseTicketFor(MemoryModuleType<GlobalPos> memoryModuleType) {
        VillagerEntity entity = (VillagerEntity)(Object)this;

        if (!(entity.world instanceof ServerWorld)) {
            return;
        }
        /* PATCH BEGIN */
        ((ServerWorld)entity.world).getSynchronizedServer().write(minecraftServer -> {
            entity.getBrain().getOptionalMemory(memoryModuleType).ifPresent(pos -> {
                ServerWorld serverWorld = minecraftServer.getWorld(pos.getDimension());
                if (serverWorld == null) {
                    return;
                }
                PointOfInterestStorage pointOfInterestStorage = serverWorld.getPointOfInterestStorage();
                Optional<RegistryEntry<PointOfInterestType>> optional = pointOfInterestStorage.getType(pos.getPos());
                BiPredicate<VillagerEntity, RegistryEntry<PointOfInterestType>> biPredicate = POINTS_OF_INTEREST.get(memoryModuleType);
                if (optional.isPresent() && biPredicate.test(entity, optional.get())) {
                    pointOfInterestStorage.releaseTicket(pos.getPos());
                    DebugInfoSender.sendPointOfInterest(serverWorld, pos.getPos());
                }
            });
        });
        /* PATCH BEGIN */
    }
}
