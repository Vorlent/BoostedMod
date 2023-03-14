package org.boosted.util;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.boosted.ThreadCoordinator;

public class BoostedTeleportation {
    public static void teleportEntity(Entity entity, ServerWorld destination) { // this should be moved into an injected interface
        if (!(entity.world instanceof ServerWorld) || entity.isRemoved()) {
            return;
        }
        entity.detach();
        //entity.setRemoved(Entity.RemovalReason.CHANGED_DIMENSION);
        ThreadCoordinator.getInstance().getBoostedContext().postTick().execute(() -> entity.moveToWorld(destination));
    }
}
