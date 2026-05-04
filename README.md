You can find the Latest version here: https://github.com/NezerX/Aspect-Alchemy/releases

Aspect Alchemy

Aspect Alchemy is a Minecraft mod for the Fabric loader that introduces a deep, logic-based potion-making system centered around the Aspect Cauldron. Features Aspect Brewing System

Every ingredient possesses hidden status effects known as "aspects." Players must combine ingredients to manifest these effects in a final brew.

Logic: An effect is only added to the potion if it is present in at least 2 different ingredients.

Amplification: * Effect appears in 2 ingredients: Level I

    Effect appears in 3 ingredients: Level II

Capacity: The Aspect Cauldron accepts up to 3 unique ingredients per cycle.

Mechanics

Boiling: The cauldron must be placed over a heat source (Fire, Campfire, Lava, Magma) to function.

Visuals: The water color dynamically blends based on the status effects of the added ingredients.

Absorption: Players can stand inside the cauldron to apply effects instantly without using bottles.

Compatibility: Full support for Glass Bottles and Buckets.

Technical Information

Minecraft Version: 1.20.1

Mod Loader: Fabric

Dependencies: Fabric API

Environment: Required on both Client and Server.

Installation

Ensure you have the latest version of the Fabric Loader installed.

Download the Aspect Alchemy jar file.

Place the jar file and the Fabric API into your mods folder.

Building from source

To build the mod locally, clone the repository and run the following command: Bash

./gradlew build

The compiled jar will be located in build/libs. Planned Features

Configuration files for custom duration and amplifier rules.

EMI/JEI/REI integration.

Datapack support for adding custom ingredient aspects.

Aspect Lens for previewing brews.

Contributing

Contributions are welcome. Please follow these steps:

Fork the repository.

Create a feature branch.

Submit a Pull Request with a detailed description of your changes.

For bug reports, please use the GitHub Issues tab and include logs via Pastebin. License

This project is licensed under the MIT License. You are free to use the code with proper attribution.