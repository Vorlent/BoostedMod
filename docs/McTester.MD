# McTester

McTester is quite the mysterious mod. By mysterious I mean you have to
read the source code to understand how to use it because of the - ahem -
bountiful documentation.

After some reading, the only hint at what the mod actually does is the extremely
powerful line `return true;`. What does it do? It does nothing short of enabling
a hidden Mojang command called `/test`. If it wasn't for Mojang's command completion,
the writer would have had to dig through Mojang's code as well. He shudders from imagining
the mental handicaps one would acquire by not reading this document beforehand.

# The Test Command

What amazing features does the `/test` command bless us with?

| Subcommmand        | Description                                                                                                                                                                                                                                                                                                                                                              | Syntax                                                   | Example |
|--------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------|---------|
| `/test clearall`   | This command removes all placed tests in the world.                                                                                                                                                                                                                                                                                                                      | `/test clearall`                                         |         |
| `/test create`     | This command creates a new named test. It will also generate a command block that triggers the test and a structure block that places the associated structure. You can save your ingame created structure by opening the structure block menu and pressing `SAVE`. Running this command on a previously created test will recreate it from the provided structure file. | `/test create <testName> [<width>] [<height>] [<depth>]` |         |
| `/test export`     | This command takes an in-game created test and its associated structure and saves it in `run/gameteststructures/<testName>.snbt`                                                                                                                                                                                                                                         | `/test export <testName>`                                |         |
| `/test exportthis` | Same as `/test export` but exports the nearby test.                                                                                                                                                                                                                                                                                                                      |                                                          |         |
| `/test import`     | Import a test saved in `run/gameteststructures/<testName>.snbt`                                                                                                                                                                                                                                                                                                          | `/test import <testName>`                                |         |
| `/test pos`        | You must point your cursor at the structure block.                                                                                                                                                                                                                                                                                                                       | `/test pos`                                              |         |
| `/test run`        |                                                                                                                                                                                                                                                                                                                                                                          |                                                          |         |
| `/test runall`     |                                                                                                                                                                                                                                                                                                                                                                          |                                                          |         |
| `/test runfailed`  |                                                                                                                                                                                                                                                                                                                                                                          |                                                          |         |
| `/test runthese`   |                                                                                                                                                                                                                                                                                                                                                                          |                                                          |         |
| `/test runthis`    |                                                                                                                                                                                                                                                                                                                                                                          |                                                          |         |

# Structure Block

https://minecraft.fandom.com/wiki/Structure_Block

Structures are saved in `.minecraft/saves/(WorldName)/generated/(namespace)/structures`.

# McTester Autorun

What does it do? It runs `/test runall`.