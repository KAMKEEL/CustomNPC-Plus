package kamkeel.npcs.controllers.data.profile;

import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import kamkeel.npcs.controllers.SyncController;
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
    public INbt getCurrentNBT(IPlayer player) {
        PlayerData customNPCData = PlayerData.get(player);
        INbt compound = customNPCData.getNBT();
        compound.removeTag(PlayerTradeData.NBT_KEY);

        return compound;
    }

    @Override
    public void save(IPlayer player) {
        PlayerData customNPCData = PlayerData.get(player);
        customNPCData.save();
        SyncController.syncPlayerData((IPlayer) player, false);
    }

    @Override
    public void setNBT(IPlayer player, INbt replace) {
        PlayerData customNPCData = PlayerData.get(player);

        INbt sharedData = new INbt();
        customNPCData.tradeData.writeToNBT(sharedData);

        if (replace.hasNoTags()) {
            PlayerData newData = new PlayerData();
            newData.player = player;
            customNPCData.setNBT((INbt) newData.getNBT().copy());
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
    public ProfileOperation verifySwitch(IPlayer player) {
        PlayerData playerData = PlayerData.get(player);
        if (playerData.partyUUID != null)
            return ProfileOperation.error("Cannot switch while in Party");

        if (playerData.abilityData.isExecutingAbility())
            return ProfileOperation.error("Cannot switch while performing an Ability");

        return ProfileOperation.success("");
    }

    @Override
    public List<ProfileInfoEntry> getInfo(IPlayer player, INbt compound) {
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
