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
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collections;
import java.util.List;

@Mixin(AbstractBlock.class)
public abstract class AbstractBlockMixin {

	@Shadow public abstract Identifier getLootTableId();

	/**
	 * @author Vorlent
	 * @reason Wrap getServer().getLootManager() for exclusive read access to MinecraftServer
	 */
	@Overwrite
	public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
		Identifier identifier = getLootTableId();
		if (identifier == LootTables.EMPTY) {
			return Collections.emptyList();
		}
		LootContext lootContext = builder.parameter(LootContextParameters.BLOCK_STATE, state).build(LootContextTypes.BLOCK);
		// loot manager is mostly thread safe. Best option would be to make it and getLootManager() threadsafe
		// and then skip the getServer() call
		return lootContext.getWorld().getSynchronizedServer().readExp((server) -> {
			LootTable lootTable = server.getLootManager().getTable(identifier);
			return lootTable.generateLoot(lootContext);
		});
	}
}
