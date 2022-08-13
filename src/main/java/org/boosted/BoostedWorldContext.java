package org.boosted;

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

    public void setThread(Thread thread) {
        preTick.setServerThread(thread);
        midTick.setServerThread(thread);
        postTick.setServerThread(thread);
    }
}
