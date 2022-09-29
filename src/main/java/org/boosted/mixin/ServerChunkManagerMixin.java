package org.boosted.mixin;

import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import org.boosted.ChunkRepairHookTerminator;
import org.boosted.config.GeneralConfig;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.concurrent.CompletableFuture;

@Mixin(ServerChunkManager.class)
public abstract class ServerChunkManagerMixin extends ChunkManager {
    @Shadow
    @Final
    public ServerChunkManager.MainThreadExecutor mainThreadExecutor;
    /**
     * Intercept the upper bound of the for loop in getChunk. Returning 0 will skip the entire loop.
     * The loop checks the chunk cache. Skipping the loop forces the ChunkManager to always obtain a future which allows us to parallelize chunk loading
     */
    @ModifyConstant(method = "getChunk(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/Chunk;", constant = @Constant(intValue = 4))
    private int injected(int value) {
        if(!GeneralConfig.disableMultiChunk) {
            return 0; // read 0 values from the chunk cache
        }
        return value; // read value entries from the chunk cache
    }

    /**
     * if (Thread.currentThread() != this.serverThread) {
     * will be replaced by
     * if (Thread.currentThread() != Thread.currentThread()) {
     * which always evaluates to false, which bypasses the if statement
     * to execute getChunk in the current thread instead of dispatching it to the main thread.
     * @param mgr
     * @return current thread
     */

    @Redirect(method = {"getChunk(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/Chunk;", "getWorldChunk(II)Lnet/minecraft/world/chunk/WorldChunk;"},
            at = @At(value = "FIELD", target = "Lnet/minecraft/server/world/ServerChunkManager;serverThread:Ljava/lang/Thread;", opcode = Opcodes.GETFIELD))
    private Thread bypassServerThread(ServerChunkManager mgr) {
        return Thread.currentThread();
    }

    @Inject(method = "getChunk(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/Chunk;", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/world/ServerChunkManager$MainThreadExecutor;runTasks (Ljava/util/function/BooleanSupplier;)V"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private void chunkLoadDrive(int x, int z, ChunkStatus leastStatus, boolean create, CallbackInfoReturnable<Chunk> cir, Profiler profiler, long l, CompletableFuture i) {
        long chunkPos = ChunkPos.toLong(x, z);
        ChunkRepairHookTerminator.chunkLoadDrive(this.mainThreadExecutor, i::isDone, (ServerChunkManager) (Object) this, i, chunkPos);
    }
}
