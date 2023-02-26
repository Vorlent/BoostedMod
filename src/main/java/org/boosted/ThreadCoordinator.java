package org.boosted;

import org.jetbrains.annotations.Nullable;

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
    private final AtomicInteger currentEnvs = new AtomicInteger();
    private final AtomicInteger threadID = new AtomicInteger();
    private final AtomicInteger currentWorlds = new AtomicInteger();
    private final AtomicBoolean isTicking = new AtomicBoolean();

    @Nullable
    private BoostedGlobalContext boostedContext;
    private final static ThreadCoordinator instance;
    static {
        instance = new ThreadCoordinator();
    }
    public static ThreadCoordinator getInstance() {
        return instance;
    }

    @Nullable
    public BoostedGlobalContext getBoostedContext() {
        return boostedContext;
    }

    public void setBoostedContext(BoostedGlobalContext boostedContext) {
        this.boostedContext = boostedContext;
    }

    public boolean shouldThreadChunks() {
        return true;
    }

    public boolean isThreadPooled(String poolName, Thread t) {
        return mcThreadTracker.containsKey(poolName) && mcThreadTracker.get(poolName).contains(t);
    }

    public void setupThreadpool(int parallelism) {
        final ClassLoader cl = BoostedMod.class.getClassLoader();
        ForkJoinPool.ForkJoinWorkerThreadFactory fjpf = p -> {
            ForkJoinWorkerThread fjwt = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(p);
            fjwt.setName("Boosted-Pool-Thread-" + threadID.getAndIncrement());
            mcThreadTracker.computeIfAbsent("Boosted", s -> ConcurrentHashMap.newKeySet()).add(fjwt);
            fjwt.setContextClassLoader(cl);
            return fjwt;
        };
        executorService = new ForkJoinPool(parallelism, fjpf,null, false);
    }

    @Nullable
    public Phaser getPhaser() {
        return phaser;
    }

    @Nullable
    public ExecutorService getExecutorService() {
        return executorService;
    }

    public Set<String> getCurrentTasks() {
        return currentTasks;
    }

    public AtomicInteger getCurrentEnvs() {
        return currentEnvs;
    }

    public AtomicBoolean getIsTicking() {
        return isTicking;
    }
}
