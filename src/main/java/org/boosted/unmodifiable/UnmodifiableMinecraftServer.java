package org.boosted.unmodifiable;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import net.minecraft.command.DataCommandStorage;
import net.minecraft.entity.boss.BossBarManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.condition.LootConditionManager;
import net.minecraft.loot.function.LootFunctionManager;
import net.minecraft.network.encryption.SignatureVerifier;
import net.minecraft.network.message.MessageDecorator;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.*;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.filter.TextStream;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.MetricsData;
import net.minecraft.util.ModStatus;
import net.minecraft.util.UserCache;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.security.KeyPair;
import java.util.Optional;
import java.util.Set;

/**
 * This UnmodifiableMinecraftServer is supposed to be a stepping stone,
 * so that ServerWorld.getServer() can be replaced by ServerWorld.getSynchronizedServer() or a similar method.
 * The method returns a synchronized wrapper of the MinecraftServer which must contain
 * both a modifiable and an unmodifiable MinecraftServer. This is because the synchronized wrapper
 * grants access to multiple readers simultaneously when starting a read "transaction".
 *
 * So this means all server worlds can access the MinecraftServer concurrently.
 * When one one ServerWorld has to write it will end up blocking all readers but this is still better
 * than delaying everything to the post tick phase which is sequential in nature.
 *
 * This class does not yet provide unmodifiable instances to referenced objects like the ScoreBoard. (TODO)
 *
 * When attempting to write, the class will throw an exception and the caller must fix their code. (e.g. acquire the write lock)
 */
public class UnmodifiableMinecraftServer {

    private final MinecraftServer server;

    public UnmodifiableMinecraftServer(MinecraftServer server) {
        this.server = server;
    }

    // GameMode is immutable
    public GameMode getDefaultGameMode() {
        return server.getDefaultGameMode();
    }

    public boolean isHardcore() {
        return server.isHardcore();
    }

    public int getOpPermissionLevel() {
        return server.getOpPermissionLevel();
    }

    public int getFunctionPermissionLevel() {
        return server.getFunctionPermissionLevel();
    }

    public boolean shouldBroadcastRconToOps() {
        return server.shouldBroadcastRconToOps();
    }

    public String getServerIp() {
        return server.getServerIp();
    }

    public boolean isRunning() {
        return server.isRunning();
    }

    public boolean canExecute(ServerTask serverTask) {
        return server.canExecute(serverTask);
    }

    public Optional<Path> getIconFile() {
        return server.getIconFile();
    }

    public File getRunDirectory() {
        return server.getRunDirectory();
    }

    public boolean isNetherAllowed() {
        return server.isNetherAllowed();
    }

    public boolean isStopping() {
        return server.isStopping();
    }

    public File getFile(String path) {
        return server.getFile(path);
    }

    //TODO consider unmodifiable server world
    public ServerWorld getOverworld() {
        return server.getOverworld();
    }

    //TODO consider unmodifiable server world
    public ServerWorld getWorld(RegistryKey<World> key) {
        return server.getWorld(key);
    }

    public Set<RegistryKey<World>> getWorldRegistryKeys() {
        return server.getWorldRegistryKeys();
    }

    // consider unmodifiable server world
    /*public Iterable<ServerWorld> getWorlds() {
        return server.getWorlds();
    }*/

    public String getVersion() {
        return server.getVersion();
    }

    public int getCurrentPlayerCount() {
        return server.getCurrentPlayerCount();
    }

    public int getMaxPlayerCount() {
        return server.getMaxPlayerCount();
    }

    public String[] getPlayerNames() {
        return server.getPlayerNames();
    }

    public String getServerModName() {
        return server.getServerModName();
    }

    public ModStatus getModStatus() {
        return server.getModStatus();
    }

    public KeyPair getKeyPair() {
        return server.getKeyPair();
    }

    public int getServerPort() {
        return server.getServerPort();
    }

    // game profile is unmodifiable
    public GameProfile getHostProfile() {
        return server.getHostProfile();
    }

    public boolean isSingleplayer() {
        return server.isSingleplayer();
    }

    public boolean isMonsterSpawningEnabled() {
        return server.isMonsterSpawningEnabled();
    }

    public boolean isDemo() {
        return server.isDemo();
    }

