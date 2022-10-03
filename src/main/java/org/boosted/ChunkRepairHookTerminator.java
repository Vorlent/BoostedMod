package org.boosted;

import com.mojang.datafixers.util.Either;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.boosted.config.GeneralConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;

/* 1.15.2 code; AKA the only thing that changed
import net.minecraft.world.biome.provider.SingleBiomeProviderSettings;
/* */
/**
 * Handles chunk forcing in scenarios where world corruption has occurred
 *
 * @author jediminer543
 *
 */
public class ChunkRepairHookTerminator {
    private static final Logger LOGGER = LogManager.getLogger();

    private static boolean bypassLoadTarget = false;

    public static class BrokenChunkLocator {
        long chunkPos;
        CompletableFuture<?> maincf;
        CompletableFuture<?> brokecf;
        public BrokenChunkLocator(long chunkPos, CompletableFuture<?> maincf, CompletableFuture<?> brokecf) {
            super();
            this.chunkPos = chunkPos;
            this.maincf = maincf;
            this.brokecf = brokecf;
        }
        public long getChunkPos() {
            return chunkPos;
        }

    }

    public static List<BrokenChunkLocator> breaks = new ArrayList<>();

    public static boolean isBypassLoadTarget() {
        return bypassLoadTarget;
    }

    public static AtomicBoolean mainThreadChunkLoad = new AtomicBoolean();
    public static AtomicLong mainThreadChunkLoadCount = new AtomicLong();
    public static String mainThread = "Server thread";

    public static void chunkLoadDrive(ServerChunkManager.MainThreadExecutor executor, BooleanSupplier isDone, ServerChunkManager scp,
                                      CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> completableFuture, long chunkpos) {
		/*
		if (!GeneralConfig.enableChunkTimeout) {
			bypassLoadTarget = false;
			executor.driveUntil(isDone);
			return;
		}
		*/
        int failcount = 0;
        if (isMainThread()) {
            mainThreadChunkLoadCount.set(0);
            mainThreadChunkLoad.set(true);
        }
        while (!isDone.getAsBoolean()) {
            if (!executor.runTask()) {
                if(isDone.getAsBoolean()) {
                    if (isMainThread()) {
                        mainThreadChunkLoad.set(false);
                    }
                    break;
                }
                // Nothing more to execute
                if (!GeneralConfig.enableChunkTimeout || failcount++ < GeneralConfig.timeoutCount) {
                    if (isMainThread()) {
                        mainThreadChunkLoadCount.incrementAndGet();
                    }
                    Thread.yield();
                    LockSupport.parkNanos("THE END IS ~~NEVER~~ LOADING", 100000L);
                } else {
                    LOGGER.error("", new TimeoutException("Error fetching chunk " + chunkpos));
                    bypassLoadTarget = true;
                    if (GeneralConfig.enableTimeoutRegen || GeneralConfig.enableBlankReturn) {

                        if (GeneralConfig.enableBlankReturn) {
							Chunk blankChunk = new WorldChunk(scp.getWorld(), new ChunkPos(chunkpos));
							completableFuture.complete(Either.left(blankChunk));
                        } else {
                            try {
                                NbtCompound cnbt = scp.threadedAnvilChunkStorage.getNbt(new ChunkPos(chunkpos));
                                if (cnbt != null) {
                                    ProtoChunk cp = ChunkSerializer.deserialize((ServerWorld) scp.getWorld(), scp.threadedAnvilChunkStorage.pointOfInterestStorage, new ChunkPos(chunkpos), cnbt);
                                    completableFuture.complete(Either.left(new WorldChunk((ServerWorld) scp.getWorld(), cp, null)));
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            completableFuture.complete(ChunkHolder.UNLOADED_CHUNK);
                        }
                    } else {
                        System.err.println(completableFuture.toString());
                        ChunkHolder chunkholder = scp.getChunkHolder(chunkpos);
                        CompletableFuture<?> firstBroke = null;
                        for (ChunkStatus cs : ChunkStatus.createOrderedList()) {
                            CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> cf = chunkholder.getFutureFor(cs);
                            if (cf == ChunkHolder.UNLOADED_CHUNK_FUTURE) {
                                System.out.println("Status: " + cs.toString() + " is not yet loaded");
                            } else {
                                System.out.println("Status: " + cs.toString() + " is " + cf.toString());
                                if (firstBroke == null && !cf.toString().contains("Completed normally")) {
                                    firstBroke = cf;
                                }
                            }
                        }
                        breaks.add(new BrokenChunkLocator(chunkpos, completableFuture, firstBroke));
                        completableFuture.complete(Either.right(new ChunkHolder.Unloaded() {
                            @Override
                            public String toString() {
                                return "TIMEOUT";
                            }
                        }));
                    }
                }
            }
        }
    }

    private static boolean isMainThread() {
        return Thread.currentThread().getName().equals(mainThread);
    }

}
