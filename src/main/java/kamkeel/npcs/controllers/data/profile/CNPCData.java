package kamkeel.npcs.controllers.data.profile;

import kamkeel.npcs.controllers.SyncController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerTradeData;

import java.util.ArrayList;
import java.util.List;

public class CNPCData implements IProfileData {

    @Override
    public String getTagName() {
        return "CNPC+";
    }

    @Override
    public NBTTagCompound getCurrentNBT(EntityPlayer player) {
        PlayerData customNPCData = PlayerData.get(player);
        NBTTagCompound compound = customNPCData.getNBT();
        compound.removeTag(PlayerTradeData.NBT_KEY);

        return compound;
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

        NBTTagCompound sharedData = new NBTTagCompound();
        customNPCData.tradeData.writeToNBT(sharedData);

        if (replace.hasNoTags()) {
            PlayerData newData = new PlayerData();
            newData.player = player;
            customNPCData.setNBT((NBTTagCompound) newData.getNBT().copy());
        } else {
            customNPCData.setNBT(replace);
        }

        customNPCData.tradeData.readFromNBT(sharedData);
        customNPCData.updateClient = true;
    }

    @Override
    public int getSwitchPriority() {
        return 0;
    }

    @Override
    public ProfileOperation verifySwitch(EntityPlayer player) {
        PlayerData playerData = PlayerData.get(player);
        if (playerData.partyUUID != null)
            return ProfileOperation.error("Cannot switch while in Party");

        return ProfileOperation.success("");
    }

    @Override
    public List<ProfileInfoEntry> getInfo(EntityPlayer player, NBTTagCompound compound) {
        PlayerData playerData = new PlayerData();
        playerData.player = player;
        playerData.setNBT(compound);

        List<ProfileInfoEntry> info = new ArrayList<>();
        info.add(new ProfileInfoEntry("profile.info.quest.finished", 0x60fa57, playerData.questData.finishedQuests.size(), 0xFFFFFF));
        info.add(new ProfileInfoEntry("profile.info.quest.active", 0xf75336, playerData.questData.activeQuests.size(), 0xFFFFFF));
        info.add(new ProfileInfoEntry("profile.info.dialog.read", 0x47acf5, playerData.dialogData.dialogsRead.size(), 0xFFFFFF));
        return info;
    }
}
