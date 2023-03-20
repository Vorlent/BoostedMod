package org.boosted.stagetwo.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import org.boosted.BoostedMod;

public class DashboardScreenHandler extends ScreenHandler {
    public DashboardScreenHandler(int i, PlayerInventory playerInventory) {
        super(BoostedMod.DASHBOARD, i);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }


}
