package org.boosted.mixin.getServer;

import net.minecraft.command.EntitySelectorOptions;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntitySelectorOptions.class)
public class EntitySelectorOptionsMixin {

    /* This executes when the server starts. If this is necessary, we'll know it.
       public static void register() {
       }
     */
}
