package org.boosted.util;

import java.util.function.Consumer;
import java.util.function.Function;

public interface SynchronizedResource<T, U> {
    void read(Consumer<U> consumer);

    <R> R readExp(Function<U, R> consumer);

    <R> R writeExp(Function<T, R> consumer);

    void write(Consumer<T> consumer);
}


