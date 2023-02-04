package org.boosted.mixin.unmodifiable;

import net.minecraft.world.level.storage.LevelStorage;
import org.boosted.unmodifiable.UnmodifiableLevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

@Mixin(LevelStorage.class)
public class UnmodifiableLevelStorageMixin {

	@Redirect(method = "<init>", at = @At(value = "INVOKE",
			target = "java/nio/file/Files.createDirectories (Ljava/nio/file/Path;[Ljava/nio/file/attribute/FileAttribute;)Ljava/nio/file/Path;"))
	private Path skipInitialization(Path savesDirectory, FileAttribute<?>[] x2) throws IOException {
		if ((Object)this instanceof UnmodifiableLevelStorage) {
			return null;
		}
		return Files.createDirectories(Files.exists(savesDirectory, new LinkOption[0]) ? savesDirectory.toRealPath(new LinkOption[0]) : savesDirectory, new FileAttribute[0]);
	}
}
