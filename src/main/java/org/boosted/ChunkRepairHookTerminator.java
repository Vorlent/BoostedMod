package org.boosted;

import com.mojang.datafixers.util.Either;
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
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;

/**
 * Handles chunk forcing in scenarios where world corruption has occurred
 *
 * @author jediminer543
 *
 */
public class ChunkRepairHookTerminator {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final AtomicBoolean mainThreadChunkLoad = new AtomicBoolean();
    public static final AtomicLong mainThreadChunkLoadCount = new AtomicLong();
    public static final String mainThread = "Server thread";

    public static void chunkLoadDrive(ServerChunkManager.MainThreadExecutor executor, BooleanSupplier isDone, ServerChunkManager scp,
                                      CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> completableFuture, long chunkpos) {
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
                    if (GeneralConfig.enableTimeoutRegen || GeneralConfig.enableBlankReturn) {

                        if (GeneralConfig.enableBlankReturn) {
							Chunk blankChunk = new WorldChunk(scp.getWorld(), new ChunkPos(chunkpos));
							completableFuture.complete(Either.left(blankChunk));
                        } else {
                            scp.threadedAnvilChunkStorage.getNbt(new ChunkPos(chunkpos))
                                .thenAccept((ocnbt) -> {
                                    ocnbt.ifPresent((cnbt) -> {
                                        ProtoChunk cp = ChunkSerializer.deserialize((ServerWorld) scp.getWorld(),
                                                scp.threadedAnvilChunkStorage.pointOfInterestStorage, new ChunkPos(chunkpos), cnbt);
                                        completableFuture.complete(Either.left(new WorldChunk((ServerWorld) scp.getWorld(), cp, null)));
                                    });
                                });

                            completableFuture.complete(ChunkHolder.UNLOADED_CHUNK);
                        }
                    } else {
                        System.err.println(completableFuture);
                        ChunkHolder chunkholder = Objects.requireNonNull(scp.getChunkHolder(chunkpos));
                        @Nullable
                        CompletableFuture<?> firstBroke = null;
                        for (ChunkStatus cs : ChunkStatus.createOrderedList()) {
                            CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> cf = chunkholder.getFutureFor(cs);
                            if (cf == ChunkHolder.UNLOADED_CHUNK_FUTURE) {
                                System.out.println("Status: " + cs + " is not yet loaded");
                            } else {
                                System.out.println("Status: " + cs + " is " + cf.toString());
                                if (firstBroke == null && !cf.toString().contains("Completed normally")) {
                                    firstBroke = cf;
                                }
                            }
                        }
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
