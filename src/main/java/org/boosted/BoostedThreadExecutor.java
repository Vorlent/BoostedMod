package org.boosted;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.profiler.SampleType;
import net.minecraft.util.profiler.Sampler;
import net.minecraft.util.thread.ThreadExecutor;
import org.jetbrains.annotations.Nullable;

import java.util.List;


/**
 * A very simple ThreadExecutor that lets us queue tasks
 * and explicitly process them on a specific thread.
 * The intention is that we are going to issue tasks to other world threads
 * and wait for the other world to finish processing its current tick or a potentially more fine-grained unit of work
 * and then empty out the current queue
 */
public class BoostedThreadExecutor extends ThreadExecutor<Runnable> {
    @Nullable
    private Thread serverThread;
    BoostedThreadExecutor() {
        this(null);
    }
    BoostedThreadExecutor(@Nullable Thread serverThread) {
        super("Boosted Thread executor");
        this.serverThread = serverThread;
    }

    @Override
    public String getName() {
        return serverThread == null ? "Boosted Thread executor" : "Boosted Thread executor for " + serverThread.getName();
    }
    @Override
    public List<Sampler> createSamplers() {
        return ImmutableList.of(Sampler.create(this.getName() + "-pending-tasks", SampleType.EVENT_LOOPS, this::getTaskCount));
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
    @Nullable
    protected Thread getThread() {
        return serverThread;
    }


    /**
     * This threadpool ensures that all queued tasks will be executed on a designated thread.
     * However, running worlds on a thread pool will mean that after every tick, the world can be run on a different thread,
     * therefore any world sent to a thread pool must update this server thread before simulating the world
     * @param serverThread the current thread of the thread pool that will be executing the world
     */
    public void setServerThread(Thread serverThread) {
        this.serverThread = serverThread;
    }

    @Override
    public void executeTask(Runnable task) {
        if (serverThread == null) {
            throw new IllegalStateException("no thread to execute on available");
        }
        if (!isOnThread()) {
            throw new IllegalStateException("runTask must be executed on thread " + serverThread.getName());
        }
        super.executeTask(task);
    }

    @Override
    public void runTasks() {
        super.runTasks();
    }
}
