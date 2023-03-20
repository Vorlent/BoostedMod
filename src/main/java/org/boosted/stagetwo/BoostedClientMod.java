package org.boosted.stagetwo;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import org.boosted.BoostedMod;
import org.boosted.stagetwo.gui.DashboardHandledScreen;

@Environment(EnvType.CLIENT)
public class BoostedClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(BoostedMod.DASHBOARD, DashboardHandledScreen::new);
    }
}