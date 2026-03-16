package noppes.npcs.controllers;

import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import kamkeel.npcs.controllers.ProfileController;
import kamkeel.npcs.controllers.data.profile.Profile;
import noppes.npcs.config.ConfigMain;

import java.util.Map;

public class ProfileConfigSync {
    public static INbt writeToNBT(IPlayer player) {
        INbt compound = new INbt();
        if (!ConfigMain.ProfilesEnabled || ProfileController.Instance == null) {
            return compound;
        }

        Profile profile = ProfileController.Instance.getProfile(player);
        if (profile == null) {
            return compound;
        }

        INbtList questList = new INbtList();
        for (Map.Entry<Integer, Long> entry : profile.sharedQuestTimestamps.entrySet()) {
            INbt questEntry = new INbt();
            questEntry.setInteger("Quest", entry.getKey());
            questEntry.setLong("Date", entry.getValue());
            questList.appendTag(questEntry);
        }
        compound.setTag("SharedQuestTimestamps", questList);
        return compound;
    }
}
