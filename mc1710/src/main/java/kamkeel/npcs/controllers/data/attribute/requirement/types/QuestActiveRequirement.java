package kamkeel.npcs.controllers.data.attribute.requirement.types;

import kamkeel.npcs.controllers.data.attribute.requirement.IRequirementChecker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.PlayerData;

public class QuestActiveRequirement implements IRequirementChecker {

    @Override
    public String getKey() {
        return "cnpc_quest_active";
    }

    @Override
    public String getTranslation() {
        return "quest.active";
    }

    @Override
    public String getTooltipValue(NBTTagCompound nbt) {
        if (nbt.hasKey(getKey())) {
            int questID = nbt.getInteger(getKey());
            if (QuestController.Instance.get(questID) != null) {
                return QuestController.Instance.get(questID).getName();
            }
        }
        return "null";
    }

    @Override
    public Object getValue(NBTTagCompound nbt) {
        if (nbt.hasKey(getKey())) {
            return nbt.getInteger(getKey());
        }
        return null;
    }

    @Override
    public void apply(NBTTagCompound nbt, Object value) {
        if (value instanceof Integer) {
            nbt.setInteger(getKey(), (Integer) value);
        }
    }

    @Override
    public boolean check(EntityPlayer player, NBTTagCompound nbt) {
        if (nbt.hasKey(getKey())) {
            int questID = nbt.getInteger(getKey());
            return PlayerData.get(player).questData.hasActiveQuest(questID);
        }
        return true;
    }
}