    // consider unmodifiable ServerResourcePackProperties
    /*public Optional<net.minecraft.server.MinecraftServer.ServerResourcePackProperties> getResourcePackProperties() {
        return server.getResourcePackProperties();
    }*/

    public boolean requireResourcePack() {
        return server.requireResourcePack();
    }

    public boolean isDedicated() {
        return server.isDedicated();
    }

    public int getRateLimit() {
        return server.getRateLimit();
    }

    public boolean isOnlineMode() {
        return server.isOnlineMode();
    }

    public boolean shouldPreventProxyConnections() {
        return server.shouldPreventProxyConnections();
    }

    public boolean shouldSpawnAnimals() {
        return server.shouldSpawnAnimals();
    }

    public boolean shouldSpawnNpcs() {
        return server.shouldSpawnNpcs();
    }

    public boolean isUsingNativeTransport() {
        return server.isUsingNativeTransport();
    }

    public boolean isPvpEnabled() {
        return server.isPvpEnabled();
    }

    public boolean isFlightEnabled() {
        return server.isFlightEnabled();
    }


    public boolean areCommandBlocksEnabled() {
        return server.areCommandBlocksEnabled();
    }

    public String getServerMotd() {
        return server.getServerMotd();
    }

    public boolean shouldPreviewChat() {
        return server.shouldPreviewChat();
    }

    public boolean isStopped() {
        return server.isStopped();
    }

    //TODO consider unmodifiable PlayerManager
    public PlayerManager getPlayerManager() {
        return server.getPlayerManager();
    }

    public boolean isRemote() {
        return server.isRemote();
    }

    // consider unmodifiable ServerNetworkIo
    /*public ServerNetworkIo getNetworkIo() {
        return server.getNetworkIo();
    }*/

    public boolean isLoading() {
        return server.isLoading();
    }

    public boolean hasGui() {
        return server.hasGui();
    }

    public int getTicks() {
        return server.getTicks();
    }

    public int getSpawnProtectionRadius() {
        return server.getSpawnProtectionRadius();
    }

    public boolean isSpawnProtected(ServerWorld world, BlockPos pos, PlayerEntity player) {
        return server.isSpawnProtected(world, pos, player);
    }

    public boolean acceptsStatusQuery() {
        return server.acceptsStatusQuery();
    }

    public boolean hideOnlinePlayers() {
        return server.hideOnlinePlayers();
    }

    // never used
    // public Proxy getProxy() { return server.getProxy(); }

    public int getPlayerIdleTimeout() {
        return server.getPlayerIdleTimeout();
    }

    // consider unmodifiable MinecraftSessionService
    /*public MinecraftSessionService getSessionService() {
        return server.getSessionService();
    }*/

    // consider unmodifiable SignatureVerifier
    /*public SignatureVerifier getServicesSignatureVerifier() {
        return server.getServicesSignatureVerifier();
    }*/

    // consider unmodifiable GameProfileRepository
    /*public GameProfileRepository getGameProfileRepo() {
        return server.getGameProfileRepo();
    }*/

    // consider unmodifiable UserCache
    /*public UserCache getUserCache() {
        return server.getUserCache();
    }*/

    // consider unmodifiable ServerMetadata
    /*public ServerMetadata getServerMetadata() {
        return server.getServerMetadata();
    }*/

    public int getMaxWorldBorderRadius() {
        return server.getMaxWorldBorderRadius();
    }

    public boolean shouldExecuteAsync() {
        return server.shouldExecuteAsync();
    }

    public Thread getThread() {
        return server.getThread();
    }

    public int getNetworkCompressionThreshold() {
        return server.getNetworkCompressionThreshold();
    }

    public boolean shouldEnforceSecureProfile() {
        return server.shouldEnforceSecureProfile();
    }

    public long getTimeReference() {
        return server.getTimeReference();
    }

    // consider unmodifiable DataFixer
    /*public DataFixer getDataFixer() {
        return server.getDataFixer();
    }*/

    public int getSpawnRadius(@Nullable ServerWorld world) {
        return server.getSpawnRadius(world);
    }

    //TODO consider unmodifiable ServerAdvancementLoader
    public ServerAdvancementLoader getAdvancementLoader() {
        return server.getAdvancementLoader();
    }

