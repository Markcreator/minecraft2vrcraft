# Minecraft2VRCraft

## Overview

Minecraft2VRCraft is a Fabric mod for Minecraft client designed to export Minecraft world data in a format compatible with VRCraft. This mod allows players to export sections of their Minecraft world and save the export data to a file.

## Installation

1. Ensure you have Fabric installed for your Minecraft client.
2. Download the Minecraft2VRCraft mod jar file.
3. Place the jar file in your Minecraft `mods` folder.
4. Launch Minecraft with the Fabric profile.

## Commands

### `/export [bedrock]`

- **Description:** Exports a section of the world centered around the player. If the `bedrock` argument is provided, a bedrock layer will be included in the export.
- **Usage:** 
  - `/export` - Exports the world without bedrock.
  - `/export bedrock` - Exports the world with a bedrock layer included.

## How to Use

1. **Launch Minecraft:** Start Minecraft with the Fabric profile and the Minecraft2VRCraft mod installed.
2. **Position Yourself:** Stand at the location where you want the export to be centered.
3. **Execute the Command:** Type `/export` or `/export bedrock` in the chat.
4. **Check Notifications:** After the export is successful, you will receive an in-game message with the file path of the saved export.

## Export Details

- **World Size:** The exported area is 128x24x128 blocks, centered on the player's position.
- **Chunk Size:** The world is divided into chunks of 8x8x8 blocks for processing.
- **File Location:** Exports are saved in the `config/minecraft2vrcraft` folder of your Minecraft directory.
- **File Naming:** Export files are named in the format `export-YYYY-MM-DD-HH-mm-ss.txt`.

## File Format

The export file is saved in a text format and contains the following components:

- **Base64 Encoded Data:** The exported world data encoded in Base64.
- **Block IDs:** A list of block IDs used in the export.
- **World Size:** The size of the exported world section in the format `width x height x depth`.
