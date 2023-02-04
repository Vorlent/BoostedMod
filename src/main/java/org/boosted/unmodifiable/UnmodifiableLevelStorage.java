package org.boosted.unmodifiable;

import com.mojang.datafixers.DataFixer;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.level.storage.LevelStorage;

import java.io.IOException;
import java.nio.file.Path;

// TODO consider implementing more but this is just supposed to be a dummy

/**
 * Unfortunately the default constructor uses Files.createDirectories which does filesystem I/O in an unmodifiable class
 * that isn't supposed to have any side effects.
 */
public class UnmodifiableLevelStorage extends LevelStorage {
    public UnmodifiableLevelStorage(Path savesDirectory, Path backupsDirectory, DataFixer dataFixer) {
        super(savesDirectory, backupsDirectory, dataFixer);
    }

    public UnmodifiableLevelStorage() {
        super(Path.of("/"), null, null);
    }

    public Session createSession2(String directoryName) {
        try {
            return new DummySession(directoryName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    class DummySession extends LevelStorage.Session {
        public DummySession(String directoryName) throws IOException {
            super(directoryName);
        }

        @Override
        public WorldSaveHandler createSaveHandler() {
            return null;
        }
    }
}
