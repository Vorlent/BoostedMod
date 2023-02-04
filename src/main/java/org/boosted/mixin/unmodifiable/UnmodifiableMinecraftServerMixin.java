package org.boosted.mixin.unmodifiable;

import com.mojang.datafixers.DataFixer;
import net.minecraft.entity.boss.BossBarManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.DataPackContents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.ServerNetworkIo;
import net.minecraft.server.function.CommandFunctionManager;
import net.minecraft.server.function.FunctionLoader;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.MetricsData;
import net.minecraft.world.level.storage.LevelStorage;
import org.boosted.unmodifiable.UnmodifiableLevelStorage;
import org.boosted.unmodifiable.UnmodifiableMinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;

/**
 * Using new inside the constructor is just ugly...
 * The MinecraftServer instance creates a huge tree of instances and fabric doesn't let you simply inject new constructors,
 * so I am putting a huge amount of effort into accomplishing very little...
 * The bad Mojang API design strikes again. All I need is a better constructor in MinecraftServer...
 *
 * Fabric lets me redirect new calls which gives me at least some hope...
 */
@Mixin(MinecraftServer.class)
public class UnmodifiableMinecraftServerMixin {

	/*@Redirect(method = "<init>", at = @At(value = "NEW",
			target = "net/minecraft/server/ServerMetadata"))
	private ServerMetadata skipServerMetadata() {
		if ((Object)this instanceof UnmodifiableMinecraftServer) {
			return null;
		}
		return new ServerMetadata();
	}

	@Redirect(method = "<init>", at = @At(value = "NEW",
			target = "net/minecraft/scoreboard/ServerScoreboard"))
	private ServerScoreboard skipServerScoreboard(MinecraftServer server) {
		if ((Object)this instanceof UnmodifiableMinecraftServer) {
			return null;
		}
		return new ServerScoreboard(server);
	}

	@Redirect(method = "<init>", at = @At(value = "NEW",
			target = "net/minecraft/entity/boss/BossBarManager"))
	private BossBarManager skipBossBarManager() {
		if ((Object)this instanceof UnmodifiableMinecraftServer) {
			return null;
		}
		return new BossBarManager();
	}

	@Redirect(method = "<init>", at = @At(value = "NEW",
			target = "net/minecraft/util/MetricsData"))
	private MetricsData skipMetricsData() {
		if ((Object)this instanceof UnmodifiableMinecraftServer) {
			return null;
		}
		return new MetricsData();
	}

	@Redirect(method = "<init>", at = @At(value = "NEW",
			target = "net/minecraft/server/ServerNetworkIo"))
	private ServerNetworkIo skipServerNetworkIo(MinecraftServer server) {
		if ((Object)this instanceof UnmodifiableMinecraftServer) {
			return null;
		}
		return new ServerNetworkIo(server);
	}

	@Redirect(method = "<init>", at = @At(value = "NEW",
			target = "net/minecraft/server/function/CommandFunctionManager"))
	private CommandFunctionManager skipCommandFunctionManager(MinecraftServer server, FunctionLoader loader) {
		if ((Object)this instanceof UnmodifiableMinecraftServer) {
			return null;
		}
		return new CommandFunctionManager(server, loader);
	}

	@Redirect(method = "<init>", at = @At(value = "NEW",
			target = "net/minecraft/structure/StructureTemplateManager"))
	private StructureTemplateManager skipStructureTemplateManager(ResourceManager resourceManager,
																  LevelStorage.Session session, DataFixer dataFixer) {
		if ((Object)this instanceof UnmodifiableMinecraftServer) {
			return null;
		}
		return new StructureTemplateManager(resourceManager, session, dataFixer);
	}*/

	@Redirect(method = "<init>", at = @At(value = "INVOKE",
			target = "net/minecraft/server/DataPackContents.getFunctionLoader ()Lnet/minecraft/server/function/FunctionLoader;"))
	private FunctionLoader skipGetFunctionLoader(DataPackContents dataPackContents) {
		if ((Object)this instanceof UnmodifiableMinecraftServer) {
			return null;
		}
		return dataPackContents.getFunctionLoader();
	}
}
