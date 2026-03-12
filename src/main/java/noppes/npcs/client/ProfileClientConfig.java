package noppes.npcs.client;

import kamkeel.npcs.controllers.ProfileController;
import kamkeel.npcs.controllers.data.profile.Profile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.config.ConfigMain;

import java.util.HashMap;
import java.util.Map;

public class ProfileClientConfig {
    private static volatile Map<Integer, Long> sharedQuestTimestamps = new HashMap<>();

    public static void readFromNBT(NBTTagCompound compound) {
        if (compound == null) return;

        Map<Integer, Long> timestamps = new HashMap<>();
        if (compound.hasKey("SharedQuestTimestamps")) {
            NBTTagList list = compound.getTagList("SharedQuestTimestamps", 10);
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound entry = list.getCompoundTagAt(i);
                timestamps.put(entry.getInteger("Quest"), entry.getLong("Date"));
            }
        }
        sharedQuestTimestamps = timestamps;
    }

    public static void reset() {
        sharedQuestTimestamps = new HashMap<>();
    }

    /**
     * Whether profiles are enabled, checking the correct source for each side.
     */
    public static boolean isProfilesEnabled() {
        if (ProfileController.Instance != null)
            return ConfigMain.ProfilesEnabled;
        return ClientCacheHandler.allowProfiles;
    }

    /**
     * Check if a quest has a shared timestamp, resolving from server or client cache.
     */
    public static boolean hasSharedQuest(EntityPlayer player, int questId) {
        if (ProfileController.Instance != null) {
            Profile profile = ProfileController.Instance.getProfile(player);
            return profile != null && profile.sharedQuestTimestamps.containsKey(questId);
        }
        return sharedQuestTimestamps.containsKey(questId);
    }

    /**
     * Get the shared quest timestamp, resolving from server or client cache.
     */
    public static Long getSharedQuestTimestamp(EntityPlayer player, int questId) {
        if (ProfileController.Instance != null) {
            Profile profile = ProfileController.Instance.getProfile(player);
            return profile != null ? profile.sharedQuestTimestamps.get(questId) : null;
        }
        return sharedQuestTimestamps.get(questId);
    }
}
