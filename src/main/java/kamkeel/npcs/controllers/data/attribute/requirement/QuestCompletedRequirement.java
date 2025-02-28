package kamkeel.npcs.controllers.data.attribute.requirement;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.data.PlayerData;

public class QuestCompletedRequirement implements IRequirementChecker {

    @Override
    public String getKey() {
        return "cnpc_quest_completed";
    }

    @Override
    public boolean check(EntityPlayer player, NBTTagCompound nbt) {
        if(nbt.hasKey(getKey())) {
            int questID = nbt.getInteger(getKey());
            return PlayerData.get(player).questData.hasFinishedQuest(questID);
        }
        return true;
    }
}
