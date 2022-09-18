package org.boosted.test;

import mctester.annotation.GameTest;
import mctester.common.test.creation.GameTestHelper;

import static net.minecraft.entity.EntityType.MINECART;

public class Diamond {
    @GameTest
    public static void turn(GameTestHelper helper) {
        helper.pressButton(0, 3, 1);
        helper.succeedWhenEntityPresent(MINECART, 3, 2, 2);
    }
}

