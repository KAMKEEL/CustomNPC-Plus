package noppes.npcs.controllers.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.controllers.TransportController;
import noppes.npcs.api.handler.data.ITransportCategory;
import noppes.npcs.api.handler.data.ITransportLocation;

public class TransportCategory implements ITransportCategory {
	public int id = -1;
	public String title = "";
	public HashMap<Integer,TransportLocation> locations;

	public TransportCategory(){
		locations = new HashMap<Integer, TransportLocation>();
	}

	public Vector<TransportLocation> getDefaultLocations() {
		Vector<TransportLocation> list = new Vector<TransportLocation>();
		for(TransportLocation loc : locations.values())
			if(loc.isDefault())
				list.add(loc);
		return list;
	}

	public void readNBT(NBTTagCompound compound){
    	id = compound.getInteger("CategoryId");
    	title = compound.getString("CategoryTitle");

    	NBTTagList locs = compound.getTagList("CategoryLocations", 10);
        if(locs == null || locs.tagCount() == 0)
        	return;

        for(int ii = 0; ii < locs.tagCount(); ii++)
        {
        	TransportLocation location = new TransportLocation();
        	location.readNBT(locs.getCompoundTagAt(ii));
        	location.category = this;
        	locations.put(location.id,location);
        }
	}

	public void writeNBT(NBTTagCompound compound){
    	compound.setInteger("CategoryId", id);
    	compound.setString("CategoryTitle", title);
        NBTTagList locs = new NBTTagList();
    	for(TransportLocation location : locations.values()){
	        locs.appendTag(location.writeNBT());
    	}
    	compound.setTag("CategoryLocations", locs);
	}

	public int getId() {
		return id;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void addLocation(String name) {
		int id = TransportController.getInstance().getUniqueIdLocation();
		TransportLocation location = new TransportLocation();
		location.id = id;
		location.name = name;
		location.category = this;

		TransportController.getInstance().setLocation(location);
	}

	public ITransportLocation getLocation(String name) {
		TransportLocation location = null;
		for (TransportLocation l : locations.values()) {
			if (l.getName().equals(name)) {
				location = l;
				break;
			}
		}

		return location;
	}

	public void removeLocation(String name) {
		int id = -1;
		for (Map.Entry<Integer,TransportLocation> entry : locations.entrySet()) {
			if (entry.getValue().getName().equals(name)) {
				id = entry.getKey();;
				locations.remove(entry.getKey());
				break;
			}
		}

		if (id >= 0) {
			TransportController.getInstance().removeLocation(id);
		}
	}
}
