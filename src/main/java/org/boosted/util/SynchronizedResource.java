package org.boosted.util;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

public class SynchronizedResource<T, U> {
    private final T resource;
    private final U unmodifiableResource;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final ThreadLocal<Integer> readOnly = ThreadLocal.withInitial(() -> 0);

    public SynchronizedResource(T resource, U unmodifiableResource) {
        this.resource = resource;
        this.unmodifiableResource = unmodifiableResource;
    }

    public void read(Consumer<U> consumer) {
        lock.readLock().lock();
        readOnly.set(readOnly.get() + 1);
        try {
            consumer.accept(unmodifiableResource);
        } finally {
            readOnly.set(readOnly.get() - 1);
            lock.readLock().unlock();
        }
    }

    public <R> R read(Function<U, R> consumer) {
        lock.readLock().lock();
        readOnly.set(readOnly.get() + 1);
        R result = null;
        try {
            result = consumer.apply(unmodifiableResource);
        } finally {
            readOnly.set(readOnly.get() - 1);
            lock.readLock().unlock();
        }
        return result;
    }

    public <R> R write(Function<T, R> consumer) {
        if (readOnly.get() > 0) {
            throw new IllegalStateException("Cannot write while in read-only mode");
        }
        lock.writeLock().lock();
        R result = null;
        try {
            result = consumer.apply(resource);
        } finally {
            lock.writeLock().unlock();
        }
        return result;
    }

    public void write(Consumer<T> consumer) {
        if (readOnly.get() > 0) {
            throw new IllegalStateException("Cannot write while in read-only mode");
        }
        lock.writeLock().lock();
        try {
            consumer.accept(resource);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
