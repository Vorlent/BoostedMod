package org.boosted;

import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.world.World;

public class BoostedWorldContext {
    private final World world;
    private final BoostedThreadExecutor preTick;
    private final BoostedThreadExecutor midTick;
    private final BoostedThreadExecutor postTick;

    /**
     * There should be exactly one BoostedWorldContext for every world.
     * @param world The world for which this BoostedWorldContext was created.
     */

    public BoostedWorldContext(World world) {
        this.world = world;
        preTick = new BoostedThreadExecutor();
        midTick = new BoostedThreadExecutor();
        postTick = new BoostedThreadExecutor();
    }

    /**
     * Use this executor if you want to execute work on a world thread before the tick has started.
     * @return preTick Executor
     */
    public BoostedThreadExecutor preTick() {
        return preTick;
    }

    /**
     * Use this executor if you want to execute work on a world thread at an unspecified safepoint within a tick
     * (the intention is as soon as possible).
     * @return midTick Executor
     */
    public BoostedThreadExecutor midTick() {
        return midTick;
    }

    /**
     * Use this executor if you want to execute work on a world thread after the tick has finished.
     * @return postTick Executor
     */
    public BoostedThreadExecutor postTick() {
        return postTick;
    }

    /**
     * Using a thread pool means the threads change all the time, we have to
     * update the threads in the executors, etc. every time the thread changes
     * @param thread the thread that is supposed to execute this world
     */
    public void setThread(Thread thread) {
        preTick.setServerThread(thread);
        midTick.setServerThread(thread);
        postTick.setServerThread(thread);
        world.thread = thread;
        ((ServerChunkManager)world.getChunkManager()).serverThread = thread;
    }
}
