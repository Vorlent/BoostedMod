# Thread Safety Violations


# Portals:

## Problem:

Entity.moveToWorld(ServerWorld destination) is not thread safe.

## Solution:

Execute teleportation on the main thread.
Entity.moveToWorld cannot be redirected on its own, we must redirect the calling code (tickPortal).
EnforceBoosted is used to ensure that moveToWorld is only called inside a ThreadExecutor managed by Boosted.

## Redstone:

### Problem:
RedstoneWireBlock.wiresGivePower is effectively a global variable via Blocks.REDSTONE_WIRE.

### Solution:

Use a ThreadLocal variable

## ScoreBoard

### Problem:

ServerWorld.getScoreBoard() references MinecraftServer.getScoreBoard() which
means there is a global scoreboard which is being shared by every world.

Entity.getScoreboardTeam() references ServerWorld.getScoreBoard().

Player.getScoreboard() references ServerWorld.getScoreBoard().

EntityScoresLootCondition.test(...) references ServerWorld.getScoreBoard()

ScoreLootNumberProvider.nextFloat(...) references ServerWorld.getScoreBoard()

### Solution:

Replace all getX methods with versions that return immutable values
to limit the amount of mutations. Use locks for exclusive write access.

# Commands

As far as I am aware, commands should never be processed in parallel
and there is no significant demand to parallelize them.
Always run the commands on the main thread.

# Networking

Networking should be threadsafe already but I have not confirmed it.

# Saving/Loading

These should happen either before the tick simulation start or if they happen inbetween ticks,
then all worlds must finish their tick simulation and only then does saving start.

# ChunkManager

The ChunkloadDrive may or may not be necessary. I don't know.

# ServerWorld

ServerWorld.server has been found to be problematic in context with the ScoreBoard.
There may be other potential issues regarding this field.

ServerWorld.getMapState(String id)
ServerWorld.putMapState(String id, MapState state)
ServerWorld.getNextMapId()

Always reference the Overworld.

ServerWorld.worldProperties is also unsafe.

ServerWorld.tickTime()
ServerWorld.tickWeather()
ServerWorld.resetWeather()

ServerWorld.setTime()
ServerWorld.setSpawnPos()

are unsafe.

# StructureTemplateManager


# PlayerManager, DedicatedPlayerManager, IntegratedPlayerManager