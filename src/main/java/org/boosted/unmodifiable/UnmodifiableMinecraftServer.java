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
import net.minecraft.network.message.MessageType;
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
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.ProfileResult;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.*;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.nio.file.Path;
import java.security.KeyPair;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

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
public class UnmodifiableMinecraftServer extends MinecraftServer {

    private static final SaveProperties DUMMY_SAVE_PROPERTIES = new DummySaveProperties();
    private static final ApiServices DUMMY_API_SERVICES = new ApiServices(null, null, null, null);
    private static final SaveLoader DUMMY_SAVELOADER = new SaveLoader(null, null, null, DUMMY_SAVE_PROPERTIES);

    private final MinecraftServer server;

    public UnmodifiableMinecraftServer(MinecraftServer server) {
        super(null, new UnmodifiableLevelStorage().createSession2("DUMMY"), null, DUMMY_SAVELOADER, null,
                null, DUMMY_API_SERVICES, null);
        this.server = server;

        // TODO use reflection to delete state
        //this.networkIo = null;
        //this.commandFunctionManager = null;
        //this.structureTemplateManager = null;
        //this.workerExecutor = null;
    }

    /*
        public MinecraftServer(Thread serverThread, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, Proxy proxy, DataFixer dataFixer, ApiServices apiServices, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory) {
        super("Server");
        this.registryManager = saveLoader.dynamicRegistryManager();
        this.saveProperties = saveLoader.saveProperties();
        if (!this.saveProperties.getGeneratorOptions().getDimensions().contains(DimensionOptions.OVERWORLD)) {
            throw new IllegalStateException("Missing Overworld dimension data");
        }
        this.resourceManagerHolder = new ResourceManagerHolder(saveLoader.resourceManager(), saveLoader.dataPackContents());
        this.apiServices = apiServices;
        if (apiServices.userCache() != null) {
            apiServices.userCache().setExecutor(this);
        }
        this.networkIo = new ServerNetworkIo(this);
        this.worldGenerationProgressListenerFactory = worldGenerationProgressListenerFactory;
        this.session = session;
        this.saveHandler = session.createSaveHandler();
        this.dataFixer = dataFixer;
        this.commandFunctionManager = new CommandFunctionManager(this, this.resourceManagerHolder.dataPackContents.getFunctionLoader());
        this.structureTemplateManager = new StructureTemplateManager(saveLoader.resourceManager(), session, dataFixer);
        this.serverThread = serverThread;
        this.workerExecutor = Util.getMainWorkerExecutor();
    }
     */

    @Override
    protected boolean setupServer() throws IOException {
        throw readOnlyException();
    }

    @Override
    protected void loadWorld() {
        throw readOnlyException();
    }

    @Override
    protected void updateDifficulty() {
        throw readOnlyException();
    }

    @Override
    protected void createWorlds(WorldGenerationProgressListener worldGenerationProgressListener) {
        throw readOnlyException();
    }

    //TODO consider unmodifiable game mode
    public GameMode getDefaultGameMode() {
        return this.saveProperties.getGameMode();
    }

