package org.boosted;

public class BoostedGlobalContext {
    private final BoostedThreadExecutor preTick;
    private final BoostedThreadExecutor postTick;

    /**
     * The boosted global context is meant to be used for single threaded work between world simulations.
     * @param thread the main thread of minecraft that would usually execute all worlds in vanilla.
     */
    public BoostedGlobalContext(Thread thread) {
        preTick = new BoostedThreadExecutor(thread);
        postTick = new BoostedThreadExecutor(thread);
    }

    /**
     * Use this executor if you want to schedule work on the main thread before the tick has started.
     * @return preTick Executor
     */
    public BoostedThreadExecutor preTick() {
        return preTick;
    }


    /**
     * Use this executor if you want to schedule work on the main thread after the tick has finished.
     * @return postTick Executor
     */
    public BoostedThreadExecutor postTick() {
        return postTick;
    }

    /**
     * The main thread can change over time
     * @param thread the thread that is supposed to execute this world
     */
    public void setThread(Thread thread) {
        preTick.setServerThread(thread);
        postTick.setServerThread(thread);
    }
}
