package org.boosted.mixin.getServer;

import net.minecraft.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SignBlockEntity.class)
public class SignBlockEntityMixin {

   /** Unfortunnately not possible yet
   public ServerCommandSource getCommandSource(@Nullable ServerPlayerEntity player) {
        String string = player == null ? "Sign" : player.getName().getString();
        Text text = player == null ? Text.literal("Sign") : player.getDisplayName();
        return new ServerCommandSource(CommandOutput.DUMMY, Vec3d.ofCenter(this.pos), Vec2f.ZERO, (ServerWorld)this.world, 2, string, text, this.world.getServer(), player);
   }
   */
}
