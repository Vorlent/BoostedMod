package org.boosted;

import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.World;


/**
 * A very simple ThreadExecutor that lets us queue tasks
 * and explicitly process them on a specific thread.
 * The intention is that we are going to issue tasks to other world threads
 * and wait for the other world to finish processing its current tick or a potentially more fine grained unit of work
 * and then empty out the current queue
 */
public class BoostedThreadExecutor extends ThreadExecutor<Runnable> {
    private Thread serverThread;

    BoostedThreadExecutor(World world, Thread serverThread) {
        super("Thread executor for " + serverThread.getName());
        this.serverThread = serverThread;
    }

    @Override
    protected Runnable createTask(Runnable runnable) {
        return runnable;
    }

    @Override
    protected boolean canExecute(Runnable task) {
        return true;
    }

    @Override
    protected boolean shouldExecuteAsync() {
        return true;
    }

    @Override
    protected Thread getThread() {
        return serverThread;
    }

    @Override
    public void executeTask(Runnable task) {
        if(!isOnThread()) {
            throw new IllegalStateException("runTask must be executed on thread " + serverThread.getName());
        }
        super.executeTask(task);
    }
}
