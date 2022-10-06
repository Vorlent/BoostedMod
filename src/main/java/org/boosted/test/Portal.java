package org.boosted.test;

import com.mojang.authlib.GameProfile;
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
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.DimensionType;
import org.boosted.ThreadCoordinator;
import org.boosted.util.FakePlayerClientConnection;

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

    /**
     * Send arrows
     */
    @GameTest
    public static void arrows(GameTestHelper helper) {
        ArmorStandEntity arrowsCounter = helper.spawnEntity(4, 2, 1, EntityType.ARMOR_STAND);
        arrowsCounter.setCustomNameVisible(true);

        ArmorStandEntity expectedCounter = helper.spawnEntity( 4, 2, 2, EntityType.ARMOR_STAND);
        expectedCounter.setCustomNameVisible(true);

        ArmorStandEntity otherCounter = helper.spawnEntity( 1, 2, 1, EntityType.ARMOR_STAND);
        otherCounter.setCustomNameVisible(true);

        MinecraftServer server = helper.gameTest.getWorld().getServer();
        ServerWorld nether = server.getWorld(World.NETHER);
        BlockPos gameTestPos = helper.gameTest.getPos();
        Map<String, ArrowEntity> entityByName = new HashMap<>();

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
                ArrowEntity arrowEntity = helper.spawnEntity(3,3,3, EntityType.ARROW);
                arrowEntity.setVelocity(0.0f, 0.0f, 0.5f);

                arrowEntity.setCustomNameVisible(true);
                arrowEntity.setCustomName(Text.of("" + testPrefix + ":" + entityByName.size()));
                entityByName.put(arrowEntity.getCustomName().getString(), arrowEntity);
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
            List<ArrowEntity> netherArrows = nether.getEntitiesByType(EntityType.ARROW, box, (Entity entity) -> entity.hasCustomName() && entityByName.get(entity.getCustomName().getString()) != null);
            arrowsCounter.setCustomName(Text.of("Arrows: " + netherArrows.size()));
            List<Entity> netherEntities = nether.getEntitiesByClass(Entity.class, box, entity -> !entity.hasCustomName() || entityByName.get(entity.getCustomName().getString()) == null);
            otherCounter.setCustomName(Text.of("Other: " + netherEntities.size()));
            boolean success = startChecking.get() && netherArrows.size() == entityByName.size() && netherEntities.isEmpty();
            if (startChecking.get() && netherArrows.size() == entityByName.size()) {
                netherArrows.forEach(Entity::kill); // get rid of items
                netherEntities.forEach(Entity::kill); // get rid of unrelated entities
            }
            return success;
        });
    }

    private static int totalMinecartItemCount(Collection<ChestMinecartEntity> items) {
        return items.stream().map(item -> item.getStack(0).getCount()).reduce(0, (acc, curr) -> acc + curr);
    }

    /**
     * Send entities that contain items
     */
    @GameTest
    public static void minecartchest(GameTestHelper helper) {
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
        Map<String, ChestMinecartEntity> entityByName = new HashMap<>();

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
                ChestMinecartEntity minecartEntity = helper.spawnEntity(3,2,4, EntityType.CHEST_MINECART);
                minecartEntity.setStack(0, new ItemStack(cycledColors.get(colorIndex[0]), 1));
                colorIndex[0] = (colorIndex[0] + 1) % cycledColors.size();

                minecartEntity.setCustomNameVisible(true);
                minecartEntity.setCustomName(Text.of("" + testPrefix + ":" + entityByName.size()));
                entityByName.put(minecartEntity.getCustomName().getString(), minecartEntity);
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
            List<ChestMinecartEntity> netherMinecarts = nether.getEntitiesByType(EntityType.CHEST_MINECART, box, (Entity entity) -> entity.hasCustomName() && entityByName.get(entity.getCustomName().getString()) != null);
            itemsCounter.setCustomName(Text.of("Items: " + totalMinecartItemCount(netherMinecarts)));
            List<Entity> netherEntities = nether.getEntitiesByClass(Entity.class, box, entity -> !entity.hasCustomName() || entityByName.get(entity.getCustomName().getString()) == null);
            otherCounter.setCustomName(Text.of("Other: " + netherEntities.size()));
            boolean success = startChecking.get() && totalMinecartItemCount(netherMinecarts) == totalMinecartItemCount(entityByName.values()) && netherMinecarts.size() == entityByName.size() && netherEntities.isEmpty();
            if (startChecking.get() && totalMinecartItemCount(netherMinecarts) == totalMinecartItemCount(entityByName.values()) && netherMinecarts.size() == entityByName.size()) {
                netherMinecarts.forEach(Entity::kill); // get rid of items
                netherEntities.forEach(Entity::kill); // get rid of unrelated entities
            }
            return success;
        });
    }

    /**
     * Send entities from the overworld to the nether and from the nether to the overworld at the same time
     */
    @GameTest
    public static void duplex(GameTestHelper helper) {
        ArmorStandEntity netherCowsCounter = helper.spawnEntity(3, 2, 1, EntityType.ARMOR_STAND);
        netherCowsCounter.setCustomNameVisible(true);

        ArmorStandEntity overworldCowsCounter = helper.spawnEntity(4, 2, 1, EntityType.ARMOR_STAND);
        overworldCowsCounter.setCustomNameVisible(true);

        ArmorStandEntity expectedNetherworldCounter = helper.spawnEntity( 4, 2, 2, EntityType.ARMOR_STAND);
        expectedNetherworldCounter.setCustomNameVisible(true);

        ArmorStandEntity expectedOverworldCounter = helper.spawnEntity( 4, 2, 3, EntityType.ARMOR_STAND);
        expectedOverworldCounter.setCustomNameVisible(true);

        ArmorStandEntity otherNetherCounter = helper.spawnEntity( 1, 2, 1, EntityType.ARMOR_STAND);
        otherNetherCounter.setCustomNameVisible(true);

        ArmorStandEntity otherOverworldCounter = helper.spawnEntity( 1, 2, 2, EntityType.ARMOR_STAND);
        otherOverworldCounter.setCustomNameVisible(true);

        MinecraftServer server = helper.gameTest.getWorld().getServer();
        ServerWorld overworld = server.getOverworld();
        ServerWorld nether = server.getWorld(World.NETHER);
        BlockPos gameTestPos = helper.gameTest.getPos();
        Map<String, Entity> netherEntityByName = new HashMap<>();
        Map<String, Entity> overworldEntityByName = new HashMap<>();

        helper.addAction(0, (gameTestHelper -> {
            BlockPos netherTeleportTarget = getNetherTeleportTarget(gameTestPos, helper.gameTest.getWorld(), nether);
            if (netherTeleportTarget == null) {
                helper.gameTest.fail(new IllegalStateException("No nether portal"));
                return;
            }
            Box netherBox = Box.from(Vec3d.ofCenter(netherTeleportTarget)).expand(20);
            List<Entity> netherEntities = nether.getEntitiesByClass(Entity.class, netherBox, entity -> !entity.hasCustomName()
                    || (netherEntityByName.get(entity.getCustomName().getString()) == null
                    && overworldEntityByName.get(entity.getCustomName().getString()) == null));
            netherEntities.forEach(Entity::kill); // get rid of unrelated entities

            BlockPos overworldTeleportTarget = getNetherTeleportTarget(netherTeleportTarget, nether, helper.gameTest.getWorld());
            if (overworldTeleportTarget == null) {
                helper.gameTest.fail(new IllegalStateException("No overworld portal"));
                return;
            }
            Box overworldBox = Box.from(Vec3d.ofCenter(overworldTeleportTarget)).expand(20);
            List<Entity> overworldEntities = overworld.getEntitiesByClass(Entity.class, overworldBox, entity -> !entity.hasCustomName()
                    || (netherEntityByName.get(entity.getCustomName().getString()) == null
                    && overworldEntityByName.get(entity.getCustomName().getString()) == null));
            overworldEntities.forEach(Entity::kill); // get rid of unrelated entities
        }));

        AtomicBoolean startChecking = new AtomicBoolean(false);
        Random random = new Random();
        long cowPrefix = random.nextLong();

        helper.addRepeatedAction((gameTestHelper, ticks) -> {
            if (0 < ticks && ticks <= 100) {
                ThreadCoordinator.getInstance().getBoostedContext(overworld).postTick().executeTask(() -> {
                    CowEntity cowEntity = helper.spawnWithNoFreeWill(EntityType.COW, 3, 2, 4);
                    cowEntity.setInvulnerable(true);
                    cowEntity.setCustomNameVisible(true);
                    cowEntity.setCustomName(Text.of("" + cowPrefix + ".overworld:" + overworldEntityByName.size()));
                    overworldEntityByName.put(cowEntity.getCustomName().getString(), cowEntity);
                    expectedNetherworldCounter.setCustomName(Text.of("Expected (in Nether): " + overworldEntityByName.size()));
                });

                BlockPos netherTeleportTarget = getNetherTeleportTarget(gameTestPos, helper.gameTest.getWorld(), nether);
                if (netherTeleportTarget == null) {
                    helper.gameTest.fail(new IllegalStateException("No nether portal"));
                    return;
                }
                ThreadCoordinator.getInstance().getBoostedContext(nether).postTick().executeTask(() -> {
                    CowEntity netherCow = EntityType.COW.create(nether);
                    netherCow.setPosition(netherTeleportTarget.getX(), netherTeleportTarget.getY() + 1,
                            netherTeleportTarget.getZ());
                    netherCow.setInvulnerable(true);
                    netherCow.setCustomNameVisible(true);
                    netherCow.setCustomName(Text.of("" + cowPrefix + ".nether:" + netherEntityByName.size()));
                    nether.spawnEntity(netherCow);
                    netherEntityByName.put(netherCow.getCustomName().getString(), netherCow);
                    expectedOverworldCounter.setCustomName(Text.of("Expected (in Overworld): " + netherEntityByName.size()));
                });
            }
            if (ticks > 105) { // boosted adds one tick delay
                startChecking.set(true);
            }
        });
        helper.succeedWhen(() -> {
            BlockPos netherTeleportTarget = getNetherTeleportTarget(gameTestPos, helper.gameTest.getWorld(), nether);
            if (netherTeleportTarget == null) {
                helper.gameTest.fail(new IllegalStateException("No nether portal"));
                return false;
            }
            BlockPos overworldTeleportTarget = getNetherTeleportTarget(netherTeleportTarget, nether, helper.gameTest.getWorld());
            if (overworldTeleportTarget == null) {
                helper.gameTest.fail(new IllegalStateException("No overworld portal"));
                return false;
            }

            clearNetherPortal(netherTeleportTarget, nether);
            Box netherBox = Box.from(Vec3d.ofCenter(netherTeleportTarget)).expand(10);
            List<CowEntity> netherCows = nether.getEntitiesByType(EntityType.COW, netherBox, (Entity entity) -> entity.hasCustomName()
                    && overworldEntityByName.get(entity.getCustomName().getString()) != null);
            netherCowsCounter.setCustomName(Text.of("Nether Cows: " + netherCows.size()));
            List<Entity> netherEntities = nether.getEntitiesByClass(Entity.class, netherBox, entity -> !entity.hasCustomName()
                    || (netherEntityByName.get(entity.getCustomName().getString()) == null
                    && overworldEntityByName.get(entity.getCustomName().getString()) == null));
            otherNetherCounter.setCustomName(Text.of("Other (Nether): " + netherEntities.size()));

            Box overworldBox = Box.from(Vec3d.ofCenter(overworldTeleportTarget)).expand(10);
            List<CowEntity> overworldCows = overworld.getEntitiesByType(EntityType.COW, overworldBox, (Entity entity) -> entity.hasCustomName()
                    && netherEntityByName.get(entity.getCustomName().getString()) != null);
            overworldCowsCounter.setCustomName(Text.of("Overworld Cows: " + overworldCows.size()));
            List<Entity> overworldEntities = overworld.getEntitiesByClass(Entity.class, overworldBox, entity -> !entity.hasCustomName()
                    || (netherEntityByName.get(entity.getCustomName().getString()) == null
                    && overworldEntityByName.get(entity.getCustomName().getString()) == null));
            otherOverworldCounter.setCustomName(Text.of("Other (Overworld): " + overworldEntities.size()));

            boolean success = startChecking.get() && netherCows.size() == netherEntityByName.size()
                    && overworldCows.size() == netherEntityByName.size()
                    && netherCows.size() == overworldEntityByName.size();

            if (success) {
                netherCows.forEach(LivingEntity::kill); // get rid of cows
                netherEntities.forEach(Entity::kill); // get rid of unrelated entities
                overworldCows.forEach(LivingEntity::kill); // get rid of cows
                overworldEntities.forEach(Entity::kill); // get rid of unrelated entities
            }
            return success
                && netherEntities.isEmpty()
                && overworldEntities.isEmpty();
        });
    }

    /**
     * Send items from the overworld to the nether and from the nether to the overworld at the same time.
     * Item merging will occur which means the item quantities in the nether and overworld will be unbalanced,
     * this is intentional because other mods would start duping as item merging is executed by the teleported item.
     * If an item from the overworld is sent to the nether, it will merge with nether items but the merging code in the
     * nether would be executed on the overworld thread. If the nether thread attempts its own item merging, then dupes
     * are possible although rare.
     */
    @GameTest
    public static void duplexitems(GameTestHelper helper) {
        ArmorStandEntity netherItemsCounter = helper.spawnEntity(3, 2, 1, EntityType.ARMOR_STAND);
        netherItemsCounter.setCustomNameVisible(true);

        ArmorStandEntity overworldItemsCounter = helper.spawnEntity(4, 2, 1, EntityType.ARMOR_STAND);
        overworldItemsCounter.setCustomNameVisible(true);

        ArmorStandEntity expectedNetherworldCounter = helper.spawnEntity( 4, 2, 2, EntityType.ARMOR_STAND);
        expectedNetherworldCounter.setCustomNameVisible(true);

        ArmorStandEntity expectedOverworldCounter = helper.spawnEntity( 4, 2, 3, EntityType.ARMOR_STAND);
        expectedOverworldCounter.setCustomNameVisible(true);

        ArmorStandEntity otherNetherCounter = helper.spawnEntity( 1, 2, 1, EntityType.ARMOR_STAND);
        otherNetherCounter.setCustomNameVisible(true);

        ArmorStandEntity otherOverworldCounter = helper.spawnEntity( 1, 2, 2, EntityType.ARMOR_STAND);
        otherOverworldCounter.setCustomNameVisible(true);

        MinecraftServer server = helper.gameTest.getWorld().getServer();
        ServerWorld overworld = server.getOverworld();
        ServerWorld nether = server.getWorld(World.NETHER);
        BlockPos gameTestPos = helper.gameTest.getPos();
        Map<String, ItemEntity> netherEntityByName = new HashMap<>();
        Map<String, ItemEntity> overworldEntityByName = new HashMap<>();

        helper.addAction(0, (gameTestHelper -> {
            BlockPos netherTeleportTarget = getNetherTeleportTarget(gameTestPos, helper.gameTest.getWorld(), nether);
            if (netherTeleportTarget == null) {
                helper.gameTest.fail(new IllegalStateException("No nether portal"));
                return;
            }
            Box netherBox = Box.from(Vec3d.ofCenter(netherTeleportTarget)).expand(20);
            List<Entity> netherEntities = nether.getEntitiesByClass(Entity.class, netherBox, entity -> !entity.hasCustomName()
                    || (netherEntityByName.get(entity.getCustomName().getString()) == null
                    && overworldEntityByName.get(entity.getCustomName().getString()) == null));
            netherEntities.forEach(Entity::kill); // get rid of unrelated entities

            BlockPos overworldTeleportTarget = getNetherTeleportTarget(netherTeleportTarget, nether, helper.gameTest.getWorld());
            if (overworldTeleportTarget == null) {
                helper.gameTest.fail(new IllegalStateException("No overworld portal"));
                return;
            }
            Box overworldBox = Box.from(Vec3d.ofCenter(overworldTeleportTarget)).expand(20);
            List<Entity> overworldEntities = overworld.getEntitiesByClass(Entity.class, overworldBox, entity -> !entity.hasCustomName()
                    || (netherEntityByName.get(entity.getCustomName().getString()) == null
                    && overworldEntityByName.get(entity.getCustomName().getString()) == null));
            overworldEntities.forEach(Entity::kill); // get rid of unrelated entities
        }));

        AtomicBoolean startChecking = new AtomicBoolean(false);
        Random random = new Random();
        long cowPrefix = random.nextLong();

        helper.addRepeatedAction((gameTestHelper, ticks) -> {
            if (0 < ticks && ticks <= 100) {
                ThreadCoordinator.getInstance().getBoostedContext(overworld).postTick().executeTask(() -> {
                    ItemEntity overworldEntity = helper.spawnEntity(3, 3, 3, EntityType.ITEM);
                    overworldEntity.setVelocity(0.0,0.0,0.1);
                    overworldEntity.setStack(new ItemStack(Items.ARROW, 1));
                    overworldEntity.setInvulnerable(true);
                    overworldEntity.setCustomNameVisible(true);
                    overworldEntity.setCustomName(Text.of("" + cowPrefix + ".overworld:" + overworldEntityByName.size()));
                    overworldEntityByName.put(overworldEntity.getCustomName().getString(), overworldEntity);
                    expectedNetherworldCounter.setCustomName(Text.of("Expected (in Nether): " + overworldEntityByName.size()));
                });

                BlockPos netherTeleportTarget = getNetherTeleportTarget(gameTestPos, helper.gameTest.getWorld(), nether);
                if (netherTeleportTarget == null) {
                    helper.gameTest.fail(new IllegalStateException("No nether portal"));
                    return;
                }
                ThreadCoordinator.getInstance().getBoostedContext(nether).postTick().executeTask(() -> {
                    ItemEntity netherEntity = EntityType.ITEM.create(nether);
                    netherEntity.setStack(new ItemStack(Items.ARROW, 1));
                    netherEntity.setPosition(netherTeleportTarget.getX() + 1, netherTeleportTarget.getY() + 1, netherTeleportTarget.getZ() + 2);
                    netherEntity.setVelocity(0.0,0.0,-0.2);
                    netherEntity.setInvulnerable(true);
                    netherEntity.setCustomNameVisible(true);
                    netherEntity.setCustomName(Text.of("" + cowPrefix + ".nether:" + netherEntityByName.size()));
                    netherEntityByName.put(netherEntity.getCustomName().getString(), netherEntity);
                    nether.spawnEntity(netherEntity);
                    expectedOverworldCounter.setCustomName(Text.of("Expected (in Overworld): " + netherEntityByName.size()));
                });
            }
            if (ticks > 105) { // boosted adds one tick delay
                startChecking.set(true);
            }
        });
        helper.succeedWhen(() -> {
            BlockPos netherTeleportTarget = getNetherTeleportTarget(gameTestPos, helper.gameTest.getWorld(), nether);
            if (netherTeleportTarget == null) {
                helper.gameTest.fail(new IllegalStateException("No nether portal"));
                return false;
            }
            BlockPos overworldTeleportTarget = getNetherTeleportTarget(netherTeleportTarget, nether, helper.gameTest.getWorld());
            if (overworldTeleportTarget == null) {
                helper.gameTest.fail(new IllegalStateException("No overworld portal"));
                return false;
            }

            clearNetherPortal(netherTeleportTarget, nether);
            Box netherBox = Box.from(Vec3d.ofCenter(netherTeleportTarget)).expand(10);
            List<ItemEntity> netherItems = nether.getEntitiesByType(EntityType.ITEM, netherBox, (Entity entity) -> entity.hasCustomName()
                    && overworldEntityByName.get(entity.getCustomName().getString()) != null);
            netherItemsCounter.setCustomName(Text.of("Nether items: " + totalItemCount(netherItems)));
            List<Entity> netherEntities = nether.getEntitiesByClass(Entity.class, netherBox, entity -> !entity.hasCustomName()
                    || (netherEntityByName.get(entity.getCustomName().getString()) == null
                    && overworldEntityByName.get(entity.getCustomName().getString()) == null));
            otherNetherCounter.setCustomName(Text.of("Other (Nether): " + netherEntities.size()));

            Box overworldBox = Box.from(Vec3d.ofCenter(overworldTeleportTarget)).expand(10);
            List<ItemEntity> overworldItems = overworld.getEntitiesByType(EntityType.ITEM, overworldBox, (Entity entity) -> entity.hasCustomName()
                    && netherEntityByName.get(entity.getCustomName().getString()) != null);
            overworldItemsCounter.setCustomName(Text.of("Overworld items: " + totalItemCount(overworldItems)));
            List<Entity> overworldEntities = overworld.getEntitiesByClass(Entity.class, overworldBox, entity -> !entity.hasCustomName()
                    || (netherEntityByName.get(entity.getCustomName().getString()) == null
                    && overworldEntityByName.get(entity.getCustomName().getString()) == null));
            otherOverworldCounter.setCustomName(Text.of("Other (Overworld): " + overworldEntities.size()));

            boolean success = startChecking.get()
                    && totalItemCount(netherItems) + totalItemCount(overworldItems)
                    == netherEntityByName.size() + overworldEntityByName.size();

            if (success) {
                netherItems.forEach(Entity::kill); // get rid of items
                netherEntities.forEach(Entity::kill); // get rid of unrelated entities
                overworldItems.forEach(Entity::kill); // get rid of items
                overworldEntities.forEach(Entity::kill); // get rid of unrelated entities
            }
            return success
                    && netherEntities.isEmpty()
                    && overworldEntities.isEmpty();
        });
    }

    /**
     * Send fake players through the portal
     */
    @GameTest
    public static void fakeplayers(GameTestHelper helper) {
        ArmorStandEntity netherItemsCounter = helper.spawnEntity(3, 2, 1, EntityType.ARMOR_STAND);
        netherItemsCounter.setCustomNameVisible(true);

        ArmorStandEntity overworldItemsCounter = helper.spawnEntity(4, 2, 1, EntityType.ARMOR_STAND);
        overworldItemsCounter.setCustomNameVisible(true);

        ArmorStandEntity expectedNetherworldCounter = helper.spawnEntity( 4, 2, 2, EntityType.ARMOR_STAND);
        expectedNetherworldCounter.setCustomNameVisible(true);

        ArmorStandEntity expectedOverworldCounter = helper.spawnEntity( 4, 2, 3, EntityType.ARMOR_STAND);
        expectedOverworldCounter.setCustomNameVisible(true);

        ArmorStandEntity otherNetherCounter = helper.spawnEntity( 1, 2, 1, EntityType.ARMOR_STAND);
        otherNetherCounter.setCustomNameVisible(true);

        ArmorStandEntity otherOverworldCounter = helper.spawnEntity( 1, 2, 2, EntityType.ARMOR_STAND);
        otherOverworldCounter.setCustomNameVisible(true);

        MinecraftServer server = helper.gameTest.getWorld().getServer();
        ServerWorld overworld = server.getOverworld();
        ServerWorld nether = server.getWorld(World.NETHER);
        BlockPos gameTestPos = helper.gameTest.getPos();
        Map<String, ServerPlayerEntity> netherEntityByName = new HashMap<>();
        Map<String, ServerPlayerEntity> overworldEntityByName = new HashMap<>();

        helper.addAction(0, (gameTestHelper -> {
            BlockPos netherTeleportTarget = getNetherTeleportTarget(gameTestPos, helper.gameTest.getWorld(), nether);
            if (netherTeleportTarget == null) {
                helper.gameTest.fail(new IllegalStateException("No nether portal"));
                return;
            }
            Box netherBox = Box.from(Vec3d.ofCenter(netherTeleportTarget)).expand(20);
            List<Entity> netherEntities = nether.getEntitiesByClass(Entity.class, netherBox, entity -> !entity.hasCustomName()
                    || (netherEntityByName.get(entity.getEntityName()) == null
                    && overworldEntityByName.get(entity.getEntityName()) == null));
            netherEntities.forEach(Entity::kill); // get rid of unrelated entities

            BlockPos overworldTeleportTarget = getNetherTeleportTarget(netherTeleportTarget, nether, helper.gameTest.getWorld());
            if (overworldTeleportTarget == null) {
                helper.gameTest.fail(new IllegalStateException("No overworld portal"));
                return;
            }
            Box overworldBox = Box.from(Vec3d.ofCenter(overworldTeleportTarget)).expand(20);
            List<Entity> overworldEntities = overworld.getEntitiesByClass(Entity.class, overworldBox, entity -> !entity.hasCustomName()
                    || (netherEntityByName.get(entity.getEntityName()) == null
                    && overworldEntityByName.get(entity.getEntityName()) == null));
            overworldEntities.forEach(Entity::kill); // get rid of unrelated entities
        }));

        AtomicBoolean startChecking = new AtomicBoolean(false);
        Random random = new Random();
        long testPrefix = random.nextLong(0, 1000);

        helper.addRepeatedAction((gameTestHelper, ticks) -> {
            if (0 < ticks && ticks <= 1) {
                ThreadCoordinator.getInstance().getBoostedContext(overworld).postTick().executeTask(() -> {
                    ServerPlayerEntity fakeOverworldPlayer = new ServerPlayerEntity(server, overworld, new GameProfile(UUID.randomUUID(), "" + testPrefix + ".overworld." + overworldEntityByName.size()));
                    fakeOverworldPlayer.setPosition(gameTestPos.getX() + 3, gameTestPos.getY() + 2, gameTestPos.getZ() + 4.5);
                    FakePlayerClientConnection clientConnection = new FakePlayerClientConnection(NetworkSide.SERVERBOUND);
                    server.getPlayerManager().onPlayerConnect(clientConnection, fakeOverworldPlayer);
                    server.getNetworkIo().getConnections().add(clientConnection);
                    fakeOverworldPlayer.setVelocity(0.0,0.0, 0.5);
                    fakeOverworldPlayer.interactionManager.changeGameMode(GameMode.SURVIVAL);
                    fakeOverworldPlayer.networkHandler.onPlayerMove(new PlayerMoveC2SPacket.PositionAndOnGround(fakeOverworldPlayer.getX(), fakeOverworldPlayer.getY(), fakeOverworldPlayer.getZ() + 1, true));
                    overworldEntityByName.put(fakeOverworldPlayer.getEntityName(), fakeOverworldPlayer);
                    expectedNetherworldCounter.setCustomName(Text.of("Expected (in Nether): " + overworldEntityByName.size()));
                });

                BlockPos netherTeleportTarget = getNetherTeleportTarget(gameTestPos, helper.gameTest.getWorld(), nether);
                if (netherTeleportTarget == null) {
                    helper.gameTest.fail(new IllegalStateException("No nether portal"));
                    return;
                }
                /*ThreadCoordinator.getInstance().getBoostedContext(nether).postTick().executeTask(() -> {
                    ServerPlayerEntity fakeNetherPlayer = new ServerPlayerEntity(server, overworld, new GameProfile(UUID.randomUUID(), "" + testPrefix + ".netherworld." + netherEntityByName.size()));
                    fakeNetherPlayer.setPosition(netherTeleportTarget.getX() + 1, netherTeleportTarget.getY() + 1, netherTeleportTarget.getZ() + 2);
                    fakeNetherPlayer.setVelocity(0.0,0.0,-0.2);
                    netherEntityByName.put(fakeNetherPlayer.getEntityName(), fakeNetherPlayer);
                    FakePlayerClientConnection clientConnection = new FakePlayerClientConnection(NetworkSide.SERVERBOUND);
                    server.getPlayerManager().onPlayerConnect(clientConnection, fakeNetherPlayer);
                    fakeNetherPlayer.interactionManager.changeGameMode(GameMode.SURVIVAL);
                    server.getNetworkIo().getConnections().add(clientConnection);
                    expectedOverworldCounter.setCustomName(Text.of("Expected (in Overworld): " + netherEntityByName.size()));
                });*/
            }
            if (ticks > 105) { // boosted adds one tick delay
                startChecking.set(true);
            }
            if (ticks > 395) {
                overworldEntityByName.values().forEach((player) -> {
                    server.getPlayerManager().remove(player);
                    player.networkHandler.disconnect(Text.of("Test Over"));
                    server.getNetworkIo().getConnections().remove(player.networkHandler.getConnection());
                });
                netherEntityByName.values().forEach((player) -> {
                    server.getPlayerManager().remove(player);
                    player.networkHandler.disconnect(Text.of("Test Over"));
                    server.getNetworkIo().getConnections().remove(player.networkHandler.getConnection());
                });
            }
        });
        helper.succeedWhen(() -> {
            BlockPos netherTeleportTarget = getNetherTeleportTarget(gameTestPos, helper.gameTest.getWorld(), nether);
            if (netherTeleportTarget == null) {
                helper.gameTest.fail(new IllegalStateException("No nether portal"));
                return false;
            }
            BlockPos overworldTeleportTarget = getNetherTeleportTarget(netherTeleportTarget, nether, helper.gameTest.getWorld());
            if (overworldTeleportTarget == null) {
                helper.gameTest.fail(new IllegalStateException("No overworld portal"));
                return false;
            }

            clearNetherPortal(netherTeleportTarget, nether);
            Box netherBox = Box.from(Vec3d.ofCenter(netherTeleportTarget)).expand(10);
            List<PlayerEntity> netherItems = nether.getEntitiesByType(EntityType.PLAYER, netherBox, (Entity entity) ->
                    overworldEntityByName.get(entity.getEntityName()) != null);
            netherItemsCounter.setCustomName(Text.of("Nether items: " + netherItems.size()));
            List<Entity> netherEntities = nether.getEntitiesByClass(Entity.class, netherBox, entity ->
                    (netherEntityByName.get(entity.getEntityName()) == null
                    && overworldEntityByName.get(entity.getEntityName()) == null));
            otherNetherCounter.setCustomName(Text.of("Other (Nether): " + netherEntities.size()));

            Box overworldBox = Box.from(Vec3d.ofCenter(overworldTeleportTarget)).expand(10);
            List<PlayerEntity> overworldItems = overworld.getEntitiesByType(EntityType.PLAYER, overworldBox, (Entity entity) ->
                    netherEntityByName.get(entity.getEntityName()) != null);
            overworldItemsCounter.setCustomName(Text.of("Overworld items: " + overworldItems.size()));
            List<Entity> overworldEntities = overworld.getEntitiesByClass(Entity.class, overworldBox, entity ->
                    netherEntityByName.get(entity.getEntityName()) == null
                    && overworldEntityByName.get(entity.getEntityName()) == null);
            otherOverworldCounter.setCustomName(Text.of("Other (Overworld): " + overworldEntities.size()));

            boolean success = startChecking.get()
                    && netherItems.size() + overworldItems.size()
                    == netherEntityByName.size() + overworldEntityByName.size();

            if (success) {
                netherItems.forEach(Entity::kill); // get rid of items
                netherEntities.forEach(Entity::kill); // get rid of unrelated entities
                overworldItems.forEach(Entity::kill); // get rid of items
                overworldEntities.forEach(Entity::kill); // get rid of unrelated entities
            }
            return success
                    && netherEntities.isEmpty()
                    && overworldEntities.isEmpty();
        });
    }
}

