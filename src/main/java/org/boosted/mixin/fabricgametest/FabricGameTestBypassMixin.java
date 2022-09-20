package org.boosted.mixin.fabricgametest;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructureManager;
import net.minecraft.test.StructureTestUtil;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import static net.minecraft.test.StructureTestUtil.testStructuresDirectoryName;

/**
 * fabric-gametest-api overrides createStructure entirely so there is nothing we can do other than bypass all calls to createStructure
 * and go back to vanilla logic for McTester
 */
@Mixin(StructureTestUtil.class)
public abstract class FabricGameTestBypassMixin {
	private static final String GAMETEST_STRUCTURE_PATH = "gametest/structures/";

	/**
	 * Override createStructure call and replace it with a call that is compatible with both fabric-gametest-api and McTester.
	 *
	 * @return
	 */
	@Redirect(method = "createStructure(Ljava/lang/String;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/BlockRotation;ILnet/minecraft/server/world/ServerWorld;Z)Lnet/minecraft/block/entity/StructureBlockBlockEntity;",
		at = @At(value = "INVOKE", target = "net/minecraft/test/StructureTestUtil.createStructure (Ljava/lang/String;Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/structure/Structure;"))
	private static Structure createStructureRedirect1(String structureId, ServerWorld world) {
		return createStructureFix(structureId, world);
	}

	/**
	 * Override createStructure call and replace it with a call that is compatible with both fabric-gametest-api and McTester.
	 *
	 * @return
	 */
	@Redirect(method = "placeStructure(Ljava/lang/String;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/BlockRotation;Lnet/minecraft/server/world/ServerWorld;Z)Lnet/minecraft/block/entity/StructureBlockBlockEntity;",
			at = @At(value = "INVOKE", target = "net/minecraft/test/StructureTestUtil.createStructure (Ljava/lang/String;Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/structure/Structure;"))
	private static Structure createStructureRedirect2(String structureId, ServerWorld world) {
		return createStructureFix(structureId, world);
	}

	private static Structure createStructureFix(String structureId, ServerWorld world) {
		Identifier baseId = new Identifier(structureId);
		Identifier structureIdFabric = new Identifier(baseId.getNamespace(), GAMETEST_STRUCTURE_PATH + baseId.getPath() + ".snbt");

		try {
			return StructureTestUtil.createStructure(structureId, world); // this uses fabric logic
		} catch (RuntimeException e) {
			StructureManager structureManager = world.getStructureManager();
			Optional<Structure> optional = structureManager.getStructure(new Identifier(structureId));
			if (optional.isPresent()) {
				return optional.get();
			}

			String string = structureId + ".snbt";
			Path path = Paths.get(testStructuresDirectoryName, string);
			NbtCompound nbtCompound = StructureTestUtil.loadSnbt(path);
			if (nbtCompound == null) {
				throw new RuntimeException("Fabric: Could not find structure file " + path + ", and the structure is not available in the world structures either." +
					"Vanilla: Error while trying to load structure: " + structureId, e);
			}
			return structureManager.createStructure(nbtCompound);
		}
	}
}
