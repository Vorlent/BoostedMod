package org.boosted.mixin.getServer;

import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.block.entity.LecternBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LecternBlockEntity.class)
public class LecternBlockEntityMixin {

	/* This cant be fixed for now
        private ServerCommandSource getCommandSource(@Nullable PlayerEntity player) {
            ...
            return new ServerCommandSource(CommandOutput.DUMMY, vec3d, Vec2f.ZERO, (ServerWorld)this.world, 2, string, text, this.world.getServer(), player);
        }
	*/
}
