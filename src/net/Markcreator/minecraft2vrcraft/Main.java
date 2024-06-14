package net.Markcreator.minecraft2vrcraft;

import java.io.File;
import java.io.FileWriter;
import java.util.Base64;

import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class Main extends JavaPlugin implements Listener, CommandExecutor {

	public static String prefix;
	
	@Override
	public void onEnable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		prefix = "[" + pdfFile.getName() + "] ";
		this.getLogger().info(prefix + " has been enabled.");
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("export")) {
			if (sender instanceof Player player) {
				String export = exportWorld(player, player.getLocation().clone().add(-WORLD_SIZE.getBlockX() / 2, -WORLD_SIZE.getBlockY() / 2, -WORLD_SIZE.getBlockZ() / 2));
								
				try {
					File file = new File(this.getDataFolder(), "export.txt");
					if (!file.getParentFile().exists())
					{
						file.getParentFile().mkdir();
					}
					
					FileWriter writer = new FileWriter(file);
					writer.write(export);
					writer.close();
					
					TextComponent message = new TextComponent(prefix + "Export successful! Click to copy file location");
					message.setUnderlined(true);
					message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Copy export file location to clipboard: " + file.getAbsolutePath())));
					message.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, file.getAbsolutePath()));
					player.spigot().sendMessage(message);
				} catch (Exception e2) {
					e2.printStackTrace();
					player.sendMessage("An error occurred while exporting");
				}
			}
			else
			{
				sender.sendMessage(prefix + "Export can only be triggered by player");
			}
			return true;
		}
		
		return false; 
	}

	private String exportWorld(Player player, Location origin) {
		int chunkX = WORLD_SIZE.getBlockX() / Chunk.CHUNK_SIZE;
		int chunkY = WORLD_SIZE.getBlockY() / Chunk.CHUNK_SIZE;
		int chunkZ = WORLD_SIZE.getBlockZ() / Chunk.CHUNK_SIZE;
		int chunkCount = chunkX * chunkY * chunkZ;
		
		Chunk[] chunks = new Chunk[chunkCount];
		
		int i = 0;
		for (int y = 0; y < chunkY; y++) {
			for (int z = 0; z < chunkZ; z++) {
				for (int x = 0; x < chunkX; x++) {
					Vector chunkId = new Vector(chunkX - x, y, z);
					Location relativePos = origin.clone().add(new Vector(WORLD_SIZE.getBlockX() - x * Chunk.CHUNK_SIZE, y * Chunk.CHUNK_SIZE, z * Chunk.CHUNK_SIZE));
					chunks[i++] = new Chunk(origin.getWorld(), chunkId, relativePos);;
				}
			}
		}
		
		return exportWorld(player.getWorld(), chunks);
	}
	
	private static final int SAVE_VERSION = 2;
    private static final BlockVector WORLD_SIZE = new BlockVector(128, 24, 128);
    
    private String[] blockIds;
    private short blockIndex;
    private short[] export;
    private int exportIndex;
    private String exportType;
    private int exportCount;
	
	public String exportWorld(World world, Chunk[] chunks) {
        blockIds = new String[1024];
        blockIds[0] = "air";
        blockIndex = 1;

        export = new short[1024];
        exportIndex = 0;
        exportType = null;
        exportCount = 0;

        export[exportIndex++] = SAVE_VERSION;
        export[exportIndex++] = 0;

        for (Chunk chunk : chunks) {
            String[] blocks = chunk.getBlocks();
            for (int i = 0; i < blocks.length; i++) {
                if (blocks[i].equals(exportType)) {
                    exportCount++;
                    if (exportCount == Short.MAX_VALUE) {
                        commit();
                        exportType = blocks[i];
                    }
                } else {
                    commit();
                    exportType = blocks[i];
                    exportCount++;
                }
            }
        }
        commit();

        byte[] byteArray = new byte[exportIndex * 2];
        for (int i = 0; i < exportIndex; i++) {
            byteArray[i * 2] = (byte) (export[i] >> 8);
            byteArray[i * 2 + 1] = (byte) (export[i] & 0xFF);
        }

        String[] shrunkBlockIds = new String[blockIndex];
        System.arraycopy(blockIds, 0, shrunkBlockIds, 0, blockIndex);

        String sizeString = (int) WORLD_SIZE.getX() + "x" + (int) WORLD_SIZE.getY() + "x" + (int) WORLD_SIZE.getZ();
        return Base64.getEncoder().encodeToString(byteArray) + "|" + String.join(",", shrunkBlockIds) + "|" + sizeString;
    }

    private void commit() {
        if (exportType != null) {
            int exportTypeIndex = ArrayUtils.indexOf(blockIds, exportType);
            if (exportTypeIndex == ArrayUtils.INDEX_NOT_FOUND) {
                blockIds[exportTypeIndex = blockIndex] = exportType;
                blockIndex++;
            }

            export[exportIndex++] = (short) exportTypeIndex;
            export[exportIndex++] = (short) exportCount;

            if (exportIndex == export.length) {
                // Grow array
                short[] newExport = new short[export.length + 1024];
                System.arraycopy(export, 0, newExport, 0, exportIndex);
                export = newExport;
            }
        }
        exportType = null;
        exportCount = 0;
    }
}
