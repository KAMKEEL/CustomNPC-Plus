package noppes.npcs.controllers.data;

import noppes.npcs.api.handler.IPlayerTransportData;
import noppes.npcs.api.handler.data.ITransportLocation;
import noppes.npcs.platform.nbt.INBTCompound;
import noppes.npcs.platform.nbt.INBTList;
import noppes.npcs.core.NBT;

import java.util.HashSet;

public class PlayerTransportData implements IPlayerTransportData {
    public HashSet<Integer> transports = new HashSet<Integer>();

    public PlayerTransportData() {
    }

    public void loadNBTData(INBTCompound compound) {
        HashSet<Integer> dialogsRead = new HashSet<Integer>();
        if (compound == null)
            return;
        INBTList list = compound.getList("TransportData", 10);
        if (list == null) {
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            INBTCompound nbttagcompound = list.getCompound(i);
            dialogsRead.add(nbttagcompound.getInteger("Transport"));
        }
        this.transports = dialogsRead;
    }

    public void saveNBTData(INBTCompound compound) {
        INBTList list = NBT.list();
        for (int dia : transports) {
            INBTCompound nbttagcompound = NBT.compound();
            nbttagcompound.setInteger("Transport", dia);
            list.addCompound(nbttagcompound);
        }

        compound.setList("TransportData", list);
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
        return null; // Resolved in version-specific code
    }

    public ITransportLocation[] getTransports() {
        return new ITransportLocation[0]; // Resolved in version-specific code
    }

    public void removeTransport(int id) {
        transports.remove(id);
    }
}
