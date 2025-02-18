package kamkeel.npcs.controllers.data;

import kamkeel.npcs.controllers.SyncController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.data.PlayerData;

public class CNPCData implements IProfileData {

    @Override
    public String getTagName() {
        return "CustomNPC+";
    }

    @Override
    public NBTTagCompound getCurrentNBT(EntityPlayer player) {
        PlayerData customNPCData = PlayerData.get(player);
        return customNPCData.getNBT();
    }

    @Override
    public void save(EntityPlayer player) {
        PlayerData customNPCData = PlayerData.get(player);
        customNPCData.save();
        SyncController.syncPlayerData((EntityPlayerMP) player, false);
    }

    @Override
    public void setNBT(EntityPlayer player, NBTTagCompound replace) {
        PlayerData customNPCData = PlayerData.get(player);
        if(replace.hasNoTags()){
            PlayerData newData = new PlayerData();
            newData.player = player;
            customNPCData.setNBT((NBTTagCompound) newData.getNBT().copy());
        }
        else {
            customNPCData.setNBT(replace);
        }
        customNPCData.updateClient = true;
    }
}
