package org.boosted.mixin.scoreboard;

import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Collection;
import java.util.Set;

@Mixin(Team.class)
public interface ModifiablePlayerListAccessor {

    @Accessor("playerList")
    Set<String> getModifiablePlayerList();
}
