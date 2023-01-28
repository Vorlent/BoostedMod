package org.boosted.test;

import com.mojang.authlib.GameProfile;
import mctester.annotation.GameTest;
import mctester.common.test.creation.GameTestHelper;
import mctester.common.util.GameTestUtil;
import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.PositionedException;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.Heightmap;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.DimensionType;
import org.boosted.ThreadCoordinator;
import org.boosted.util.FakePlayerClientConnection;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Redstone {

    private static Optional<BlockPos> getNetherTeleportTarget(BlockPos pos, ServerWorld origin, ServerWorld destination) {
        boolean destIsNether = destination.getRegistryKey() == World.NETHER;
        WorldBorder worldBorder = destination.getWorldBorder();
        double d = DimensionType.getCoordinateScaleFactor(origin.getDimension(), destination.getDimension());
        BlockPos blockPos2 = worldBorder.clamp(pos.getX() * d, pos.getY(), pos.getZ() * d);
        return destination.getPortalForcer().getPortalRect(blockPos2, destIsNether, worldBorder).map(rect -> rect.lowerLeft);
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
     * Run two redstone clocks. One in the overworld, one in the nether.
     * The contraption tries to drop sand on redstone which drops the sand as items
     * The test succeeds once the sand pile is empty and fails if the redstone
     * logic gets stuck in an infinite loop
     */
    @GameTest
    public static void nether(GameTestHelper helper) {
        MinecraftServer server = helper.gameTest.getWorld().getServer();
        ServerWorld overworld = server.getOverworld();
        ServerWorld nether = Objects.requireNonNull(server.getWorld(World.NETHER));
        BlockPos gameTestPos = helper.gameTest.getPos();

        ServerPlayerEntity fakeOverworldPlayer = new ServerPlayerEntity(server, overworld, new GameProfile(UUID.randomUUID(), "redstone.nether.1"), null);

        helper.addRepeatedAction((gameTestHelper, ticks) -> {

            if (ticks == 1) {
                // start redstone clock by destroying the lever
                helper.setBlockState(1, 2, 0, Blocks.AIR.getDefaultState());

                ThreadCoordinator.getInstance().getBoostedContext(overworld).postTick().executeTask(() -> {
                    fakeOverworldPlayer.setPosition(gameTestPos.getX() + 3, gameTestPos.getY() + 2, gameTestPos.getZ() + 4.5);
                    FakePlayerClientConnection clientConnection = new FakePlayerClientConnection(NetworkSide.SERVERBOUND);
                    server.getPlayerManager().onPlayerConnect(clientConnection, fakeOverworldPlayer);
                    server.getNetworkIo().getConnections().add(clientConnection);
                    fakeOverworldPlayer.setVelocity(0.0,0.0, 0.5);
                    fakeOverworldPlayer.setPos(fakeOverworldPlayer.getX(), fakeOverworldPlayer.getY(), fakeOverworldPlayer.getZ() + 1);
                    fakeOverworldPlayer.interactionManager.changeGameMode(GameMode.SURVIVAL);
                    fakeOverworldPlayer.networkHandler.onPlayerMove(new PlayerMoveC2SPacket.PositionAndOnGround(fakeOverworldPlayer.getX(), fakeOverworldPlayer.getY(), fakeOverworldPlayer.getZ() + 1, true));
                });

                Optional<BlockPos> netherTeleportTarget = getNetherTeleportTarget(gameTestPos, helper.gameTest.getWorld(), nether);
                if (netherTeleportTarget.isEmpty()) {
                    helper.gameTest.fail(new IllegalStateException("No nether portal"));
                    return;
                }
            }
            if (ticks > 395) {
                server.getPlayerManager().remove(fakeOverworldPlayer);
                fakeOverworldPlayer.networkHandler.disconnect(Text.of("Test Over"));
                server.getNetworkIo().getConnections().remove(fakeOverworldPlayer.networkHandler.getConnection());
            }
        });

        final boolean[] netherStructureBlockSetUp = {false};
        AtomicInteger emptyTicks = new AtomicInteger();

        helper.succeedWhen(() -> {
            System.out.println("Overworld " + gameTestPos);

            Optional<BlockPos> netherTeleportTargetOptional = getNetherTeleportTarget(gameTestPos, helper.gameTest.getWorld(), nether);
            if (netherTeleportTargetOptional.isEmpty()) {
                helper.gameTest.fail(new IllegalStateException("No nether portal"));
                return false;
            }
            BlockPos netherTeleportTarget = netherTeleportTargetOptional.get().add(0,10,0);
            System.out.println("Portal: " + netherTeleportTarget);

            if (!netherStructureBlockSetUp[0]) {

                // instantiate redstone structure block
                nether.setBlockState(netherTeleportTarget.add(1, -1, 1), Blocks.STRUCTURE_BLOCK.getDefaultState());
                BlockEntity structureBlock = nether.getBlockEntity(netherTeleportTarget.add(1, -1, 1));
                System.out.println("structureBlock " + structureBlock);
                if (structureBlock instanceof StructureBlockBlockEntity) {
                    ((StructureBlockBlockEntity) structureBlock).setTemplateName("minecraft:redstone.nether");
                    ((StructureBlockBlockEntity) structureBlock).loadStructure(nether, false);
                }

                //nether.setBlockState(netherTeleportTarget.add(4, 2, 3), Blocks.IRON_BLOCK.getDefaultState());

                // start nether clock by destroying the lever
                nether.setBlockState(netherTeleportTarget.add(2,1,1), Blocks.IRON_BLOCK.getDefaultState());

                netherStructureBlockSetUp[0] = true;
            }

            Box box = Box.from(Vec3d.ofCenter(netherTeleportTarget)).expand(10);

            // check if the sand piles have disappeared
            System.out.println("SAND " + helper.getBlockState(3, 4, 2).getBlock());
            if (Objects.equals(helper.getBlockState(3, 4, 2).getBlock(), Blocks.AIR)) {
                emptyTicks.getAndIncrement();
            } else {
                emptyTicks.set(0);
            }
            return emptyTicks.get() > 20;
        });
    }

    public static void pressButton(GameTestHelper helper, World world, int x, int y, int z) {
        BlockPos blockPos = new BlockPos(x, y, z);
        BlockState blockState = world.getBlockState(blockPos);
        if (blockState.getBlock() instanceof AbstractButtonBlock) {
            blockState.onUse(world, null, null, new BlockHitResult(new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()), Direction.DOWN, blockPos, false));
        } else {
            throw new PositionedException("No pushable button found.", GameTestUtil.transformRelativeToAbsolutePos(helper.gameTest, new BlockPos(x, y, z)), new BlockPos(x, y, z), helper.currTick);
        }
    }

    @Nullable
    private static TeleportTarget getEndTeleportTarget(ServerWorld origin, ServerWorld destination) {
        boolean bl = origin.getRegistryKey() == World.END && destination.getRegistryKey() == World.OVERWORLD;
        boolean bl2 = destination.getRegistryKey() == World.END;
        if (bl || bl2) {
            BlockPos blockPos = bl2 ? ServerWorld.END_SPAWN_POS : destination.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, destination.getSpawnPos());
            return new TeleportTarget(new Vec3d((double) blockPos.getX() + 0.5, blockPos.getY(), (double) blockPos.getZ() + 0.5), Vec3d.ZERO, 0, 0);
        } else {
            return null;
        }
    }
}

