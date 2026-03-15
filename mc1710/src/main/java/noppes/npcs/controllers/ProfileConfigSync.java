package noppes.npcs.controllers;

import kamkeel.npcs.controllers.ProfileController;
import kamkeel.npcs.controllers.data.profile.Profile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.config.ConfigMain;

import java.util.Map;

public class ProfileConfigSync {
    public static NBTTagCompound writeToNBT(EntityPlayer player) {
        NBTTagCompound compound = new NBTTagCompound();
        if (!ConfigMain.ProfilesEnabled || ProfileController.Instance == null) {
            return compound;
        }

        Profile profile = ProfileController.Instance.getProfile(player);
        if (profile == null) {
            return compound;
        }

        NBTTagList questList = new NBTTagList();
        for (Map.Entry<Integer, Long> entry : profile.sharedQuestTimestamps.entrySet()) {
            NBTTagCompound questEntry = new NBTTagCompound();
            questEntry.setInteger("Quest", entry.getKey());
            questEntry.setLong("Date", entry.getValue());
            questList.appendTag(questEntry);
        }
        compound.setTag("SharedQuestTimestamps", questList);
        return compound;
    }
}
