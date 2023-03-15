package org.boosted.mixin.fabricgametest;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.resource.Resource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.test.StructureTestUtil;
import net.minecraft.util.Identifier;
import org.boosted.parallelized.ParallelServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

	/**
	 * Override createStructure call and replace it with a call that is compatible with both fabric-gametest-api and McTester.
	 *
	 * @return
	 */
	@Redirect(method = "createStructureTemplate(Ljava/lang/String;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/BlockRotation;ILnet/minecraft/server/world/ServerWorld;Z)Lnet/minecraft/block/entity/StructureBlockBlockEntity;",
		at = @At(value = "INVOKE", target = "net/minecraft/test/StructureTestUtil.createStructureTemplate (Ljava/lang/String;Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/structure/StructureTemplate;"))
	private static StructureTemplate boosted$createStructureTemplateRedirect1(String templateId, ServerWorld world) {
		return boosted$createStructureTemplateFix(templateId, world);
	}

	/**
	 * Override createStructure call and replace it with a call that is compatible with both fabric-gametest-api and McTester.
	 *
	 * @return
	 */
	@Redirect(method = "placeStructureTemplate(Ljava/lang/String;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/BlockRotation;Lnet/minecraft/server/world/ServerWorld;Z)Lnet/minecraft/block/entity/StructureBlockBlockEntity;",
			at = @At(value = "INVOKE", target = "net/minecraft/test/StructureTestUtil.createStructureTemplate (Ljava/lang/String;Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/structure/StructureTemplate;"))
	private static StructureTemplate  boosted$createStructureTemplateRedirect2(String templateId, ServerWorld world) {
		return boosted$createStructureTemplateFix(templateId, world);
	}

	private static StructureTemplate boosted$createStructureTemplateFix(String templateId, ServerWorld world) {
		System.out.println("ASD");
		try {
			try {
				System.out.println("ASD");
				if (world instanceof ParallelServerWorld parallelServerWorld) {
					parallelServerWorld.allowGetServer(); // allow a single getServer() call
				}
				return StructureTestUtil.createStructureTemplate(templateId, world);
			} finally {
				if (world instanceof ParallelServerWorld parallelServerWorld) {
					parallelServerWorld.disallowGetServer();
				}
			}
		} catch (RuntimeException e) { // this uses vanilla logic
			return world.getSynchronizedServer().readExp(server -> {
				StructureTemplateManager structureManager = server.getStructureTemplateManager();

				Optional<StructureTemplate> optional = structureManager.getTemplate(new Identifier(templateId));
				if (optional.isPresent()) {
					return optional.get();
				}

				Path path = Paths.get(testStructuresDirectoryName, templateId + ".snbt");
				NbtCompound nbtCompound = StructureTestUtil.loadSnbt(path);
				if (nbtCompound == null) {
					throw new RuntimeException("Fabric: Could not find structure file " + path + ", and the structure is not available in the world structures either." +
							"Vanilla: Error while trying to load structure: " + templateId, e);
				}
				return structureManager.createTemplate(nbtCompound);
			});
		}
	}
}
