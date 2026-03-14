package noppes.npcs.controllers.data;

import noppes.npcs.api.handler.data.ITransportCategory;
import noppes.npcs.api.handler.data.ITransportLocation;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.core.NBT;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class TransportCategory implements ITransportCategory {
    public int id = -1;
    public String title = "";
    public HashMap<Integer, TransportLocation> locations;

    public TransportCategory() {
        locations = new HashMap<Integer, TransportLocation>();
    }

    public Vector<TransportLocation> getDefaultLocations() {
        Vector<TransportLocation> list = new Vector<TransportLocation>();
        for (TransportLocation loc : locations.values())
            if (loc.isDefault())
                list.add(loc);
        return list;
    }

    public void readNBT(INbt compound) {
        id = compound.getInteger("CategoryId");
        title = compound.getString("CategoryTitle");

        INbtList locs = compound.getTagList("CategoryLocations", 10);
        if (locs == null || locs.size() == 0)
            return;

        for (int ii = 0; ii < locs.size(); ii++) {
            TransportLocation location = new TransportLocation();
            location.readNBT(locs.getCompound(ii));
            location.category = this;
            locations.put(location.id, location);
        }
    }

    public void writeNBT(INbt compound) {
        compound.setInteger("CategoryId", id);
        compound.setString("CategoryTitle", title);
        INbtList locs = NBT.list();
        for (TransportLocation location : locations.values()) {
            locs.addCompound(location.writeNBT());
        }
        compound.setTagList("CategoryLocations", locs);
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
        // Overridden in version-specific code
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
        // Overridden in version-specific code
    }
}
