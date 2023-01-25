package org.boosted;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadCoordinator {

    private final Map<String, Set<Thread>> mcThreadTracker = new ConcurrentHashMap<>();
    @Nullable
    private Phaser phaser;
    @Nullable
    private ExecutorService executorService;
    private final Set<String> currentTasks = ConcurrentHashMap.newKeySet();

    private final AtomicInteger currentEnts = new AtomicInteger();

    private final AtomicInteger currentEnvs = new AtomicInteger();

    private final AtomicInteger currentTEs = new AtomicInteger();

    private final AtomicInteger threadID = new AtomicInteger();

    private final AtomicInteger currentWorlds = new AtomicInteger();

    private final AtomicBoolean isTicking = new AtomicBoolean();

    @Nullable
    private BoostedGlobalContext boostedContext;

    private final HashMap<World, BoostedWorldContext> boostedContextMapping = new HashMap<>();

    private final static ThreadCoordinator instance;
    static {
        instance = new ThreadCoordinator();
    }
    public static ThreadCoordinator getInstance() {
        return instance;
    }

    public BoostedWorldContext getBoostedContext(World world) {
        return boostedContextMapping.get(world);
    }

    public void setBoostedContext(World world, BoostedWorldContext context) {
        boostedContextMapping.put(world, context);
    }

    /** Because I am not aware of a way of adding new attributes to existing classes
     we must construct a hashmap that points at the object instead.
     Because the world isn't pointing at our boosted world context
     we will have to do our own garbage collection.
     */
    public void garbageCollectBoostedWorldContexts(Iterable<ServerWorld> worlds) {
        Set<World> reachableWorlds = new HashSet<>();
        worlds.iterator().forEachRemaining(reachableWorlds::add);
        Set<World> unreachableWorlds = new HashSet<>(boostedContextMapping.keySet());
        unreachableWorlds.removeAll(reachableWorlds);
        unreachableWorlds.forEach(boostedContextMapping::remove);
    }

    public BoostedGlobalContext getBoostedContext() {
        return boostedContext;
    }

    public void setBoostedContext(BoostedGlobalContext boostedContext) {
        this.boostedContext = boostedContext;
    }

    public boolean shouldThreadChunks() {
        return true; //TODO
    }

    public boolean isThreadPooled(String poolName, Thread t) {
        return mcThreadTracker.containsKey(poolName) && mcThreadTracker.get(poolName).contains(t);
    }

    private void regThread(String poolName, Thread thread) {
        mcThreadTracker.computeIfAbsent(poolName, s -> ConcurrentHashMap.newKeySet()).add(thread);
    }

    public void setupThreadpool(int parallelism) {
        final ClassLoader cl = BoostedMod.class.getClassLoader();
        ForkJoinPool.ForkJoinWorkerThreadFactory fjpf = p -> {
            ForkJoinWorkerThread fjwt = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(p);
            fjwt.setName("Boosted-Pool-Thread-" + threadID.getAndIncrement());
            regThread("Boosted", fjwt);
            fjwt.setContextClassLoader(cl);
            return fjwt;
        };
        executorService = new ForkJoinPool(parallelism, fjpf,null, false);
    }

    @Nullable
    public Phaser getPhaser() {
        return phaser;
    }

    public void setPhaser(@Nullable Phaser phaser) {
        this.phaser = phaser;
    }

    @Nullable
    public ExecutorService getExecutorService() {
        return executorService;
    }

    public Set<String> getCurrentTasks() {
        return currentTasks;
    }

    public AtomicInteger getCurrentEnts() {
        return currentEnts;
    }

    public AtomicInteger getCurrentTEs() {
        return currentTEs;
    }

    public AtomicInteger getCurrentEnvs() {
        return currentEnvs;
    }

    public AtomicInteger getCurrentWorlds() {
        return currentWorlds;
    }

    public AtomicBoolean getIsTicking() {
        return isTicking;
    }

}
