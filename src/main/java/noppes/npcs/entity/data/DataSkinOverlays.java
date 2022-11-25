package noppes.npcs.entity.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.SkinOverlay;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.api.ISkinOverlay;
import noppes.npcs.api.handler.IOverlayHandler;
import java.util.HashMap;
import java.util.Map;

public class DataSkinOverlays implements IOverlayHandler {
    public final Object parent;
    public HashMap<Integer, ISkinOverlay> overlayList = new HashMap<>();

    public DataSkinOverlays(Object parent) {
        this.parent = parent;
    }

    public void readFromNBT(NBTTagCompound nbtTagCompound) {
        HashMap<Integer,ISkinOverlay> skinOverlays = new HashMap<>();
        NBTTagList skinOverlayList = nbtTagCompound.getTagList("SkinOverlayData",10);
        for (int i = 0; i < skinOverlayList.tagCount(); i++) {
            int tagID = skinOverlayList.getCompoundTagAt(i).getInteger("SkinOverlayID");
            SkinOverlay skinOverlay = (SkinOverlay) SkinOverlay.overlayFromNBT(skinOverlayList.getCompoundTagAt(i));
            skinOverlay.parent = this;
            skinOverlays.put(tagID, skinOverlay);
        }
        this.overlayList = skinOverlays;
        this.updateClient();
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
        NBTTagList overlayList = new NBTTagList();
        if (!this.overlayList.isEmpty()) {
            for (Map.Entry<Integer,ISkinOverlay> overlayData : this.overlayList.entrySet()) {
                NBTTagCompound compound = new NBTTagCompound();
                compound.setInteger("SkinOverlayID", overlayData.getKey());
                compound = ((SkinOverlay) overlayData.getValue()).writeToNBT(compound);
                overlayList.appendTag(compound);
            }
        }
        nbttagcompound.setTag("SkinOverlayData",overlayList);
        return nbttagcompound;
    }

    public void updateClient() {
        if (parent != null) {
            if (parent instanceof PlayerData && ((PlayerData) parent).player != null) {
                NBTTagCompound compound = this.writeToNBT(new NBTTagCompound());
                ((PlayerData) parent).player.getEntityData().setTag("SkinOverlayData", compound.getTagList("SkinOverlayData", 10));
                Server.sendToAll(EnumPacketClient.PLAYER_UPDATE_SKIN_OVERLAYS, ((PlayerData) parent).player.getCommandSenderName(), compound);
            } else if (parent instanceof EntityNPCInterface) {
                ((EntityNPCInterface) parent).updateClient = true;
            }
        }
    }

    public void add(int id, ISkinOverlay data) {
        if (this.overlayList.size() >= CustomNpcs.SkinOverlayLimit) {
            return;
        }

        ((SkinOverlay) data).parent = this;
        this.overlayList.put(id, data);
        updateClient();
    }

    public ISkinOverlay get(int id) {
        return this.overlayList.get(id);
    }

    public boolean has(int id) {
        return this.overlayList.containsKey(id);
    }

    public boolean remove(int id) {
        boolean removed = this.overlayList.remove(id) != null;
        updateClient();
        return removed;
    }

    public int size() { return this.overlayList.size(); }

    public void clear() {
        this.overlayList.clear();
        updateClient();
    }
}
