package kamkeel.npcs.controllers.data.attribute.requirement;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import java.util.HashMap;
import java.util.Map;

public class RequirementCheckerRegistry {
    private static final Map<String, IRequirementChecker> checkers = new HashMap<>();

    public static void registerChecker(String key, IRequirementChecker checker) {
        if (!checkers.containsKey(key)) {
            checkers.put(key, checker);
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
}
