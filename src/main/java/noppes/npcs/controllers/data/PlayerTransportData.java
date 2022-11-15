package noppes.npcs.controllers.data;

import java.util.ArrayList;
import java.util.HashSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.controllers.TransportController;
import noppes.npcs.api.handler.IPlayerTransportData;
import noppes.npcs.api.handler.data.ITransportLocation;

public class PlayerTransportData implements IPlayerTransportData {
	private final PlayerData parent;
	public HashSet<Integer> transports = new HashSet<Integer>();

	public PlayerTransportData(PlayerData parent) {
		this.parent = parent;
	}

	public void loadNBTData(NBTTagCompound compound) {
		HashSet<Integer> dialogsRead = new HashSet<Integer>();
		if(compound == null)
			return;
        NBTTagList list = compound.getTagList("TransportData", 10);
        if(list == null){
        	return;
        }

        for(int i = 0; i < list.tagCount(); i++)
        {
            NBTTagCompound nbttagcompound = list.getCompoundTagAt(i);
            dialogsRead.add(nbttagcompound.getInteger("Transport"));
        }
        this.transports = dialogsRead;
	}

	public void saveNBTData(NBTTagCompound compound) {
		NBTTagList list = new NBTTagList();
		for(int dia : transports){
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			nbttagcompound.setInteger("Transport", dia);
			list.appendTag(nbttagcompound);
		}
		
		compound.setTag("TransportData", list);
	}

	public boolean hasTransport(int id) {
		return transports.contains(id);
	}

	public void addTransport(int id) {
		transports.add(id);
	}

	public void addTransport(ITransportLocation location) {
		transports.add(location.getId());
	}

	public ITransportLocation getTransport(int id) {
		return TransportController.getInstance().getTransport(id);
	}

	public ITransportLocation[] getTransports() {
		ArrayList<ITransportLocation> list = new ArrayList<>();
		for (int id : transports) {
			ITransportLocation location = TransportController.getInstance().getTransport(id);
			list.add(location);
		}

		return list.toArray(new ITransportLocation[0]);
	}

	public void removeTransport(int id) {
		transports.remove(id);
	}
}
