package org.boosted.parallelized;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Npc;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.map.MapState;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.BlockEvent;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.tag.TagKey;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.*;
import net.minecraft.world.GameRules;
import net.minecraft.world.IdCountsState;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;
import net.minecraft.world.spawner.Spawner;
import org.boosted.SynchronizedServerGetter;
import org.boosted.mixin.getServer.SetServerAccessor;
import org.boosted.unmodifiable.UnmodifiableMinecraftServer;
import org.boosted.util.SynchronizedResource;
import org.boosted.util.UnsynchronizedResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;

public class ParallelServerWorld extends ServerWorld implements SynchronizedServerGetter {
    private final SynchronizedResource<MinecraftServer, UnmodifiableMinecraftServer> synchronizedServer;
    private final MinecraftServer unsynchronizedServer; // try to never use this!

    public ParallelServerWorld(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey<World> worldKey, DimensionOptions dimensionOptions, WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List<Spawner> spawners, boolean shouldTickTime) {
        super(server, workerExecutor, session, properties, worldKey, dimensionOptions, worldGenerationProgressListener, debugWorld, seed, spawners, shouldTickTime);

        unsynchronizedServer = server;
        ((SetServerAccessor) this).setServer(null);
        synchronizedServer = server.getSynchronizedServer();
    }

    /**
     * Synchronized by WeatherTimeBarrier
     */
    @Override
    protected void tickTime() {
        super.tickTime();
    }

    @Override
    public boolean shouldCancelSpawn(Entity entity) {
        if ((entity instanceof AnimalEntity || entity instanceof WaterCreatureEntity)
            && getSynchronizedServer().readExp(server -> !server.shouldSpawnAnimals())) {
            return true;
        }
        return entity instanceof Npc && getSynchronizedServer().readExp(server -> !server.shouldSpawnNpcs());
    }

    @Override
    public ServerScoreboard getScoreboard() {
        //throw new UnsupportedOperationException();
        return this.getUnsynchronizedServer().getScoreboard();
    }

    /**
     * Synchronized by WeatherTimeBarrier
     */
    /*@Override
    private void tickWeather() {
        super.tickWeather();
    }*/

    @Override
    public boolean canPlayerModifyAt(PlayerEntity player, BlockPos pos) {
        return this.getWorldBorder().contains(pos)
               && getSynchronizedServer().readExp(server -> !server.isSpawnProtected(this, pos, player));
    }

    @Override
    public void saveLevel() {
        getSynchronizedServer().write(server -> {
            if (this.getEnderDragonFight() != null) {
                server.getSaveProperties().setDragonFight(this.getEnderDragonFight().toNbt());
            }
        });
        this.getChunkManager().getPersistentStateManager().save();
    }

