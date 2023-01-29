package org.boosted.mixin.context;

import net.minecraft.world.World;
import org.boosted.BoostedWorldContext;
import org.boosted.WorldContextGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(World.class)
public class WorldMixin implements WorldContextGetter {
	@Unique
	private BoostedWorldContext boosted$WorldContext;

	public BoostedWorldContext getBoostedWorldContext() {
		if (boosted$WorldContext == null) {
			boosted$WorldContext = new BoostedWorldContext(((World)(Object)this));
		}
		return boosted$WorldContext;
	}
}
