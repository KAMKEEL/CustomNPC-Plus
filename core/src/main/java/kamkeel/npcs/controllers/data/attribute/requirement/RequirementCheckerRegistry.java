package kamkeel.npcs.controllers.data.attribute.requirement;

import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
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
    public static boolean checkRequirements(IPlayer player, INbt nbt) {
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