    @Override
    public void setBlockBreakingInfo(int entityId, BlockPos pos, int progress) {
        getSynchronizedServer().write(server -> {
            for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
                double f;
                double e;
                double d;
                if (serverPlayerEntity == null || serverPlayerEntity.world != this || serverPlayerEntity.getId() == entityId || !((d = (double)pos.getX() - serverPlayerEntity.getX()) * d + (e = (double)pos.getY() - serverPlayerEntity.getY()) * e + (f = (double)pos.getZ() - serverPlayerEntity.getZ()) * f < 1024.0)) continue;
                serverPlayerEntity.networkHandler.sendPacket(new BlockBreakingProgressS2CPacket(entityId, pos, progress));
            }
        });
    }

    @Override
    public void playSound(@Nullable PlayerEntity except, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch, long seed) {
        getSynchronizedServer().write(server -> {
            server.getPlayerManager().sendToAround(except, x, y, z, sound.getDistanceToTravel(volume), this.getRegistryKey(), new PlaySoundS2CPacket(sound, category, x, y, z, volume, pitch, seed));
        });
    }

    @Override
    public void playSoundFromEntity(@Nullable PlayerEntity except, Entity entity, SoundEvent sound, SoundCategory category, float volume, float pitch, long seed) {
        getSynchronizedServer().write(server -> {
            server.getPlayerManager().sendToAround(except, entity.getX(), entity.getY(), entity.getZ(), sound.getDistanceToTravel(volume), this.getRegistryKey(), new PlaySoundFromEntityS2CPacket(sound, category, entity, volume, pitch, seed));
        });
    }

    @Override
    public void syncGlobalEvent(int eventId, BlockPos pos, int data) {
        getSynchronizedServer().write(server -> {
            server.getPlayerManager().sendToAll(new WorldEventS2CPacket(eventId, pos, data, true));
        });
    }

    @Override
    public void syncWorldEvent(@Nullable PlayerEntity player, int eventId, BlockPos pos, int data) {
        getSynchronizedServer().write(server -> {
            server.getPlayerManager().sendToAround(player, pos.getX(), pos.getY(), pos.getZ(), 64.0, this.getRegistryKey(), new WorldEventS2CPacket(eventId, pos, data, false));
        });
    }

    @Override
    public void processSyncedBlockEvents() {
        this.blockEventQueue.clear();
        while (!this.syncedBlockEventQueue.isEmpty()) {
            BlockEvent blockEvent = this.syncedBlockEventQueue.removeFirst();
            if (this.shouldTickBlockPos(blockEvent.pos())) {
                if (!this.processBlockEvent(blockEvent)) continue;
                getSynchronizedServer().write(server -> {
                    server.getPlayerManager().sendToAround(null, blockEvent.pos().getX(), blockEvent.pos().getY(), blockEvent.pos().getZ(), 64.0, this.getRegistryKey(), new BlockEventS2CPacket(blockEvent.pos(), blockEvent.block(), blockEvent.type(), blockEvent.data()));
                });
                continue;
            }
            this.blockEventQueue.add(blockEvent);
        }
        this.syncedBlockEventQueue.addAll((Collection<BlockEvent>)this.blockEventQueue);
    }

    @Override
    @NotNull
    public MinecraftServer getServer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SynchronizedResource<MinecraftServer, UnmodifiableMinecraftServer> getSynchronizedServer() {
        if (synchronizedServer == null) {
            return new UnsynchronizedResource<>(getUnsynchronizedServer(),
                    new UnmodifiableMinecraftServer(getUnsynchronizedServer()));
        }
        return synchronizedServer;
    }

    public MinecraftServer getUnsynchronizedServer() {
        if (unsynchronizedServer == null && ((SetServerAccessor) this).getServerField() != null) {
            return ((SetServerAccessor) this).getServerField();
        }
        return unsynchronizedServer;
    }

    @Override
    public StructureTemplateManager getStructureTemplateManager() {
        throw new UnsupportedOperationException();
        //return this.server.getStructureTemplateManager();
    }

    @Override
    @Nullable
    public BlockPos locateStructure(TagKey<Structure> structureTag, BlockPos pos, int radius, boolean skipReferencedStructures) {
        if (getSynchronizedServer().readExp(server -> !server.getSaveProperties().getGeneratorOptions().shouldGenerateStructures())) {
            return null;
        }
        Optional<RegistryEntryList.Named<Structure>> optional = this.getRegistryManager().get(Registry.STRUCTURE_KEY).getEntryList(structureTag);
        if (optional.isEmpty()) {
            return null;
        }
        Pair<BlockPos, RegistryEntry<Structure>> pair = this.getChunkManager().getChunkGenerator().locateStructure(this, (RegistryEntryList<Structure>)optional.get(), pos, radius, skipReferencedStructures);
        return pair != null ? pair.getFirst() : null;
    }

    @Override
    public RecipeManager getRecipeManager() {
        //throw new UnsupportedOperationException();
        return this.getUnsynchronizedServer().getRecipeManager();
    }

    @Override
    public DynamicRegistryManager getRegistryManager() {
        //throw new UnsupportedOperationException();
        return this.getUnsynchronizedServer().getRegistryManager();
    }

    @Override
    public boolean isFlat() {
        return getSynchronizedServer().readExp(server -> server.getSaveProperties().getGeneratorOptions().isFlatWorld());
    }

    @Override
    public long getSeed() {
        return getSynchronizedServer().readExp(server -> server.getSaveProperties().getGeneratorOptions().getSeed());
    }

    @Override
    public void sendSleepingStatus() {
        if (!this.isSleepingEnabled()) {
            return;
        }
        if (getSynchronizedServer().readExp(server -> server.isSingleplayer() && server.isRemote())) {
            return;
        }
        int i = this.getGameRules().getInt(GameRules.PLAYERS_SLEEPING_PERCENTAGE);
        MutableText text = this.sleepManager.canSkipNight(i) ? Text.translatable("sleep.skipping_night") : Text.translatable("sleep.players_sleeping", this.sleepManager.getSleeping(), this.sleepManager.getNightSkippingRequirement(i));
        for (ServerPlayerEntity serverPlayerEntity : this.getPlayers()) {
            serverPlayerEntity.sendMessage(text, true);
        }
    }


    @Override
    @Nullable
    public MapState getMapState(String id) {
        // TODO verify if this can be read only
        return getSynchronizedServer().readExp(server ->
            server.getOverworld().getPersistentStateManager().get(MapState::fromNbt, id)
        );
    }

    @Override
    public void putMapState(String id, MapState state) {
        getSynchronizedServer().write(server ->
            server.getOverworld().getPersistentStateManager().set(id, state)
        );
    }

    @Override
    public int getNextMapId() {
        return getSynchronizedServer().readExp(server ->
            server.getOverworld().getPersistentStateManager().getOrCreate(IdCountsState::fromNbt, IdCountsState::new, "idcounts").getNextMapId()
        );
    }

    @Override
    public void setSpawnPos(BlockPos pos, float angle) {
        ChunkPos chunkPos = new ChunkPos(new BlockPos(this.properties.getSpawnX(), 0, this.properties.getSpawnZ()));
        this.properties.setSpawnPos(pos, angle);
        this.getChunkManager().removeTicket(ChunkTicketType.START, chunkPos, 11, Unit.INSTANCE);
        this.getChunkManager().addTicket(ChunkTicketType.START, new ChunkPos(pos), 11, Unit.INSTANCE);
        getSynchronizedServer().write(server ->
            server.getPlayerManager().sendToAll(new PlayerSpawnPositionS2CPacket(pos, angle))
        );
    }

    @Override
    public void onBlockChanged(BlockPos pos, BlockState oldBlock, BlockState newBlock) {
        Optional<RegistryEntry<PointOfInterestType>> optional2;
        Optional<RegistryEntry<PointOfInterestType>> optional = PointOfInterestTypes.getTypeForState(oldBlock);
        if (Objects.equals(optional, optional2 = PointOfInterestTypes.getTypeForState(newBlock))) {
            return;
        }
        getSynchronizedServer().write(server -> {
            BlockPos blockPos = pos.toImmutable();
            optional.ifPresent(registryEntry -> server.execute(() -> {
                this.getPointOfInterestStorage().remove(blockPos);
                DebugInfoSender.sendPoiRemoval(this, blockPos);
            }));
            optional2.ifPresent(registryEntry -> server.execute(() -> {
                this.getPointOfInterestStorage().add(blockPos, (RegistryEntry<PointOfInterestType>)registryEntry);
                DebugInfoSender.sendPoiAddition(this, blockPos);
            }));
        });
    }

    @Override
    public void cacheStructures(Chunk chunk) {
        this.getUnsynchronizedServer()
            .execute(() -> this.structureLocator.cache(chunk.getPos(), chunk.getStructureStarts()));
    }
}
