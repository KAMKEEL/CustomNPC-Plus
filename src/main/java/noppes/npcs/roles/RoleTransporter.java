package noppes.npcs.roles;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.PlayerTransportData;
import noppes.npcs.controllers.TransportController;
import noppes.npcs.controllers.TransportLocation;
import noppes.npcs.entity.EntityNPCInterface;

public class RoleTransporter extends RoleInterface{
	
	public int transportId = -1;
	public String name;
	
	public RoleTransporter(EntityNPCInterface npc) {
		super(npc);
	}
	

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("TransporterId", transportId);
		return nbttagcompound;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		transportId = nbttagcompound.getInteger("TransporterId");
		TransportLocation loc = getLocation();
		if(loc != null){
			name = loc.name;
		}
	}
	private int ticks = 10;
	@Override
	public boolean aiShouldExecute() {
		ticks--;
		if(ticks > 0)
			return false;
		ticks = 10;
		
		if(!hasTransport())
			return false;
		
		TransportLocation loc = getLocation();
		if(loc.type != 0)
			return false;

		List<EntityPlayer> inRange = npc.worldObj.getEntitiesWithinAABB(EntityPlayer.class,npc.boundingBox.expand(6D, 6D, 6D));
		for(EntityPlayer player :inRange){
			if(!npc.canSee(player))
				continue;
			unlock(player, loc);			
		}
		return false;
		
	}
	@Override
	public void aiStartExecuting() {
		
	}

	@Override
	public void interact(EntityPlayer player) {
		if(hasTransport()){
			TransportLocation loc = getLocation();
			if(loc.type == 2){
				unlock(player, loc);	
			}
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerTransporter, npc);
		}
	}
	
	private void unlock(EntityPlayer player, TransportLocation loc){
		PlayerTransportData data = PlayerDataController.instance.getPlayerData(player).transportData;
		if(data.transports.contains(transportId))
			return;
		data.transports.add(transportId);
		player.addChatMessage(new ChatComponentTranslation("transporter.unlock", loc.name));
	}
	public TransportLocation getLocation(){
		if(npc.isRemote())
			return null;
		return TransportController.getInstance().getTransport(transportId);
	}
	public boolean hasTransport(){
		TransportLocation loc = getLocation();
		return loc != null && loc.id == transportId;
	}

	public void setTransport(TransportLocation location) {
		transportId = location.id;
		name = location.name;
	}

}
