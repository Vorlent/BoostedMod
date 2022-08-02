package org.boosted.serdes.filter;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.boosted.config.GeneralConfig;
import org.boosted.serdes.SerDesRegistry;
import org.boosted.serdes.pools.ChunkLockPool;
import org.boosted.serdes.pools.ISerDesPool;
import org.boosted.serdes.ISerDesHookType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LegacyFilter implements ISerDesFilter {

	ISerDesPool clp;
	ISerDesPool.ISerDesOptions config;
	
	@Override
	public void init() {
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
		return GeneralConfig.teBlackList;
	}
	
	@Override
	public Set<Class<?>> getWhitelist() {
		return GeneralConfig.teWhiteList;
	}

}
