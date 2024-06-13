package net.Markcreator.minecraft2vrcraft;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class Chunk {

	public static final int CHUNK_SIZE = 8;
	
	private World world;
	private Vector chunkId;
	private Location pos;
	
	public Chunk(World world, Vector chunkId, Location pos) {
		this.world = world;
		this.chunkId = chunkId;
		this.pos = pos;
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
					Location relativeBlock = pos.clone().add(new Vector(x, y, z));
					blocks[i++] = world.getBlockAt(relativeBlock).getType().name().toLowerCase();
				}
			}
		}
		
		return blocks;
	}
}
