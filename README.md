# Minecraft2VRCraft

## Overview

Minecraft2VRCraft is a Bukkit plugin for Minecraft 1.20 and higher servers designed to export Minecraft world data in the format that can be used in VRCraft.
This plugin allows players to export sections of their Minecraft world and save the export data to a file.

## Installation

1. Download the Minecraft2VRCraft plugin jar file.
2. Place the jar file in your server's `plugins` directory.
3. Start or restart your Minecraft server to load the plugin.

## Commands

### `/export [bedrock]`

- **Description:** Exports a section of the world centered around the player. If the `bedrock` argument is provided, a bedrock layer will be included in the export. The player need to be an operator or have the `minecraft2vrcraft.export` permission.
- **Usage:** 
  - `/export` - Exports the world without bedrock.
  - `/export bedrock` - Exports the world with a bedrock layer included.

## How to Use

2. **Position Yourself:** Stand at the location where you want the export to be centered.
3. **Execute the Command:** Type `/export` in the chat.
4. **Check Notifications:** After the export is successful, you will receive an in-game notification with a clickable message. Clicking the message will copy the file location of the export to your clipboard.

## Configuration

No additional configuration is required. The plugin automatically handles all necessary setup and file management.

## File Format

The export file is saved in a text format and contains the following components:

- **Base64 Encoded Short Data:** The exported world data encoded in Base64.
- **Block IDs:** A list of block IDs used in the export.
- **World Size:** The size of the exported world section in the format `width x height x depth`.
