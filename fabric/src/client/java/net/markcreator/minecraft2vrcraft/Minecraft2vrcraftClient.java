package net.markcreator.minecraft2vrcraft;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class Minecraft2vrcraftClient implements ClientModInitializer {

	private static final int SAVE_VERSION = 2;
	private static final BlockPos WORLD_SIZE = new BlockPos(128, 24, 128);
	private static final int CHUNK_SIZE = 8;

	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommandManager.literal("export")
				.executes(context -> {
					exportWorld(context.getSource().getPlayer(), false);
					return 1;
				})
				.then(ClientCommandManager.literal("bedrock")
					.executes(context -> {
						exportWorld(context.getSource().getPlayer(), true);
						return 1;
					})
				)
			);
		});
	}

	private void exportWorld(ClientPlayerEntity player, boolean addBedrock) {
		World world = player.getWorld();
		BlockPos origin = player.getBlockPos().add(-WORLD_SIZE.getX() / 2, -WORLD_SIZE.getY() / 2, -WORLD_SIZE.getZ() / 2);
		BlockPos chunkSize = new BlockPos(WORLD_SIZE.getX() / CHUNK_SIZE, WORLD_SIZE.getY() / CHUNK_SIZE, WORLD_SIZE.getZ() / CHUNK_SIZE);
		int chunkCount = chunkSize.getX() * chunkSize.getY() * chunkSize.getZ();

		Chunk[] chunks = new Chunk[chunkCount];

		int i = 0;
		for (int y = 0; y < chunkSize.getY(); y++) {
			for (int z = 0; z < chunkSize.getZ(); z++) {
				for (int x = 0; x < chunkSize.getX(); x++) {
					BlockPos chunkPos = new BlockPos(chunkSize.getX() - x, y, z);
					BlockPos relativePos = origin.add(
							new BlockPos(WORLD_SIZE.getX() - x * CHUNK_SIZE, y * CHUNK_SIZE, z * CHUNK_SIZE)
					);
					chunks[i++] = new Chunk(world, chunkPos, relativePos, addBedrock);
				}
			}
		}

		String exportData = processExport(chunks);
		saveExport(exportData, player);
	}

	private String[] blockIds;
	private short blockIndex;
	private short[] export;
	private int exportIndex;
	private String exportType;
	private int exportCount;

	private String processExport(Chunk[] chunks) {
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
			for (String block : blocks) {
				if (block.equals(exportType)) {
					exportCount++;
					if (exportCount == Short.MAX_VALUE) {
						commit();
						exportType = block;
					}
				} else {
					commit();
					exportType = block;
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

		String sizeString = WORLD_SIZE.getX() + "x" + WORLD_SIZE.getY() + "x" + WORLD_SIZE.getZ();
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

	private void saveExport(String exportData, ClientPlayerEntity player) {
		try {
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
			String fileName = "export-" + now.format(formatter) + ".txt";

			File dataFolder = new File("config", "minecraft2vrcraft");
			if (!dataFolder.exists()) {
				dataFolder.mkdirs();
			}

			File file = new File(dataFolder, fileName);

			FileWriter writer = new FileWriter(file);
			writer.write(exportData);
			writer.close();

			player.sendMessage(
					Text.literal("Export successful! File saved as: ")
							.append(
									Text.literal(file.getName())
											.styled(style -> style
													.withColor(TextColor.fromRgb(0x55FFFF))
													.withUnderline(true)
													.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getParentFile().getAbsolutePath()))
													.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to open folder")))
											)
							),
					false
			);
		} catch (Exception e) {
			e.printStackTrace();
			player.sendMessage(
					Text.literal("An error occurred while exporting"),
					false
			);
		}
	}

	private static class Chunk {
		private final World world;
		private final BlockPos chunkPos;
		private final BlockPos relativePos;
		private final boolean addBedrock;

		public Chunk(World world, BlockPos chunkPos, BlockPos relativePos, boolean addBedrock) {
			this.world = world;
			this.chunkPos = chunkPos;
			this.relativePos = relativePos;
			this.addBedrock = addBedrock;
		}

		public String[] getBlocks() {
			String[] blocks = new String[CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE];

			int i = 0;
			for (int y = 0; y < CHUNK_SIZE; y++) {
				for (int z = 0; z < CHUNK_SIZE; z++) {
					for (int x = 0; x < CHUNK_SIZE; x++) {
						BlockPos blockPos = relativePos.add(new BlockPos(CHUNK_SIZE - x, y, z));
						Block block = world.getBlockState(blockPos).getBlock();
						String translationKey = block.getTranslationKey();
						String[] parts = translationKey.split("\\.");
						String name = parts[parts.length - 1];

						if (name.equals("water")) {
							name = "water_still";
						}

						if (addBedrock && chunkPos.getY() == 0 && y == 0) {
							name = "bedrock";
						}

						blocks[i++] = name;
					}
				}
			}

			return blocks;
		}
	}
}