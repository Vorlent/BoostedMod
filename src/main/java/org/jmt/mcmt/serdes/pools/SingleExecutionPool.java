package org.jmt.mcmt.serdes.pools;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SingleExecutionPool implements ISerDesPool {

	private Lock l = new ReentrantLock();
	
	@Override
	public void serialise(Runnable task, Object o, BlockPos bp, World w, @Nullable ISerDesOptions options) {
		try {
			l.lock();
			task.run();
		} finally {
			l.unlock();
		}
	}
	
}
