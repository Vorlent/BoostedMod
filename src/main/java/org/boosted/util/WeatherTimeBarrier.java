package org.boosted.util;

import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * The weather and time logic of the overworld has to be executed first.
 * To maintain determinism, these parts of the other worlds are executed in order.s
 * To avoid copying and pasting mojang code, I have decided to use a more complicated way of synchronizing ServerWorld.tick()
 */
public class WeatherTimeBarrier {
    private final Map<World, Integer> worldToPhase = new ConcurrentHashMap<>();
    private int lastId = 0;
    private final List<CountDownLatch> latches = Collections.synchronizedList(new ArrayList<>());

    public void definePhase(World world) {
        latches.add(new CountDownLatch(1));
        worldToPhase.computeIfAbsent(world, (w) -> lastId++);
    }

    public void startPhase(World world) {
        int phase = worldToPhase.get(world);
        if (phase > 0) {
            try {
                latches.get(phase - 1).await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void endPhase(World world) {
        int phase = worldToPhase.get(world);
        latches.get(phase).countDown();
    }

    public void reset() {
        lastId = 0;
        latches.clear();
        worldToPhase.clear();
    }
}
