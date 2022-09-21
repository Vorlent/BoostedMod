package org.boosted.test;

import mctester.annotation.GameTest;
import mctester.common.test.creation.GameTestHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.util.math.BlockPos;

import java.util.List;

import static net.minecraft.entity.EntityType.MINECART;
public class Portal {

    private static BlockPos getNetherTeleportTarget(BlockPos pos, ServerWorld origin, ServerWorld destination) {
        boolean bl3;
        boolean bl2;
        boolean bl5 = bl3 = destination.getRegistryKey() == World.NETHER;
        WorldBorder worldBorder = destination.getWorldBorder();
        double d = DimensionType.getCoordinateScaleFactor(origin.getDimension(), destination.getDimension());
        BlockPos blockPos2 = worldBorder.clamp(pos.getX() * d, pos.getY(), pos.getZ() * d);
        return destination.getPortalForcer().getPortalRect(blockPos2, bl3, worldBorder).map(rect -> {
            Direction.Axis axis;
            BlockPos lastNetherPortalPosition = rect.lowerLeft;
            BlockState blockState = origin.getBlockState(lastNetherPortalPosition);
            if (blockState.contains(Properties.HORIZONTAL_AXIS)) {
                axis = blockState.get(Properties.HORIZONTAL_AXIS);
            } else {
                axis = Direction.Axis.X;
            }

            System.out.println(rect.lowerLeft);

            return rect.lowerLeft;
        }).orElse(null);
    }

    public static void clearNetherPortal(BlockPos portalPos, World nether) {
        for(int x = -10; x < 10; x++) {
            for(int y = -10; y < 10; y++) {
                for(int z = -10; z < 10; z++) {
                    BlockPos pos = portalPos.add(x, y, z);
                    if (nether.getBlockState(pos).getBlock() != Blocks.OBSIDIAN && nether.getBlockState(pos).getBlock() != Blocks.NETHER_PORTAL) {
                        nether.setBlockState(pos, Blocks.AIR.getDefaultState());
                    }
                }
            }
        }
    }

    @GameTest
    public static void single(GameTestHelper helper) {
        CowEntity cowEntity = helper.spawnWithNoFreeWill(EntityType.COW, 2, 4, 2);
        cowEntity.setCustomName(Text.of("" + Math.random()));
        cowEntity.setCustomNameVisible(true);
        helper.walkTo(cowEntity, 3,2,4);
        helper.succeedWhenEntityPresent(MINECART, 3, 2, 4);
        helper.succeedWhen(() -> {
            MinecraftServer server = helper.gameTest.getWorld().getServer();
            ServerWorld nether = server.getWorld(World.NETHER);
            // I need to calculate the position in the nether
            // then load a structure definition
            // and finally check if the entity is in the other structure
            BlockPos gameTestPos = helper.gameTest.getPos();
            System.out.println("Overworld " + gameTestPos);

            BlockPos netherTeleportTarget = getNetherTeleportTarget(gameTestPos, helper.gameTest.getWorld(), nether);
            System.out.println("Portal: " + netherTeleportTarget);
            nether.setBlockState(netherTeleportTarget.add(1, -1, 1), Blocks.STRUCTURE_BLOCK.getDefaultState());
            clearNetherPortal(netherTeleportTarget, nether);
            Box box = Box.from(Vec3d.ofCenter(netherTeleportTarget)).expand(10);
            List<CowEntity> entitiesByType = nether.getEntitiesByType(EntityType.COW, box, (Entity entity) -> cowEntity.getCustomName().equals(entity.getCustomName()));
            // cleanup cows
            if(!entitiesByType.isEmpty()) {
                entitiesByType.forEach(entity -> entity.kill());
            }
            return !entitiesByType.isEmpty();
        });
    }
}

