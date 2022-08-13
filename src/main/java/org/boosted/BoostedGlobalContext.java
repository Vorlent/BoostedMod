package org.boosted;

public class BoostedGlobalContext {
    private final BoostedThreadExecutor preTick;
    private final BoostedThreadExecutor postTick;

    public BoostedGlobalContext(Thread thread) {
        preTick = new BoostedThreadExecutor(thread);
        postTick = new BoostedThreadExecutor(thread);
    }

    public BoostedThreadExecutor preTick() {
        return preTick;
    }

    public BoostedThreadExecutor postTick() {
        return postTick;
    }
}
