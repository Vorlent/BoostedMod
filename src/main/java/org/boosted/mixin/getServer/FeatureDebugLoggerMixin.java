package org.boosted.mixin.getServer;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.util.FeatureDebugLogger;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Locale;

import static net.minecraft.world.gen.feature.util.FeatureDebugLogger.FEATURES;

@Mixin(FeatureDebugLogger.class)
public class FeatureDebugLoggerMixin {
    private static final Logger LOGGER = LogUtils.getLogger();


    @Redirect(method = "method_39600(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/gen/feature/util/FeatureDebugLogger$Features;)V", at = @At(value = "INVOKE",
        target = "net/minecraft/server/world/ServerWorld.getServer ()Lnet/minecraft/server/MinecraftServer;"))
    private static MinecraftServer skipGetServer(ServerWorld instance) {
        return instance.getUnsynchronizedServer();
    }

    @Redirect(method = "method_39600(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/gen/feature/util/FeatureDebugLogger$Features;)V", at = @At(value = "INVOKE",
            target = "net/minecraft/server/MinecraftServer.isRunning ()Z"))
    private static boolean redirectIsRunning(MinecraftServer instance) {
        return instance.getSynchronizedServer().readExp(server -> server.isRunning());
    }
}
