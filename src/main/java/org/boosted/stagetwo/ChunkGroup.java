package org.boosted.stagetwo;

import net.minecraft.entity.player.PlayerEntity;

import java.util.HashSet;
import java.util.Set;

public class ChunkGroup {
    private final Set<Long> chunks = new HashSet<>();

    private final Set<PlayerEntity> players = new HashSet<>();

    public Set<Long> getChunks() {
        return chunks;
    }

    public Set<PlayerEntity> getPlayers() {
        return players;
    }
}
