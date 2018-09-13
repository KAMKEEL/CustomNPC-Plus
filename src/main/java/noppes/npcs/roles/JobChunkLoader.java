package noppes.npcs.roles;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import noppes.npcs.controllers.ChunkController;
import noppes.npcs.entity.EntityNPCInterface;

public class JobChunkLoader extends JobInterface{
	
	private List<ChunkCoordIntPair> chunks = new ArrayList<ChunkCoordIntPair>();
	private int ticks = 20;
	private long playerLastSeen = 0;

	public JobChunkLoader(EntityNPCInterface npc) {
		super(npc);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setLong("ChunkPlayerLastSeen", playerLastSeen);
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		playerLastSeen = compound.getLong("ChunkPlayerLastSeen");
	}

	@Override
	public boolean aiShouldExecute() {
		ticks--;
		if(ticks > 0)
			return false;
		ticks = 20;
		
		List players = npc.worldObj.getEntitiesWithinAABB(EntityPlayer.class, npc.boundingBox.expand(48, 48, 48));
		if(!players.isEmpty())
			playerLastSeen = System.currentTimeMillis();
		
		//unload after 10 min
		if(System.currentTimeMillis() > playerLastSeen + 600000){
			ChunkController.instance.deleteNPC(npc);
			chunks.clear();
			return false;
		}
		Ticket ticket = ChunkController.instance.getTicket(npc);
		if(ticket == null) //Only null when too many active chunkloaders already
			return false;
		double x = npc.posX / 16;
		double z = npc.posZ / 16;
		
		List<ChunkCoordIntPair> list = new ArrayList<ChunkCoordIntPair>();
		list.add(new ChunkCoordIntPair(MathHelper.floor_double(x), MathHelper.floor_double(z)));
		list.add(new ChunkCoordIntPair(MathHelper.ceiling_double_int(x), MathHelper.ceiling_double_int(z)));
		list.add(new ChunkCoordIntPair(MathHelper.floor_double(x), MathHelper.ceiling_double_int(z)));
		list.add(new ChunkCoordIntPair(MathHelper.ceiling_double_int(x), MathHelper.floor_double(z)));

		for(ChunkCoordIntPair chunk : list){
			if(!chunks.contains(chunk)){
				ForgeChunkManager.forceChunk(ticket, chunk);
			}
			else
				chunks.remove(chunk);
		}
		
		for(ChunkCoordIntPair chunk : chunks)
			ForgeChunkManager.unforceChunk(ticket, chunk);
		
		this.chunks = list;
		return false;
	}

	@Override
	public void reset() {
		ChunkController.instance.deleteNPC(npc);
		chunks.clear();
		playerLastSeen = 0;
	}
	public void delete() {
	}
}
