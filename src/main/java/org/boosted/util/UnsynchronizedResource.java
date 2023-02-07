package org.boosted.util;

import java.util.function.Consumer;
import java.util.function.Function;

public class UnsynchronizedResource<T, U> implements SynchronizedResource<T, U> {
    private final T resource;
    private final U unmodifiableResource;

    public UnsynchronizedResource(T resource, U unmodifiableResource) {
        this.resource = resource;
        this.unmodifiableResource = unmodifiableResource;
    }

    public void read(Consumer<U> consumer) {
        consumer.accept(unmodifiableResource);
    }

    public <R> R readExp(Function<U, R> consumer) {
        return consumer.apply(unmodifiableResource);
    }

    public <R> R writeExp(Function<T, R> consumer) {
        return consumer.apply(resource);
    }

    public void write(Consumer<T> consumer) {
        consumer.accept(resource);
    }
}



