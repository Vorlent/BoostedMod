package org.boosted;

import net.minecraft.server.MinecraftServer;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadCoordinator {

    private final Map<String, Set<Thread>> mcThreadTracker = new ConcurrentHashMap<>();
    private Phaser phaser;
    private ExecutorService executorService;
    private Set<String> currentTasks = ConcurrentHashMap.newKeySet();

    private final AtomicInteger currentEnts = new AtomicInteger();

    private final AtomicInteger currentEnvs = new AtomicInteger();

    private final AtomicInteger currentTEs = new AtomicInteger();

    private final AtomicInteger threadID = new AtomicInteger();

    private final AtomicInteger currentWorlds = new AtomicInteger();

    private final AtomicBoolean isTicking = new AtomicBoolean();

    private final static ThreadCoordinator instance;
    static {
        instance = new ThreadCoordinator();
    }
    public static ThreadCoordinator getInstance() {
        return instance;
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

    public boolean serverExecutionThreadPatch(MinecraftServer ms) {
        return isThreadPooled("MCMT", Thread.currentThread());
    }

    public void setupThreadpool(int parallelism) {
        final ClassLoader cl = BoostedMod.class.getClassLoader();
        ForkJoinPool.ForkJoinWorkerThreadFactory fjpf = p -> {
            ForkJoinWorkerThread fjwt = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(p);
            fjwt.setName("MCMT-Pool-Thread-" + threadID.getAndIncrement());
            regThread("MCMT", fjwt);
            fjwt.setContextClassLoader(cl);
            return fjwt;
        };
        executorService = new ForkJoinPool(
                parallelism,
                fjpf,
                null, false);
    }

    public Phaser getPhaser() {
        return phaser;
    }

    public void setPhaser(Phaser phaser) {
        this.phaser = phaser;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public Map<String, Set<Thread>> getMcThreadTracker() {
        return mcThreadTracker;
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
