package org.boosted.util;

import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The weather and time logic of the overworld has to be executed first.
 * To maintain determinism, these parts of the other worlds are executed in order.
 *
 * To avoid copying and pasting mojang code, I have decided to use a more complicated way of synchronizing ServerWorld.tick()
 */
public class WorldWeatherTimeBarrier {

    private Map<World, Integer> worldToPhase = new ConcurrentHashMap<>();
    private int lastId = 0;
    private List<CountDownLatch> latches = Collections.synchronizedList(new ArrayList<>());

    public void definePhase(World world) {
        System.out.println("Define phase for world: " + world);
        latches.add(new CountDownLatch(1));
        worldToPhase.computeIfAbsent(world, (w) -> lastId++);
        System.out.println("worlds " + worldToPhase);
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
