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

| Subcommmand        | Description                                                                                                                                                                                                                                                                                                                                                                                                                                      | Syntax                                                  |
|--------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------|
| `/test clearall`   | This command removes all placed tests in the world.                                                                                                                                                                                                                                                                                                                                                                                              | `/test clearall`                                        |
| `/test create`     | This command creates a new named test. It will also generate a command block that triggers the test and a structure block that places the associated structure. You can save your ingame created structure by opening the structure block menu and pressing `SAVE`. Running this command on a previously created test will recreate it from the provided structure file, you will have to press the `LOAD` button of the structure block though. | `/test create <testName> [<width>] [<height>] [<depth>]` |
| `/test export`     | This command takes an in-game created test structure and saves it in `run/gameteststructures/<testName>.snbt`                                                                                                                                                                                                                                                                                                                                    | `/test export <testName>`                               |
| `/test exportthis` | Same as `/test export` but exports the nearby test.                                                                                                                                                                                                                                                                                                                                                                                              |                                                         |
| `/test import`     | Import a test structure saved in `run/gameteststructures/<testName>.snbt`                                                                                                                                                                                                                                                                                                                                                                        | `/test import <testName>`                               |
| `/test pos`        | You must point your cursor at the structure block.                                                                                                                                                                                                                                                                                                                                                                                               | `/test pos`                                             |
| `/test run`        | This command creates a named test if it doesn't already exist in the world and executes it immediately.                                                                                                                                                                                                                                                                                                                                          | `/test run <testName>`                                  |
| `/test runall`     | This command tries to run all found tests.                                                                                                                                                                                                                                                                                                                                                                                                       | `/test runall`                                          |
| `/test runfailed`  | If previously executed tests fail, this command will rerun them.                                                                                                                                                                                                                                                                                                                                                                                 | `/test runfailed`                                       |
| `/test runthese`   | Run all tests within an unknown block radius                                                                                                                                                                                                                                                                                                                                                                                                     | `/test runthese`                                        |
| `/test runthis`    | Run the closest test within a 15 block radius                                                                                                                                                                                                                                                                                                                                                                                                    | `/test runthis`                                         |

# Fabric gametest-api

It turns out the developers of fabric have decided to make our lives worse because of the following justification:
`Replace the default test structure loading with something that works a bit better for mods.`

Having invisible code mess with you is not something I wish upon anyone. The fabric gamtest-api expects you to place your
tests in the following location: `data.minecraft.gametest.structures.<testName>.snbt` and it will crash your minecraft
instance if you aren't smart enough to reverse engineer fabric gametest-api.

# Structure Block

https://minecraft.fandom.com/wiki/Structure_Block

Structures are saved in `.minecraft/saves/(WorldName)/generated/(namespace)/structures`.

# McTester Autorun

What does it do? It runs `/test runall`.