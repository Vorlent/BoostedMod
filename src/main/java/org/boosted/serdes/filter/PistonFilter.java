package org.boosted.serdes.filter;

import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.boosted.serdes.SerDesRegistry;
import org.boosted.serdes.ISerDesHookType;
import org.boosted.serdes.pools.ChunkLockPool;
import org.boosted.serdes.pools.ISerDesPool;
import org.boosted.serdes.pools.ISerDesPool.ISerDesOptions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PistonFilter implements ISerDesFilter {

	ISerDesPool clp;
	ISerDesOptions config;
	
	@Override
	public void init() {
		//TODO Figure out if piston specific chunklock can be used (or just move to using the 
		clp = SerDesRegistry.getOrCreatePool("LEGACY", ChunkLockPool::new);
		Map<String, Object> cfg = new HashMap<>();
		cfg.put("range", "1");
		config = clp.compileOptions(cfg);
	}
	
	@Override
	public void serialise(Runnable task, Object obj, BlockPos bp, World w, ISerDesHookType hookType) {
		clp.serialise(task, obj, bp, w, config);
	}
	
	@Override
	public Set<Class<?>> getTargets() {
		Set<Class<?>> out = new HashSet<Class<?>>();
		out.add(PistonBlockEntity.class);
		return out;
	}

}
