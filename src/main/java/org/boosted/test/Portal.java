package org.boosted.test;

import mctester.annotation.GameTest;
import mctester.common.test.creation.GameTestHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.DimensionType;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Portal {

    private static BlockPos getNetherTeleportTarget(BlockPos pos, ServerWorld origin, ServerWorld destination) {
        boolean destIsNether = destination.getRegistryKey() == World.NETHER;
        WorldBorder worldBorder = destination.getWorldBorder();
        double d = DimensionType.getCoordinateScaleFactor(origin.getDimension(), destination.getDimension());
        BlockPos blockPos2 = worldBorder.clamp(pos.getX() * d, pos.getY(), pos.getZ() * d);
        return destination.getPortalForcer().getPortalRect(blockPos2, destIsNether, worldBorder).map(rect -> {
            Direction.Axis axis;
            BlockPos lastNetherPortalPosition = rect.lowerLeft;
            BlockState blockState = origin.getBlockState(lastNetherPortalPosition);
            if (blockState.contains(Properties.HORIZONTAL_AXIS)) {
                axis = blockState.get(Properties.HORIZONTAL_AXIS);
            } else {
                axis = Direction.Axis.X;
            }

            return rect.lowerLeft;
        }).orElse(null);
    }

    public static void clearNetherPortal(BlockPos portalPos, World nether) {
        for (int x = -10; x < 10; x++) {
            for (int y = -2; y < 10; y++) {
                for (int z = -10; z < 10; z++) {
                    BlockPos pos = portalPos.add(x, y, z);
                    if (nether.getBlockState(pos).getBlock() != Blocks.OBSIDIAN && nether.getBlockState(pos).getBlock() != Blocks.NETHER_PORTAL) {
                        if ((-10 < x && x < 10 - 1) && (-2 < y && y < 10 - 1) && (-10 < z && z < 10 - 1)) {
                            nether.setBlockState(pos, Blocks.AIR.getDefaultState());
                        } else {
                            nether.setBlockState(pos, Blocks.NETHER_BRICKS.getDefaultState());
                        }
                    }
                }
            }
        }
    }

    /**
     * Send one cow through the portal
     */
    @GameTest
    public static void single(GameTestHelper helper) {
        CowEntity cowEntity = helper.spawnWithNoFreeWill(EntityType.COW, 2, 4, 2);
        cowEntity.setCustomName(Text.of("" + Math.random()));
        cowEntity.setCustomNameVisible(true);
        helper.walkTo(cowEntity, 3,2,4);
        helper.succeedWhen(() -> {
            MinecraftServer server = helper.gameTest.getWorld().getServer();
            ServerWorld nether = server.getWorld(World.NETHER);
            BlockPos gameTestPos = helper.gameTest.getPos();
            System.out.println("Overworld " + gameTestPos);

            BlockPos netherTeleportTarget = getNetherTeleportTarget(gameTestPos, helper.gameTest.getWorld(), nether);
            if (netherTeleportTarget == null) {
                helper.gameTest.fail(new IllegalStateException("No nether portal"));
                return false;
            }
            System.out.println("Portal: " + netherTeleportTarget);
            nether.setBlockState(netherTeleportTarget.add(1, -1, 1), Blocks.STRUCTURE_BLOCK.getDefaultState());
            clearNetherPortal(netherTeleportTarget, nether);
            Box box = Box.from(Vec3d.ofCenter(netherTeleportTarget)).expand(10);
            List<CowEntity> netherCows = nether.getEntitiesByType(EntityType.COW, box, (Entity entity) -> cowEntity.getCustomName().equals(entity.getCustomName()));
            netherCows.forEach(LivingEntity::kill); // get rid of cows
            List<Entity> netherEntities = nether.getEntitiesByClass(Entity.class, box, entity -> !cowEntity.getCustomName().equals(entity.getCustomName()));
            System.out.println("netherEntities: " + netherEntities);
            if (!netherCows.isEmpty()) {
                netherEntities.forEach(Entity::kill); // get rid of unrelated entities
            }
            return !netherCows.isEmpty() && netherEntities.isEmpty();
        });
    }

    /**
     * Send 100 cows through the portal
     */
    @GameTest
    public static void mass(GameTestHelper helper) {
        ArmorStandEntity cowsCounter = helper.spawnEntity(4, 2, 1, EntityType.ARMOR_STAND);
        cowsCounter.setCustomNameVisible(true);

        ArmorStandEntity expectedCounter = helper.spawnEntity( 4, 2, 2, EntityType.ARMOR_STAND);
        expectedCounter.setCustomNameVisible(true);

        ArmorStandEntity otherCounter = helper.spawnEntity( 1, 2, 1, EntityType.ARMOR_STAND);
        otherCounter.setCustomNameVisible(true);

        MinecraftServer server = helper.gameTest.getWorld().getServer();
        ServerWorld nether = server.getWorld(World.NETHER);
        BlockPos gameTestPos = helper.gameTest.getPos();
        Map<String, Entity> entityByName = new HashMap<>();

        helper.addAction(0, (gameTestHelper -> {
            BlockPos netherTeleportTarget = getNetherTeleportTarget(gameTestPos, helper.gameTest.getWorld(), nether);
            if (netherTeleportTarget == null) {
                helper.gameTest.fail(new IllegalStateException("No nether portal"));
                return;
            }
            Box box = Box.from(Vec3d.ofCenter(netherTeleportTarget)).expand(20);
            List<Entity> netherEntities = nether.getEntitiesByClass(Entity.class, box, entity -> !entity.hasCustomName() || entityByName.get(entity.getCustomName().getString()) == null);
             netherEntities.forEach(Entity::kill); // get rid of unrelated entities
        }));

        AtomicBoolean startChecking = new AtomicBoolean(false);
        Random random = new Random();
        long cowPrefix = random.nextLong();

        helper.addRepeatedAction((gameTestHelper, ticks) -> {
            if (0 < ticks && ticks <= 100) {
                CowEntity cowEntity = helper.spawnWithNoFreeWill(EntityType.COW, 3, 2, 4);
                cowEntity.setInvulnerable(true);
                cowEntity.setCustomNameVisible(true);
                cowEntity.setCustomName(Text.of("" + cowPrefix + ":" + entityByName.size()));
                entityByName.put(cowEntity.getCustomName().getString(), cowEntity);
                expectedCounter.setCustomName(Text.of("Expected: " + entityByName.size()));
            }
            if (ticks > 100) {
                startChecking.set(true);
            }
        });
        helper.succeedWhen(() -> {
            BlockPos netherTeleportTarget = getNetherTeleportTarget(gameTestPos, helper.gameTest.getWorld(), nether);
            if (netherTeleportTarget == null) {
                helper.gameTest.fail(new IllegalStateException("No nether portal"));
                return false;
            }
            //nether.setBlockState(netherTeleportTarget.add(1, -1, 1), Blocks.STRUCTURE_BLOCK.getDefaultState());
            clearNetherPortal(netherTeleportTarget, nether);
            Box box = Box.from(Vec3d.ofCenter(netherTeleportTarget)).expand(10);
            List<CowEntity> netherCows = nether.getEntitiesByType(EntityType.COW, box, (Entity entity) -> entity.hasCustomName() && entityByName.get(entity.getCustomName().getString()) != null);
            cowsCounter.setCustomName(Text.of("Cows: " + netherCows.size()));
            List<Entity> netherEntities = nether.getEntitiesByClass(Entity.class, box, entity -> !entity.hasCustomName() || entityByName.get(entity.getCustomName().getString()) == null);
            otherCounter.setCustomName(Text.of("Other: " + netherEntities.size()));
            boolean success = startChecking.get() && netherCows.size() == entityByName.size() && netherEntities.isEmpty();
            if (startChecking.get() && netherCows.size() == entityByName.size()) {
                netherCows.forEach(LivingEntity::kill); // get rid of cows
                netherEntities.forEach(Entity::kill); // get rid of unrelated entities
            }
            return success;
        });
    }

    private static int totalItemCount(Collection<ItemEntity> items) {
        return items.stream().map(item -> item.getStack().getCount()).reduce(0, (acc, curr) -> acc + curr);
    }

    /**
     * Send 100 arrows through the portal
     */
    @GameTest
    public static void items(GameTestHelper helper) {
        ArmorStandEntity itemsCounter = helper.spawnEntity(4, 2, 1, EntityType.ARMOR_STAND);
        itemsCounter.setCustomNameVisible(true);

        ArmorStandEntity expectedCounter = helper.spawnEntity( 4, 2, 2, EntityType.ARMOR_STAND);
        expectedCounter.setCustomNameVisible(true);

        ArmorStandEntity otherCounter = helper.spawnEntity( 1, 2, 1, EntityType.ARMOR_STAND);
        otherCounter.setCustomNameVisible(true);

        MinecraftServer server = helper.gameTest.getWorld().getServer();
        ServerWorld nether = server.getWorld(World.NETHER);
        BlockPos gameTestPos = helper.gameTest.getPos();
        Map<String, ItemEntity> entityByName = new HashMap<>();

        helper.addAction(0, (gameTestHelper -> {
            BlockPos netherTeleportTarget = getNetherTeleportTarget(gameTestPos, helper.gameTest.getWorld(), nether);
            if (netherTeleportTarget == null) {
                helper.gameTest.fail(new IllegalStateException("No nether portal"));
                return;
            }
            Box box = Box.from(Vec3d.ofCenter(netherTeleportTarget)).expand(20);
            List<Entity> netherEntities = nether.getEntitiesByClass(Entity.class, box, entity -> !entity.hasCustomName() || entityByName.get(entity.getCustomName().getString()) == null);
            netherEntities.forEach(Entity::kill); // get rid of unrelated entities
        }));

        AtomicBoolean startChecking = new AtomicBoolean(false);
        Random random = new Random();
        long testPrefix = random.nextInt(0, 10000);

        helper.addRepeatedAction((gameTestHelper, ticks) -> {
            if (0 < ticks && ticks <= 100) {
                ItemEntity itemEntity = helper.spawnEntity(3,2,4, EntityType.ITEM);
                itemEntity.setStack(new ItemStack(Items.ARROW, 1));

                itemEntity.setCustomNameVisible(true);
                itemEntity.setCustomName(Text.of("" + testPrefix + ":" + entityByName.size()));
                entityByName.put(itemEntity.getCustomName().getString(), itemEntity);
                expectedCounter.setCustomName(Text.of("Expected: " + entityByName.size()));
            }
            if (ticks > 100) {
                startChecking.set(true);
            }
        });
        helper.succeedWhen(() -> {
            BlockPos netherTeleportTarget = getNetherTeleportTarget(gameTestPos, helper.gameTest.getWorld(), nether);
            if (netherTeleportTarget == null) {
                helper.gameTest.fail(new IllegalStateException("No nether portal"));
                return false;
            }
            clearNetherPortal(netherTeleportTarget, nether);
            Box box = Box.from(Vec3d.ofCenter(netherTeleportTarget)).expand(10);
            List<ItemEntity> netherItems = nether.getEntitiesByType(EntityType.ITEM, box, (Entity entity) -> entity.hasCustomName() && entityByName.get(entity.getCustomName().getString()) != null);
            itemsCounter.setCustomName(Text.of("Items: " + totalItemCount(netherItems)));
            List<Entity> netherEntities = nether.getEntitiesByClass(Entity.class, box, entity -> !entity.hasCustomName() || entityByName.get(entity.getCustomName().getString()) == null);
            otherCounter.setCustomName(Text.of("Other: " + netherEntities.size()));
            boolean success = startChecking.get() && totalItemCount(netherItems) == totalItemCount(entityByName.values())  && netherItems.size() == 2 && netherEntities.isEmpty();
            if (startChecking.get() && totalItemCount(netherItems) == totalItemCount(entityByName.values()) && netherItems.size() == 2) {
                netherItems.forEach(Entity::kill); // get rid of items
                netherEntities.forEach(Entity::kill); // get rid of unrelated entities
            }
            return success;
        });
    }

    /**
     * Send different item types
     */
    @GameTest
    public static void itemtypes(GameTestHelper helper) {
        List<Item> cycledColors = Arrays.asList(Items.BLACK_CONCRETE, Items.RED_CONCRETE, Items.BLUE_CONCRETE, Items.YELLOW_CONCRETE, Items.GREEN_CONCRETE);

        ArmorStandEntity itemsCounter = helper.spawnEntity(4, 2, 1, EntityType.ARMOR_STAND);
        itemsCounter.setCustomNameVisible(true);

        ArmorStandEntity expectedCounter = helper.spawnEntity( 4, 2, 2, EntityType.ARMOR_STAND);
        expectedCounter.setCustomNameVisible(true);

        ArmorStandEntity otherCounter = helper.spawnEntity( 1, 2, 1, EntityType.ARMOR_STAND);
        otherCounter.setCustomNameVisible(true);

        MinecraftServer server = helper.gameTest.getWorld().getServer();
        ServerWorld nether = server.getWorld(World.NETHER);
        BlockPos gameTestPos = helper.gameTest.getPos();
        Map<String, ItemEntity> entityByName = new HashMap<>();

        helper.addAction(0, (gameTestHelper -> {
            BlockPos netherTeleportTarget = getNetherTeleportTarget(gameTestPos, helper.gameTest.getWorld(), nether);
            if (netherTeleportTarget == null) {
                helper.gameTest.fail(new IllegalStateException("No nether portal"));
                return;
            }
            Box box = Box.from(Vec3d.ofCenter(netherTeleportTarget)).expand(20);
            List<Entity> netherEntities = nether.getEntitiesByClass(Entity.class, box, entity -> !entity.hasCustomName() || entityByName.get(entity.getCustomName().getString()) == null);
            netherEntities.forEach(Entity::kill); // get rid of unrelated entities
        }));

        AtomicBoolean startChecking = new AtomicBoolean(false);
        Random random = new Random();
        long testPrefix = random.nextInt(0, 10000);
        final int[] colorIndex = {0};

        helper.addRepeatedAction((gameTestHelper, ticks) -> {
            if (0 < ticks && ticks <= 100) {
                ItemEntity itemEntity = helper.spawnEntity(3,2,4, EntityType.ITEM);
                itemEntity.setStack(new ItemStack(cycledColors.get(colorIndex[0]), 1));
                colorIndex[0] = (colorIndex[0] + 1) % cycledColors.size();

                itemEntity.setCustomNameVisible(true);
                itemEntity.setCustomName(Text.of("" + testPrefix + ":" + entityByName.size()));
                entityByName.put(itemEntity.getCustomName().getString(), itemEntity);
                expectedCounter.setCustomName(Text.of("Expected: " + entityByName.size()));
            }
            if (ticks > 100) {
                startChecking.set(true);
            }
        });
        helper.succeedWhen(() -> {
            BlockPos netherTeleportTarget = getNetherTeleportTarget(gameTestPos, helper.gameTest.getWorld(), nether);
            if (netherTeleportTarget == null) {
                helper.gameTest.fail(new IllegalStateException("No nether portal"));
                return false;
            }
            clearNetherPortal(netherTeleportTarget, nether);
            Box box = Box.from(Vec3d.ofCenter(netherTeleportTarget)).expand(10);
            List<ItemEntity> netherItems = nether.getEntitiesByType(EntityType.ITEM, box, (Entity entity) -> entity.hasCustomName() && entityByName.get(entity.getCustomName().getString()) != null);
            itemsCounter.setCustomName(Text.of("Items: " + totalItemCount(netherItems)));
            List<Entity> netherEntities = nether.getEntitiesByClass(Entity.class, box, entity -> !entity.hasCustomName() || entityByName.get(entity.getCustomName().getString()) == null);
            otherCounter.setCustomName(Text.of("Other: " + netherEntities.size()));
            boolean success = startChecking.get() && totalItemCount(netherItems) == totalItemCount(entityByName.values()) && netherItems.size() == cycledColors.size() && netherEntities.isEmpty();
            if (startChecking.get() && totalItemCount(netherItems) == totalItemCount(entityByName.values()) && netherItems.size() == cycledColors.size()) {
                netherItems.forEach(Entity::kill); // get rid of items
                netherEntities.forEach(Entity::kill); // get rid of unrelated entities
            }
            return success;
        });
    }
}

