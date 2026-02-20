# LifeSteal Plugin

A lightweight lifesteal plugin for Paper 1.21.1 build by theTWIXhunter

## Features

- **Heart Stealing**: When a player kills another player, the victim loses 1 heart and the killer gains it
- **Elimination System**: Execute custom commands when a player reaches less than 1 heart
- **World-Specific**: Only active in configured worlds
- **Configurable**: Fully customizable through config.yml
- **/giftheart**: Players can gift other players hearts, gifting hearts to an eliminated player revives them.

## Configuration

Edit `config.yml` to customize the plugin:

- `enabled-worlds`: List of worlds where lifesteal is active
- `elimination-commands`: Custom commands to run when a player is eliminated (use `%player%` placeholder)
- `revive-commands`: Commands to run when a player gets revived.
- `hearts.amount`: Hearts gained/lost per kill (default: 1.0)
- `hearts.minimum`: Minimum hearts a player can have (default: 1.0)
- `hearts.maximum`: Maximum hearts a player can have (default: 20.0)

- `crafting:` Enable/disable and completely customise the crafting recipy.

## Building

Run `mvn clean package` to build the plugin. The compiled JAR will be in the `target` folder.

## Installation

1. Build the plugin or download the JAR
2. Place the JAR in your server's `plugins` folder
3. Restart the server
4. Configure the plugin in `plugins/LifeSteal/config.yml`
5. Reload or restart the server

## Author

me.thetwixhunter


