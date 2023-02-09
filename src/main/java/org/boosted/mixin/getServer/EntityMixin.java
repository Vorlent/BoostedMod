package org.boosted.mixin.getServer;

import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Entity.class)
public class EntityMixin {

    /**
     * @author Vorlent
     * @reason tickPortal needs exclusive write access to MinecraftServer to obtain the destination world reference
     */
    @Overwrite
    protected void tickPortal() {
        Entity entity = (Entity) (Object) this;
        if (!(entity.world instanceof ServerWorld)) {
            return;
        }
        int i = entity.getMaxNetherPortalTime();
        ServerWorld serverWorld = (ServerWorld)entity.world;
        if (entity.inNetherPortal) {
            /*
            Patch BEGIN
             */
            serverWorld.getSynchronizedServer().write(minecraftServer -> {
                ServerWorld serverWorld2 = minecraftServer.getWorld(entity.world.getRegistryKey() == World.NETHER ? World.OVERWORLD : World.NETHER);
                if (serverWorld2 != null && minecraftServer.isNetherAllowed() && !entity.hasVehicle() && entity.netherPortalTime++ >= i) {
                    entity.world.getProfiler().push("portal");
                    entity.netherPortalTime = i;
                    entity.resetPortalCooldown();
                    entity.moveToWorld(serverWorld2);
                    entity.world.getProfiler().pop();
                }
            });
            /*
            Patch END
             */
            entity.inNetherPortal = false;
        } else {
            if (entity.netherPortalTime > 0) {
                entity.netherPortalTime -= 4;
            }
            if (entity.netherPortalTime < 0) {
                entity.netherPortalTime = 0;
            }
        }
        entity.tickPortalCooldown();
    }

    /**
    @Nullable
    TODO GET RID OF THIS
    public MinecraftServer getServer() {
        return this.world.getServer();
    }*/

    /** unfixable
    public ServerCommandSource getCommandSource() {
        return new ServerCommandSource(this, this.getPos(), this.getRotationClient(), this.world instanceof ServerWorld ? (ServerWorld)this.world : null, this.getPermissionLevel(), this.getName().getString(), this.getDisplayName(), this.world.getServer(), this);
    }*/
}
