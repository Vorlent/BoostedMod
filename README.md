# Boosted Mod

How can Minecraft be scaled to more than 250 players on a single server?

There are many options:

We run every world on a different server and can increase the number of servers
and teleport players between servers when people change worlds.
This works and is widely deployed but it is not a satisfying solution
for servers with large shared worlds. See Spigot and BungeeCord.

We can increase the number of servers and divide the world
into fixed sections each running on their own servers.
This is much better for large shared worlds but we now have a literal edge case...
You cannot see past the edge of the world. To solve this problem each server
must communicate with each other about changes near the borders.
See WorldQL and Mammoth.

We can optimize the codebase, skip unnecessary, harmful or unwanted features
and their associated computational load. This is what optimized servers like PaperMC do.

Finally, there is the crazy option of multithreading minecraft.
The idea behind multithreading is to run minecraft worlds on different threads
and each thread will then try to run entity, block entity and environment/chunk simulations
in parallel. The biggest benefits of this approach are that we will save RAM by reducing
the number of Minecraft server instances and that we can simulate bigger and more complicated minecraft worlds
on a single computer with many cores. There is a mod that has done this called JMT-MCMT aka
Minecraft Multithreaded and its fabric port MCMTfabric.

The problems with MCMT:

MCMT tries to fully parallelize Minecraft as mentioned above. The obvious flaw
with this approach is that Minecraft was not written for multiple threads.
Every single Minecraft Java class will have to be checked and most likely requires synchronization.
This means a complete rewrite of Minecraft which is why Mojang is so reluctant to implement multithreading.

MCMT works amazingly well if all you care about is simulating large TNT explosions.
However, longterm use of MCMT results in world corruption, strange bugs like cows having two children at once,
hoppers duplicating items... Minecraft with MCMT is one of the most exploitable versions of the game.
MCMT is not useful as a mod for end users but as a case study and stepping stone for stable multithreading.
We know what will not work but we also know what works well. This is why I have borrowed the
MCMT source code as a basis for this mod.

# Goals of Boosted

As we have learned, full multithreading is difficult and unstable. We must start with the least
amount of multithreading that retains stability and divide the problem into three stages.

## Boosted Stage One

Stage one concerns itself with the most basic form of multithreading that is still
usefulf or endusers while simultaneously being easy to implement and maintain
stability.

For this reason, we are going to begin with the least useful form of minecraft server
scaling. We are going to run dimensions on separate threads. In theory this can be solved
with Spigot and Bungeecord, but this mod will speed up single player worlds as well and save precious RAM.

## Goal of Stage One:

* Run different Minecraft dimensions on different threads.
* Find all possible inter dimension interactions and synchronize them

The classic example of inter dimension interaction is teleportation of players
and entities between worldsthrough portals and teleport commands.

## Boosted Stage Two:

Stage two concerns itself with a very limited case of multithreading that may be very valuable
for anarchy servers like 2b2t. Players who are far away from each other and whose loaded chunks
do not overlap can be treated as if they were in different dimensions. This means we are going to
divide the loaded chunks into isolated islands and each isolated island will run in parallel.
This does not solve the problem of hundreds of players being in the same location, it only solves situation
where players are exploring an incredibly large world with lots of distance between bases.
There is also the pathological case where players create a very long highway so that
all players are seeing their neighbor which prevents the entire highway from being split up
onto multiple threads.

## Goal of Stage Two:

* Gather all loaded chunks within simulation distance of another player
* Merge overlapping sections into one section.
* Simulate disconnected sections on different threads

## Goal of Stage Three:

Stage Three will always remain experimental but with some hope it is possible.

Stage one and Stage two have one thing in common: They actually don't try
to parallelize Minecraft itself. They try to split Minecraft into micro servers
that can be run in parallel but since the load balancing happens in memory
on the same server, it is imperceptible that load balancing occurs.
Each micro instance of minecraft is still single threaded.

So what count as real parallelism? Each micro instance of minecraft is supposed
to run some part, not all of it, of the simulation in parallel.

The best option is to run the Minecraft simulation partially. Entityies, Block entites
are run on a single thread but we try to gather and pile up work that can be processed
in parallel. Things like path finding can be rewritten so that entities request a path,
and once all Entity.update etc finish, we have a pile of paths to compute in parallel.
This possibly introduces one tick of delay and it has the obvious downside of requiring every entity class
that touches path finding to be rewritten.

# Roadblocks and potential problems even if everything goes well

* The mod requires massive changes to the source code
* Mojang changes the codebase and any stage of Boosted needs to be completely rewritten or becomes completely imposssible
* The mod may be incompatible with every other mod
* Despite extreme care, subtle bugs will still occur (ordering, duping, delays, deadlock)

# Which of these Roadblocks do you think is potentially the most disruptive?

Mojang by far. The problem isn't actually multithreading minecraft
to run on multiple worlds but rather very simple and idiotic API design decisions
that make no sense in combination with multithreading.

For example, teleporting Entities creates a new entity and deletes the entity in the old world, 
which is actually pretty good for this mod but when you teleport from the overworld to the nether,
the overworld thread will obtain an instance of the nether entity
and the returned entity is e.g. used to merge transferred item entities in the nether.
If the overworld and nether are on separate threads then running nether code in the overworld thread
is obviously a no go.

Realisically speaking, a single Mojang employee working on multithreading could do
Boosted stage one themselves within a month.