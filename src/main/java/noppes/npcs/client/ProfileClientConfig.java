package noppes.npcs.client;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

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

    public static Map<Integer, Long> getSharedQuestTimestamps() {
        return sharedQuestTimestamps;
    }

    public static boolean hasSharedQuest(int questId) {
        return sharedQuestTimestamps.containsKey(questId);
    }

    public static Long getSharedQuestTimestamp(int questId) {
        return sharedQuestTimestamps.get(questId);
    }
}
