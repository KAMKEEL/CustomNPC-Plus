package noppes.npcs.controllers.data;


import java.util.HashMap;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.IPlayerFactionData;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.IWorld;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.EventHooks;

public class PlayerFactionData implements IPlayerFactionData {
    private final PlayerData parent;
    public HashMap<Integer, Integer> factionData = new HashMap<Integer, Integer>();

    public PlayerFactionData() {
        parent = null;
    }

    public PlayerFactionData(PlayerData parent) {
        this.parent = parent;
    }

    public void loadNBTData(INbt compound) {
        HashMap<Integer, Integer> factionData = new HashMap<Integer, Integer>();
        if (compound == null)
            return;
        INbtList list = compound.getTagList("FactionData", 10);
        if (list == null) {
            return;
        }

        for (int i = 0; i < list.tagCount(); i++) {
            INbt INbt = list.getCompoundTagAt(i);
            factionData.put(INbt.getInteger("Faction"), INbt.getInteger("Points"));
        }
        this.factionData = factionData;
    }

    public void saveNBTData(INbt compound) {
        INbtList list = new INbtList();
        for (int faction : factionData.keySet()) {
            INbt INbt = new INbt();
            INbt.setInteger("Faction", faction);
            INbt.setInteger("Points", factionData.get(faction));
            list.appendTag(INbt);
        }

        compound.setTag("FactionData", list);
    }

    public int getFactionPoints(int id) {
        if (!factionData.containsKey(id)) {
            Faction faction = FactionController.getInstance().get(id);
            factionData.put(id, faction == null ? -1 : faction.defaultPoints);
        }
        return factionData.get(id);
    }

    public void increasePoints(int factionId, int points, IPlayer player) {
        if (EventHooks.onFactionPoints(player, new FactionEvent.FactionPoints((IPlayer) NpcAPI.Instance().getIEntity((IPlayer) player), FactionController.getInstance().get(factionId), points < 0, points)))
            return;

        if (!factionData.containsKey(factionId)) {
            Faction faction = FactionController.getInstance().get(factionId);
            factionData.put(factionId, faction == null ? -1 : faction.defaultPoints);
        }
        factionData.put(factionId, factionData.get(factionId) + points);
    }

    public INbt getPlayerGuiData() {
        INbt compound = new INbt();
        saveNBTData(compound);

        INbtList list = new INbtList();
        for (int id : factionData.keySet()) {
            Faction faction = FactionController.getInstance().get(id);
            if (faction == null || faction.hideFaction)
                continue;
            INbt com = new INbt();
            faction.writeNBT(com);
            list.appendTag(com);
        }
        compound.setTag("FactionList", list);

        return compound;
    }

    public int getPoints(int id) {
        return this.getFactionPoints(id);
    }

    public void addPoints(int id, int points) {
        if (parent != null) {
            this.increasePoints(id, points, parent.player);
        }
    }

    public void setPoints(int id, int points) {
        if (parent != null) {
            increasePoints(id, points - getFactionPoints(id), parent.player);
        }
    }
}