    public boolean isHardcore() {
        return this.saveProperties.isHardcore();
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

    @Override
    public boolean save(boolean suppressLogs, boolean flush, boolean force) {
        throw readOnlyException();
    }

    @Override
    public boolean saveAll(boolean suppressLogs, boolean flush, boolean force) {
        throw readOnlyException();
    }

    @Override
    public void close() {
        throw readOnlyException();
    }

    @Override
    public void shutdown() {
       server.shutdown();
    }

    @Override
    public String getServerIp() {
        return server.getServerIp();
    }

    private UnsupportedOperationException readOnlyException() {
        return new UnsupportedOperationException("Server instance " + server + " is not allowed to be modified");
    }

    @Override
    public void setServerIp(String serverIp) {
        throw readOnlyException();
    }

    @Override
    public boolean isRunning() {
        return server.isRunning();
    }

    @Override
    public void stop(boolean bl) {
        throw readOnlyException();
    }

    @Override
    protected void runServer() {
        throw readOnlyException();
    }

    @Override
    protected void runTasksTillTickEnd() {
        throw readOnlyException();
    }

    @Override
    protected ServerTask createTask(Runnable runnable) {
        throw readOnlyException();
    }

    @Override
    public boolean canExecute(ServerTask serverTask) {
        return server.canExecute(serverTask);
    }

    @Override
    public boolean runTask() {
        throw readOnlyException();
    }

    @Override
    public void executeTask(ServerTask serverTask) {
        throw readOnlyException();
    }

    @Override
    public Optional<Path> getIconFile() {
        return server.getIconFile();
    }

    @Override
    public File getRunDirectory() {
        return server.getRunDirectory();
    }

    @Override
    public void setCrashReport(CrashReport report) {
        throw readOnlyException();
    }

    @Override
    public void exit() {
        throw readOnlyException();
    }

    @Override
    public void tick(BooleanSupplier shouldKeepTicking) {
        throw readOnlyException();
    }

    @Override
    public void tickWorlds(BooleanSupplier shouldKeepTicking) {
        throw readOnlyException();
    }

    @Override
    public boolean isNetherAllowed() {
        return server.isNetherAllowed();
    }

    @Override
    public void addServerGuiTickable(Runnable tickable) {
        throw readOnlyException();
    }

    @Override
    protected void setServerId(String serverId) {
        throw readOnlyException();
    }

    @Override
    public boolean isStopping() {
        return server.isStopping();
    }

    @Override
    public File getFile(String path) {
        return server.getFile(path);
    }

    //TODO consider unmodifiable server world
    @Override
    public ServerWorld getOverworld() {
        return server.getOverworld();
    }

    //TODO consider unmodifiable server world
    @Override
    public ServerWorld getWorld(RegistryKey<World> key) {
        return server.getWorld(key);
    }

    @Override
    public Set<RegistryKey<World>> getWorldRegistryKeys() {
        return server.getWorldRegistryKeys();
    }

    //TODO consider unmodifiable server world
    @Override
    public Iterable<ServerWorld> getWorlds() {
        return server.getWorlds();
    }

    @Override
    public String getVersion() {
        return server.getVersion();
    }

    @Override
    public int getCurrentPlayerCount() {
        return server.getCurrentPlayerCount();
    }

    @Override
    public int getMaxPlayerCount() {
        return server.getMaxPlayerCount();
    }

    @Override
    public String[] getPlayerNames() {
        return server.getPlayerNames();
    }

    @Override
    public String getServerModName() {
        return server.getServerModName();
    }

    @Override
    public SystemDetails addSystemDetails(SystemDetails details) {
        throw readOnlyException();
    }

    @Override
    public SystemDetails addExtraSystemDetails(SystemDetails details) {
        throw readOnlyException();
    }

    @Override
    public ModStatus getModStatus() {
        return server.getModStatus();
    }

    @Override
    public void sendMessage(Text message) {
        throw readOnlyException();
    }

    @Override
    public KeyPair getKeyPair() {
        return server.getKeyPair();
    }

    @Override
    public int getServerPort() {
        return server.getServerPort();
    }

    @Override
    public void setServerPort(int serverPort) {
        throw readOnlyException();
    }

    //TODO consider unmodifiable game profile
    @Override
    public GameProfile getHostProfile() {
        return server.getHostProfile();
    }

    @Override
    public void setHostProfile(@Nullable GameProfile hostProfile) {
        throw readOnlyException();
    }

    @Override
    public boolean isSingleplayer() {
        return server.isSingleplayer();
    }

    @Override
    protected void generateKeyPair() {
        throw readOnlyException();
    }

    @Override
    public void setDifficulty(Difficulty difficulty, boolean forceUpdate) {
        throw readOnlyException();
    }

    @Override
    public int adjustTrackingDistance(int initialDistance) {
        throw readOnlyException();
    }

    @Override
    public void setDifficultyLocked(boolean locked) {
        throw readOnlyException();
    }

    @Override
    public boolean isMonsterSpawningEnabled() {
        return server.isMonsterSpawningEnabled();
    }

    @Override
    public boolean isDemo() {
        return server.isDemo();
    }

    @Override
    public void setDemo(boolean demo) {
        throw readOnlyException();
    }

    //TODO consider unmodifiable ServerResourcePackProperties
    @Override
    public Optional<net.minecraft.server.MinecraftServer.ServerResourcePackProperties> getResourcePackProperties() {
        return server.getResourcePackProperties();
    }

    @Override
    public boolean requireResourcePack() {
        return server.requireResourcePack();
    }

    @Override
    public boolean isDedicated() {
        return server.isDedicated();
    }

    public int getRateLimit() {
        return server.getRateLimit();
    }

    @Override
    public boolean isOnlineMode() {
        return server.isOnlineMode();
    }

    @Override
    public void setOnlineMode(boolean onlineMode) {
        throw readOnlyException();
    }

    @Override
    public boolean shouldPreventProxyConnections() {
        return server.shouldPreventProxyConnections();
    }

    @Override
    public void setPreventProxyConnections(boolean preventProxyConnections) {
        throw readOnlyException();
    }

    @Override
    public boolean shouldSpawnAnimals() {
        return server.shouldSpawnAnimals();
    }

    @Override
    public boolean shouldSpawnNpcs() {
        return server.shouldSpawnNpcs();
    }

    @Override
    public boolean isUsingNativeTransport() {
        return server.isUsingNativeTransport();
    }

    @Override
    public boolean isPvpEnabled() {
        return server.isPvpEnabled();
    }

    @Override
    public void setPvpEnabled(boolean pvpEnabled) {
        throw readOnlyException();
    }

    @Override
    public boolean isFlightEnabled() {
        return server.isFlightEnabled();
    }

    @Override
    public void setFlightEnabled(boolean flightEnabled) {
        throw readOnlyException();
    }

    @Override
    public boolean areCommandBlocksEnabled() {
        return server.areCommandBlocksEnabled();
    }

    @Override
    public String getServerMotd() {
        return server.getServerMotd();
    }

    @Override
    public void setMotd(String motd) {
        throw readOnlyException();
    }

    @Override
    public boolean shouldPreviewChat() {
        return server.shouldPreviewChat();
    }

    @Override
    public boolean isStopped() {
        return server.isStopped();
    }

    //TODO consider unmodifiable PlayerManager
    @Override
    public PlayerManager getPlayerManager() {
        return server.getPlayerManager();
    }

    @Override
    public void setPlayerManager(PlayerManager playerManager) {
        throw readOnlyException();
    }

    @Override
    public boolean isRemote() {
        return server.isRemote();
    }

    @Override
    public void setDefaultGameMode(GameMode gameMode) {
        throw readOnlyException();
    }

    //TODO consider unmodifiable ServerNetworkIo
    @Override
    public ServerNetworkIo getNetworkIo() {
        return server.getNetworkIo();
    }

    @Override
    public boolean isLoading() {
        return server.isLoading();
    }

    @Override
    public boolean hasGui() {
        return server.hasGui();
    }

    @Override
    public boolean openToLan(@Nullable GameMode gameMode, boolean cheatsAllowed, int port) {
        throw readOnlyException();
    }

    @Override
    public int getTicks() {
        return server.getTicks();
    }

    @Override
    public int getSpawnProtectionRadius() {
        return server.getSpawnProtectionRadius();
    }

    @Override
    public boolean isSpawnProtected(ServerWorld world, BlockPos pos, PlayerEntity player) {
        return server.isSpawnProtected(world, pos, player);
    }

    @Override
    public boolean acceptsStatusQuery() {
        return server.acceptsStatusQuery();
    }

    @Override
    public boolean hideOnlinePlayers() {
        return server.hideOnlinePlayers();
    }

    //TODO consider unmodifiable Proxy
    @Override
    public Proxy getProxy() {
        return server.getProxy();
    }

    @Override
    public int getPlayerIdleTimeout() {
        return server.getPlayerIdleTimeout();
    }

    @Override
    public void setPlayerIdleTimeout(int playerIdleTimeout) {
        throw readOnlyException();
    }

    //TODO consider unmodifiable MinecraftSessionService
    @Override
    public MinecraftSessionService getSessionService() {
        return server.getSessionService();
    }

    //TODO consider unmodifiable SignatureVerifier
    @Override
    public SignatureVerifier getServicesSignatureVerifier() {
        return server.getServicesSignatureVerifier();
    }

    //TODO consider unmodifiable GameProfileRepository
    @Override
    public GameProfileRepository getGameProfileRepo() {
        return server.getGameProfileRepo();
    }

    //TODO consider unmodifiable UserCache
    @Override
    public UserCache getUserCache() {
        return server.getUserCache();
    }

    //TODO consider unmodifiable ServerMetadata
    @Override
    public ServerMetadata getServerMetadata() {
        return server.getServerMetadata();
    }

    @Override
    public void forcePlayerSampleUpdate() {
        throw readOnlyException();
    }

    @Override
    public int getMaxWorldBorderRadius() {
        return server.getMaxWorldBorderRadius();
    }

    @Override
    public boolean shouldExecuteAsync() {
        return server.shouldExecuteAsync();
    }

    @Override
    public void executeSync(Runnable runnable) {
        throw readOnlyException();
    }

    @Override
    public Thread getThread() {
        return server.getThread();
    }

    @Override
    public int getNetworkCompressionThreshold() {
        return server.getNetworkCompressionThreshold();
    }

    @Override
    public boolean shouldEnforceSecureProfile() {
        return server.shouldEnforceSecureProfile();
    }

    @Override
    public long getTimeReference() {
        return server.getTimeReference();
    }

    //TODO consider unmodifiable DataFixer
    @Override
    public DataFixer getDataFixer() {
        return server.getDataFixer();
    }

    @Override
    public int getSpawnRadius(@Nullable ServerWorld world) {
        return server.getSpawnRadius(world);
    }

    //TODO consider unmodifiable ServerAdvancementLoader
    @Override
    public ServerAdvancementLoader getAdvancementLoader() {
        return server.getAdvancementLoader();
    }

    //TODO consider unmodifiable CommandFunctionManager
    @Override
    public CommandFunctionManager getCommandFunctionManager() {
        return server.getCommandFunctionManager();
    }

    @Override
    public CompletableFuture<Void> reloadResources(Collection<String> dataPacks) {
        throw readOnlyException();
    }

    @Override
    public void kickNonWhitelistedPlayers(ServerCommandSource source) {
        throw readOnlyException();
    }

    //TODO consider unmodifiable ResourcePackManager
    @Override
    public ResourcePackManager getDataPackManager() {
        return server.getDataPackManager();
    }

    //TODO consider unmodifiable CommandManager
    @Override
    public CommandManager getCommandManager() {
        return server.getCommandManager();
    }

    //TODO consider unmodifiable ServerCommandSource
    @Override
    public ServerCommandSource getCommandSource() {
        return server.getCommandSource();
    }

    @Override
    public boolean shouldReceiveFeedback() {
        return server.shouldReceiveFeedback();
    }

    @Override
    public boolean shouldTrackOutput() {
        return server.shouldTrackOutput();
    }

    @Override
    public boolean shouldBroadcastConsoleToOps() {
        return server.shouldBroadcastConsoleToOps();
    }

    //TODO consider unmodifiable RecipeManager
    @Override
    public RecipeManager getRecipeManager() {
        return server.getRecipeManager();
    }

    //TODO consider unmodifiable ServerScoreboard
    @Override
    public ServerScoreboard getScoreboard() {
        return server.getScoreboard();
    }

    //TODO consider unmodifiable DataCommandStorage
    @Override
    public DataCommandStorage getDataCommandStorage() {
        return server.getDataCommandStorage();
    }

    //TODO consider unmodifiable LootManager
    @Override
    public LootManager getLootManager() {
        return server.getLootManager();
    }

    //TODO consider unmodifiable LootConditionManager
    @Override
    public LootConditionManager getPredicateManager() {
        return server.getPredicateManager();
    }

    //TODO consider unmodifiable LootFunctionManager
    @Override
    public LootFunctionManager getItemModifierManager() {
        return server.getItemModifierManager();
    }

    //TODO consider unmodifiable GameRules
    @Override
    public GameRules getGameRules() {
        return server.getGameRules();
    }

    //TODO consider unmodifiable BossBarManager
    @Override
    public BossBarManager getBossBarManager() {
        return server.getBossBarManager();
    }

    @Override
    public boolean isEnforceWhitelist() {
        return server.isEnforceWhitelist();
    }

    @Override
    public void setEnforceWhitelist(boolean enforceWhitelist) {
        throw readOnlyException();
    }

    @Override
    public float getTickTime() {
        return server.getTickTime();
    }

    @Override
    public int getPermissionLevel(GameProfile profile) {
        return server.getPermissionLevel(profile);
    }

    //TODO consider unmodifiable MetricsData
    @Override
    public MetricsData getMetricsData() {
        return server.getMetricsData();
    }

    //TODO consider unmodifiable Profiler
    @Override
    public Profiler getProfiler() {
        return server.getProfiler();
    }

    @Override
    public boolean isHost(GameProfile profile) {
        return server.isHost(profile);
    }

    @Override
    public void dumpProperties(Path file) throws IOException {
        throw readOnlyException();
    }

    @Override
    public boolean isRecorderActive() {
        return server.isRecorderActive();
    }

    @Override
    public void setupRecorder(Consumer<ProfileResult> resultConsumer, Consumer<Path> dumpConsumer) {
        throw readOnlyException();
    }

    @Override
    public void resetRecorder() {
        throw readOnlyException();
    }

    @Override
    public void stopRecorder() {
        throw readOnlyException();
    }

    @Override
    public void forceStopRecorder() {
        throw readOnlyException();
    }

    @Override
    public Path getSavePath(WorldSavePath worldSavePath) {
        return server.getSavePath(worldSavePath);
    }

    @Override
    public boolean syncChunkWrites() {
        return server.syncChunkWrites();
    }

    //TODO consider unmodifiable StructureTemplateManager
    @Override
    public StructureTemplateManager getStructureTemplateManager() {
        return server.getStructureTemplateManager();
    }

    //TODO consider unmodifiable SaveProperties
    @Override
    public SaveProperties getSaveProperties() {
        return server.getSaveProperties();
    }

    @Override
    public DynamicRegistryManager.Immutable getRegistryManager() {
        return server.getRegistryManager();
    }

    //TODO consider unmodifiable TextStream
    @Override
    public TextStream createFilterer(ServerPlayerEntity player) {
        return server.createFilterer(player);
    }

    //TODO consider unmodifiable ServerPlayerInteractionManager
    @Override
    public ServerPlayerInteractionManager getPlayerInteractionManager(ServerPlayerEntity player) {
        return server.getPlayerInteractionManager(player);
    }

    //TODO consider unmodifiable GameMode
    @Override
    public GameMode getForcedGameMode() {
        return server.getForcedGameMode();
    }

    //TODO consider unmodifiable ResourceManager
    @Override
    public ResourceManager getResourceManager() {
        return server.getResourceManager();
    }

    @Override
    public boolean isSaving() {
        return server.isSaving();
    }

    @Override
    public boolean isDebugRunning() {
        return server.isDebugRunning();
    }

    @Override
    public void startDebug() {
        throw readOnlyException();
    }

    //TODO consider unmodifiable ProfileResult
    @Override
    public ProfileResult stopDebug() {
        throw readOnlyException();
    }

    @Override
    public int getMaxChainedNeighborUpdates() {
        return server.getMaxChainedNeighborUpdates();
    }

    @Override
    public void logChatMessage(Text message, MessageType.Parameters params, @Nullable String prefix) {
        throw readOnlyException();
    }

    //TODO consider unmodifiable MessageDecorator
    @Override
    public MessageDecorator getMessageDecorator() {
        return server.getMessageDecorator();
    }

    /*public class DummySession extends LevelStorage.Session
            implements AutoCloseable {

        public DummySession(String directoryName) throws IOException {
            super(directoryName);
        }

        @Override
        public WorldSaveHandler createSaveHandler() {
            return null;
        }
    }*/
}

