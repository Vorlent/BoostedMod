package org.boosted;

import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.world.World;

public class BoostedWorldContext {
    private final World world;
    private final BoostedThreadExecutor preTick;
    private final BoostedThreadExecutor midTick;
    private final BoostedThreadExecutor postTick;

    public BoostedWorldContext(World world) {
        this.world = world;
        preTick = new BoostedThreadExecutor();
        midTick = new BoostedThreadExecutor();
        postTick = new BoostedThreadExecutor();
    }

    public BoostedThreadExecutor preTick() {
        return preTick;
    }

    public BoostedThreadExecutor midTick() {
        return midTick;
    }

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
