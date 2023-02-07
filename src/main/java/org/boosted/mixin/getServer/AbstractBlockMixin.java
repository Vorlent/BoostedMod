package org.boosted.mixin.getServer;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collections;
import java.util.List;

@Mixin(AbstractBlock.class)
public class AbstractBlockMixin {

	@Deprecated
	public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
		Identifier identifier = ((AbstractBlock)(Object)this).getLootTableId();
		if (identifier == LootTables.EMPTY) {
			return Collections.emptyList();
		}
		LootContext lootContext = builder.parameter(LootContextParameters.BLOCK_STATE, state).build(LootContextTypes.BLOCK);
		return lootContext.getWorld().getSynchronizedServer().writeExp((server) -> {
			LootTable lootTable = server.getLootManager().getTable(identifier);
			return lootTable.generateLoot(lootContext);
		});
	}
}
