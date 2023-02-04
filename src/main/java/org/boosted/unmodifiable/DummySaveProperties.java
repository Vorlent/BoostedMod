package org.boosted.unmodifiable;

import com.mojang.serialization.Lifecycle;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.ServerWorldProperties;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class DummySaveProperties implements SaveProperties {
    @Override
    public DataPackSettings getDataPackSettings() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateLevelInfo(DataPackSettings dataPackSettings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isModded() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getServerBrands() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addServerBrand(String brand, boolean modded) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void populateCrashReport(CrashReportSection section) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getFormatName(int id) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public NbtCompound getCustomBossEvents() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCustomBossEvents(@Nullable NbtCompound customBossEvents) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServerWorldProperties getMainWorldProperties() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LevelInfo getLevelInfo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NbtCompound cloneWorldNbt(DynamicRegistryManager registryManager, @Nullable NbtCompound playerNbt) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isHardcore() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLevelName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public GameMode getGameMode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setGameMode(GameMode gameMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean areCommandsAllowed() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Difficulty getDifficulty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDifficulty(Difficulty difficulty) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDifficultyLocked() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDifficultyLocked(boolean difficultyLocked) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GameRules getGameRules() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public NbtCompound getPlayerData() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NbtCompound getDragonFight() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDragonFight(NbtCompound dragonFight) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GeneratorOptions getGeneratorOptions() {
        return new GeneratorOptions(0, false, false, new DummyRegistry());
    }

    @Override
    public Lifecycle getLifecycle() {
        throw new UnsupportedOperationException();
    }
}
