package org.boosted.mixin.getServer;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.GiveGiftsToHeroTask;
import net.minecraft.entity.ai.brain.task.GoToWorkTask;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;
import java.util.Optional;

@Mixin(GoToWorkTask.class)
public class GoToWorkTaskMixin {
    /**
     * @author Vorlent
     * @reason this function needs exclusive write access to MinecraftServer
     */
    @Overwrite
    protected void run(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        GlobalPos globalPos = villagerEntity.getBrain().getOptionalMemory(MemoryModuleType.POTENTIAL_JOB_SITE).get();
        villagerEntity.getBrain().forget(MemoryModuleType.POTENTIAL_JOB_SITE);
        villagerEntity.getBrain().remember(MemoryModuleType.JOB_SITE, globalPos);
        serverWorld.sendEntityStatus(villagerEntity, EntityStatuses.ADD_VILLAGER_HAPPY_PARTICLES);
        if (villagerEntity.getVillagerData().getProfession() != VillagerProfession.NONE) {
            return;
        }
        /* PATCH BEGIN */
        serverWorld.getSynchronizedServer().write(minecraftServer -> {
            // TODO warning, minecraftServer.getWorld() is used to obtain a cross reference to another world
            Optional.ofNullable(minecraftServer.getWorld(globalPos.getDimension()))
                    .flatMap(world -> world.getPointOfInterestStorage().getType(globalPos.getPos()))
                    .flatMap(registryEntry -> Registry.VILLAGER_PROFESSION.stream()
                            .filter(profession -> profession.heldWorkstation().test((RegistryEntry<PointOfInterestType>)registryEntry))
                            .findFirst()).ifPresent(profession -> {
                        villagerEntity.setVillagerData(villagerEntity.getVillagerData().withProfession((VillagerProfession)profession));
                        villagerEntity.reinitializeBrain(serverWorld);
                    });
        });
        /* PATCH END */

    }
}
