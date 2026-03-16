package noppes.npcs.controllers.data;


import java.util.HashSet;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.ITransportLocation;
import noppes.npcs.api.handler.IPlayerTransportData;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.IWorld;
import noppes.npcs.core.NBT;

public class PlayerTransportData implements IPlayerTransportData {
    public HashSet<Integer> transports = new HashSet<Integer>();

    public PlayerTransportData() {
    }

    public void loadNBTData(INbt compound) {
        HashSet<Integer> dialogsRead = new HashSet<Integer>();
        if (compound == null)
            return;
        INbtList list = compound.getTagList("TransportData", 10);
        if (list == null) {
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            INbt INbt = list.getCompound(i);
            dialogsRead.add(INbt.getInteger("Transport"));
        }
        this.transports = dialogsRead;
    }

    public void saveNBTData(INbt compound) {
        INbtList list = NBT.list();
        for (int dia : transports) {
            INbt INbt = NBT.compound();
            INbt.setInteger("Transport", dia);
            list.addCompound(INbt);
        }

        compound.setTagList("TransportData", list);
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
