package org.boosted.unmodifiable;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryEntryList;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class DummyRegistry extends Registry<DimensionOptions> {

    public static final DimensionOptions DUMMY_DIMENSION_OPTIONS = new DimensionOptions(null, null);

    protected DummyRegistry(RegistryKey<? extends Registry<DimensionOptions>> key, Lifecycle lifecycle) {
        super(key, lifecycle);
    }

    public DummyRegistry() {
        super(null, null);
    }

    @Nullable
    @Override
    public Identifier getId(DimensionOptions value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<RegistryKey<DimensionOptions>> getKey(DimensionOptions entry) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getRawId(@Nullable DimensionOptions value) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public DimensionOptions get(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public DimensionOptions get(@Nullable RegistryKey<DimensionOptions> key) {
        return DUMMY_DIMENSION_OPTIONS;
    }

    @Nullable
    @Override
    public DimensionOptions get(@Nullable Identifier id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Lifecycle getEntryLifecycle(DimensionOptions entry) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Lifecycle getLifecycle() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Identifier> getIds() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Map.Entry<RegistryKey<DimensionOptions>, DimensionOptions>> getEntrySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<RegistryKey<DimensionOptions>> getKeys() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<RegistryEntry<DimensionOptions>> getRandom(Random random) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsId(Identifier id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(RegistryKey<DimensionOptions> key) {
        return true;
    }

    @Override
    public Registry<DimensionOptions> freeze() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RegistryEntry<DimensionOptions> getOrCreateEntry(RegistryKey<DimensionOptions> key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DataResult<RegistryEntry<DimensionOptions>> getOrCreateEntryDataResult(RegistryKey<DimensionOptions> key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RegistryEntry.Reference<DimensionOptions> createEntry(DimensionOptions value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<RegistryEntry<DimensionOptions>> getEntry(int rawId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<RegistryEntry<DimensionOptions>> getEntry(RegistryKey<DimensionOptions> key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<RegistryEntry.Reference<DimensionOptions>> streamEntries() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<RegistryEntryList.Named<DimensionOptions>> getEntryList(TagKey<DimensionOptions> tag) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RegistryEntryList.Named<DimensionOptions> getOrCreateEntryList(TagKey<DimensionOptions> tag) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<Pair<TagKey<DimensionOptions>, RegistryEntryList.Named<DimensionOptions>>> streamTagsAndEntries() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<TagKey<DimensionOptions>> streamTags() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsTag(TagKey<DimensionOptions> tag) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearTags() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void populateTags(Map<TagKey<DimensionOptions>, List<RegistryEntry<DimensionOptions>>> tagEntries) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Iterator<DimensionOptions> iterator() {
        throw new UnsupportedOperationException();
    }
}
