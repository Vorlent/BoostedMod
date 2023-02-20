package org.boosted.mixin.getServer;

import net.minecraft.entity.vehicle.CommandBlockMinecartEntity;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CommandBlockMinecartEntity.class)
public class CommandBlockMinecartEntityMixin {

    // this is impossible to fix as it requires rewriting the command logic so the server is passed in via parameters
    // and that would require breaking all commands.
	/*
    public ServerCommandSource getSource() {
        return new ServerCommandSource(this, CommandBlockMinecartEntity.this.getPos(),
            CommandBlockMinecartEntity.this.getRotationClient(), this.getWorld(), 2,
            this.getCustomName().getString(), CommandBlockMinecartEntity.this.getDisplayName(),
            this.getWorld().getServer(), CommandBlockMinecartEntity.this);
    }
    */
}
