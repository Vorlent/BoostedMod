package org.boosted.stagetwo.mixin;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkManager;
import org.boosted.stagetwo.ChunkGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public abstract class PartitionWorldMixin {

	@Shadow public abstract ChunkManager getChunkManager();

	@Inject(at = @At(value = "HEAD"),
		method = "Lnet/minecraft/server/world/ServerWorld;tick(Ljava/util/function/BooleanSupplier;)V")
	private void injectTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		if (getChunkManager() instanceof ServerChunkManager serverChunkManager) {
			ThreadedAnvilChunkStorage threadedAnvilChunkStorage = serverChunkManager.threadedAnvilChunkStorage;
			LongSet loadedChunks = threadedAnvilChunkStorage.loadedChunks;
			LongIterator iterator = loadedChunks.iterator();

			Map<PlayerEntity, ChunkGroup> playerToGroup = new HashMap<>();

			while (iterator.hasNext()) {
				long chunk = iterator.nextLong();

				List<ServerPlayerEntity> playersWatchingChunk = threadedAnvilChunkStorage.getPlayersWatchingChunk(new ChunkPos(chunk));
				ChunkGroup chunkGroup = null;
				for (ServerPlayerEntity player : playersWatchingChunk) {
					ChunkGroup group = playerToGroup.get(player);
					if (chunkGroup == null) { // this is the first player
						if (group == null) { // this player does not have a group yet
							chunkGroup = new ChunkGroup();
						} else { // this player already belongs to a group
							chunkGroup = group;
						}
						group.getPlayers().add(player);
						group.getChunks().add(chunk);
					} else { // this is not the first player
						if (group == null) { // this player does not have a group yet
							chunkGroup.getPlayers().add(player); // do not create a new group
						} else { // merge the groups
							chunkGroup.getPlayers().addAll(group.getPlayers());
							chunkGroup.getChunks().addAll(group.getChunks());

							for (PlayerEntity playerEntity : group.getPlayers()) {
								playerToGroup.put(playerEntity, chunkGroup);
							}
						}
					}
				}

			}
		}
		// I have nno idea wtf I am supposed to do here
		// I need to gather a list of players in this world
		// I need to track which chunks have been loaded because of this player (PlayerChunkWatchingManager)
		// then I need to form groups of chunks plus their players
		// these groups are disjoint and can be put into even smaller worlds
		// these smaller worlds can be simulated in parallel
	}
}
