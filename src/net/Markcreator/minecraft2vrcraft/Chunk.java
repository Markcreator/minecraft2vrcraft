package net.Markcreator.minecraft2vrcraft;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class Chunk {

	public static final int CHUNK_SIZE = 8;
	
	private World world;
	private Vector chunkId;
	private Location pos;
	private boolean addBedrock;
	
	public Chunk(World world, Vector chunkId, Location pos, boolean addBedrock) {
		this.world = world;
		this.chunkId = chunkId;
		this.pos = pos;
		this.addBedrock = addBedrock;
	}
	
	public int chunkX() {
		return (int) chunkId.getX();
	}
	
	public int chunkY() {
		return (int) chunkId.getY();
	}
	
	public int chunkZ() {
		return (int) chunkId.getZ();
	}
	
	public String[] getBlocks() {
		String[] blocks = new String[CHUNK_SIZE*CHUNK_SIZE*CHUNK_SIZE];
		
		int i = 0;
		for (int y = 0; y < CHUNK_SIZE; y++) {
			for (int z = 0; z < CHUNK_SIZE; z++) {
				for (int x = 0; x < CHUNK_SIZE; x++) {
					Location relativeBlock = pos.clone().add(new Vector(CHUNK_SIZE - x, y, z));
					String name = world.getBlockAt(relativeBlock).getType().name().toLowerCase();
					
					if (name.equals("water"))
					{
						name = "water_still";
					}
					
					if (addBedrock && chunkId.getBlockY() == 0 && y == 0) {
						name = "bedrock";
					}
					
					blocks[i++] = name;
				}
			}
		}
		
		return blocks;
	}
}