    //consider unmodifiable CommandFunctionManager
    /*public CommandFunctionManager getCommandFunctionManager() {
        return server.getCommandFunctionManager();
    }*/

    // consider unmodifiable ResourcePackManager
    /*public ResourcePackManager getDataPackManager() {
        return server.getDataPackManager();
    }*/

    //consider unmodifiable CommandManager
    /*public CommandManager getCommandManager() {
        return server.getCommandManager();
    }*/

    // consider unmodifiable ServerCommandSource
    /*public ServerCommandSource getCommandSource() {
        return server.getCommandSource();
    }*/

    public boolean shouldReceiveFeedback() {
        return server.shouldReceiveFeedback();
    }

    public boolean shouldTrackOutput() {
        return server.shouldTrackOutput();
    }

    public boolean shouldBroadcastConsoleToOps() {
        return server.shouldBroadcastConsoleToOps();
    }

    //TODO consider unmodifiable RecipeManager
    public RecipeManager getRecipeManager() {
        return server.getRecipeManager();
    }

    //TODO consider unmodifiable ServerScoreboard
    public ServerScoreboard getScoreboard() {
        return server.getScoreboard();
    }

    //TODO consider unmodifiable DataCommandStorage
    public DataCommandStorage getDataCommandStorage() {
        return server.getDataCommandStorage();
    }

    //TODO consider unmodifiable LootManager
    public LootManager getLootManager() {
        return server.getLootManager();
    }

    //TODO consider unmodifiable LootConditionManager
    public LootConditionManager getPredicateManager() {
        return server.getPredicateManager();
    }

    //consider unmodifiable LootFunctionManager
    /*public LootFunctionManager getItemModifierManager() {
        return server.getItemModifierManager();
    }*/

    // consider unmodifiable GameRules
    /*public GameRules getGameRules() {
        return server.getGameRules();
    }*/

    //consider unmodifiable BossBarManager
    /*public BossBarManager getBossBarManager() {
        return server.getBossBarManager();
    }*/

    public boolean isEnforceWhitelist() {
        return server.isEnforceWhitelist();
    }

    public float getTickTime() {
        return server.getTickTime();
    }

    public int getPermissionLevel(GameProfile profile) {
        return server.getPermissionLevel(profile);
    }

    //consider unmodifiable MetricsData
    /*public MetricsData getMetricsData() {
        return server.getMetricsData();
    }*/

    // consider unmodifiable Profiler
    /*public Profiler getProfiler() {
        return server.getProfiler();
    }*/

    public boolean isHost(GameProfile profile) {
        return server.isHost(profile);
    }

    public boolean isRecorderActive() {
        return server.isRecorderActive();
    }

    public Path getSavePath(WorldSavePath worldSavePath) {
        return server.getSavePath(worldSavePath);
    }

    public boolean syncChunkWrites() {
        return server.syncChunkWrites();
    }

    //TODO consider unmodifiable StructureTemplateManager
    public StructureTemplateManager getStructureTemplateManager() {
        return server.getStructureTemplateManager();
    }

    // consider unmodifiable SaveProperties
    /*public SaveProperties getSaveProperties() {
        return server.getSaveProperties();
    }*/

    public DynamicRegistryManager.Immutable getRegistryManager() {
        return server.getRegistryManager();
    }

    // TextStream is immutable
    public TextStream createFilterer(ServerPlayerEntity player) {
        return server.createFilterer(player);
    }

    // consider unmodifiable ServerPlayerInteractionManager
    /*public ServerPlayerInteractionManager getPlayerInteractionManager(ServerPlayerEntity player) {
        return server.getPlayerInteractionManager(player);
    }*/

    // GameMode is immutable
    public GameMode getForcedGameMode() {
        return server.getForcedGameMode();
    }

    //consider unmodifiable ResourceManager
    /*public ResourceManager getResourceManager() {
        return server.getResourceManager();
    }*/

    public boolean isSaving() {
        return server.isSaving();
    }

    public boolean isDebugRunning() {
        return server.isDebugRunning();
    }

    public int getMaxChainedNeighborUpdates() {
        return server.getMaxChainedNeighborUpdates();
    }

    // MessageDecorator is immutable
    public MessageDecorator getMessageDecorator() {
        return server.getMessageDecorator();
    }
}

