This a custom mod made for [Project Flattened](https://www.curseforge.com/minecraft/modpacks/project-flattened) modpack that allows players to spawn custom structure from an nbt file.   
  
It has 2 commands:

*   `/spawnHouse <fileName>` 
    *   This swill spawn the structure at the block the player is at
    *   File name argument can be left empty in which case the default one (`default_base`) is used
    *   All schematics are stored in `/config/pf_schematics` folder
    *   This command can only be run once per player
*   `/clearHouse <playerName>`
    *   `This will allow player to run the /spawnHouse command again`
    *   This commands requires at least level 2 permissions to be run
    *   Player name argument is optional, and if not provided it will be executed on the player that called the command. 

This mod could be added to other packs and used there for the same purpose, but was primarilly built for [Project Flattened](https://www.curseforge.com/minecraft/modpacks/project-flattened) modpack to help support multiple starter bases in multiplayer environment.
