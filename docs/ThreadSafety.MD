# Thread Safety Violations


# Portals:

## Problem:

Entity.moveToWorld(ServerWorld destination) is not thread safe.

## Solution:

Execute teleportation on the main thread.
Entity.moveToWorld cannot be redirected on its own, we must redirect the calling code (tickPortal).
EnforceBoosted is used to ensure that moveToWorld is only called inside a ThreadExecutor managed by Boosted.

One problem that remains is that moving moveToWorld to a post tick executor,
is that we still need to remove the entity from the world mid tick.

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

# Weather and Time ticking in Server World.

ServerWorld.worldProperties is also unsafe.

ServerWorld.tickTime()
ServerWorld.tickWeather()
ServerWorld.resetWeather()

ServerWorld.setTime()
ServerWorld.setSpawnPos()

all run in the multithreaded ServerWorld.tick() function.

One way to approach this problem is to just execute it sequentially.
Splitting split into two functions would do the trick.
Just run weather and time logic sequentially and then run the rest of the tick function in paraallel.
However, I did things the hard way and implemented an unecessarily complex barrier,
so that each world represents a phase and therefore the xth world waits for the x-1 th world to finish to
avoid copying mojang code.


# ServerWorld

ServerWorld.server has been found to be problematic in context with the ScoreBoard.
There may be other potential issues regarding this field.

ServerWorld.getMapState(String id)
ServerWorld.putMapState(String id, MapState state)
ServerWorld.getNextMapId()

Always reference the Overworld.

are unsafe.

# StructureTemplateManager


# PlayerManager, DedicatedPlayerManager, IntegratedPlayerManager

# ServerCommandSource

ServerCommandSource lets server references escape

# CommandBlock and commands

MinecraftServer.getCommandManager() access should always be accompanied
with MinecraftServer synchronization because
X.getCommandSource() leaks unsynchronized references to MinecraftServer
and it is incredibly time consuming to mixin into a hundred commands to fix it.

MinecraftServer.getCommandManager() is generally accessed to execute commands.

# ServerPlayer

has a reference to server and must be fixed like ServerWorld.

MinecraftServer.getServerWorld()

# Scoreboard

https://learn.microsoft.com/en-us/minecraft/creator/documents/scoreboardintroduction


# Maps

Minecraft Maps can show the state of chunks that are far away from the player.