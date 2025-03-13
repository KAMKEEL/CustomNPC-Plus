package kamkeel.npcs.controllers.data.attribute.requirement;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RequirementCheckerRegistry {
    private static final Map<String, IRequirementChecker> checkers = new HashMap<>();

    public static void registerChecker(IRequirementChecker checker) {
        if (!checkers.containsKey(checker.getKey())) {
            checkers.put(checker.getKey(), checker);
        }
    }

    /**
     * Loops over all registered requirement keys.
     * For each key present in the NBT, its checker must return true.
     */
    public static boolean checkRequirements(EntityPlayer player, NBTTagCompound nbt) {
        for (Map.Entry<String, IRequirementChecker> entry : checkers.entrySet()) {
            String reqKey = entry.getKey();
            if (nbt.hasKey(reqKey)) {
                if (!entry.getValue().check(player, nbt)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static Set<String> getAllKeys() {
        return checkers.keySet();
    }

    public static IRequirementChecker getChecker(String key) {
        return checkers.get(key);
    }
}
