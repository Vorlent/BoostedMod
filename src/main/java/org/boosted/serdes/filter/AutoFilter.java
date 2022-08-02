package org.boosted.serdes.filter;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.boosted.serdes.SerDesRegistry;
import org.boosted.serdes.ISerDesHookType;
import org.boosted.serdes.pools.ChunkLockPool;
import org.boosted.serdes.pools.ISerDesPool;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author Hunter Hancock (meta1203)
 * This, and any other code I submit to jediminer543's JMT-MCMT project, is licensed under the 2-Clause BSD License.
 * (https://opensource.org/licenses/BSD-2-Clause)
 */
public class AutoFilter implements ISerDesFilter {
	private static AutoFilter SINGLETON;
	
	private ISerDesPool pool;
	private Set<Class<?>> blacklist = ConcurrentHashMap.newKeySet();
	
	public static AutoFilter singleton() {
		if (SINGLETON == null) SINGLETON = new AutoFilter();
		return SINGLETON;
	}
	
	@Override
	public void init() {
		pool = SerDesRegistry.getOrCreatePool("AUTO", ChunkLockPool::new);
	}
	
	@Override
	public void serialise(Runnable task, Object obj, BlockPos bp, World w, ISerDesHookType hookType) {
		pool.serialise(task, obj, bp, w, null);
	}

	@Override
	public Set<Class<?>> getTargets() {
		return blacklist;
	}

	@Override
	public ClassMode getModeOnline(Class<?> c) {
		return ClassMode.UNKNOWN;
	}
	
	public void addClassToBlacklist(Class<?> c) {
		blacklist.add(c);
	}
}
