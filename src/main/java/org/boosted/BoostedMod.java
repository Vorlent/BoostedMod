package org.boosted;

import mctester.annotation.TestRegistryHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;
import org.boosted.stagetwo.gui.DashboardScreenHandler;
import org.boosted.test.Portal;
import org.boosted.test.Redstone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.server.command.CommandManager.literal;

@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
public class BoostedMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("boosted");

	// stage 2 specific
	public static final ScreenHandlerType<DashboardScreenHandler> DASHBOARD;
	static {
		DASHBOARD = Registry.register(Registry.SCREEN_HANDLER, "boosted-dashboard",
			new ScreenHandlerType<>(DashboardScreenHandler::new));
	}
	// stage 2 specific

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		TestRegistryHelper.shouldWarnOnMissingStructureFile = false;
		TestRegistryHelper.createTestsFromClass(Portal.class);
		TestRegistryHelper.createTestsFromClass(Redstone.class);

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("boosted")
			.executes(context -> {
				// For versions below 1.19, replace "Text.literal" with "new LiteralText".
				context.getSource().sendMessage(Text.literal("Called /boosted with no arguments"));

				context.getSource().getPlayer().openHandledScreen(new SimpleNamedScreenHandlerFactory(
					(syncId, inventory, player) ->
						new DashboardScreenHandler(syncId, inventory),
						Text.translatable("container.repair")));
				return 1;
			})));
	}
}
