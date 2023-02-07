package org.boosted.mixin.getServer;

import net.minecraft.block.entity.CommandBlockBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CommandBlockBlockEntity.class)
public class CommandBlockBlockEntityMixin {

    // this is impossible to fix as it requires rewriting the command logic so the server is passed in via parameters
    // and that would require breaking all commands.
	/*
        @Override
        public ServerCommandSource getSource() {
            return new ServerCommandSource(this, Vec3d.ofCenter(CommandBlockBlockEntity.this.pos), Vec2f.ZERO, this.getWorld(), 2, this.getCustomName().getString(), this.getCustomName(), this.getWorld().getServer(), null);
        }
	*/
}
