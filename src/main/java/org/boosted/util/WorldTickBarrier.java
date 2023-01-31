package org.boosted.util;

import java.util.concurrent.Phaser;

/**
 * This barrier is supposed to make it easier to reason about how worlds are synchronized.
 *
 * It is quite simple. Before each tick, each world registers itself with the WorldTickBarrier.
 * Once the tick has finished, the world signals that it finished
 * The main thread then waits for all Worlds to finish.
 *
 */
public class WorldTickBarrier {

    private Phaser phaser = new Phaser();

    /**
     * Each world must call this at the beginning of the tick
     */
    public void registerWorld() {
        phaser.register();
    }

    /**
     * Each world must call this at the end of the tick
     */
    public void finishWorld() {
        phaser.arriveAndDeregister();
    }

    /**
     * The main thread calls this to wait for all worlds
     */
    public void waitForAllWorlds() {
        phaser.arriveAndAwaitAdvance();
        phaser = null;
    }

    /**
     * The main thread also has to register itself
     */
    public void reset() {
        phaser = new Phaser();
        phaser.register();
    }
}
