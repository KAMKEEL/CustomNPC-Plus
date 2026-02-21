package kamkeel.npcs.controllers.data.ability.conditions;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.UserType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.gui.builder.FieldDef;
import noppes.npcs.client.gui.select.GuiQuestSelection;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.Quest;

import java.util.List;

public class ConditionQuestCompleted extends AbilityCondition {
    private int questId = -1;

    public ConditionQuestCompleted() {
        this.typeId = "condition.cnpc.quest_completed";
        this.name = "condition.quest_completed";
        this.userType = UserType.PLAYER_ONLY;
    }

    @Override
    protected boolean checkEntity(EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) return false;
        if (questId < 0) return false;

        EntityPlayer player = (EntityPlayer) entity;
        return PlayerData.get(player).questData.hasFinishedQuest(questId);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getConditionDefinitions(List<FieldDef> defs) {
        defs.add(FieldDef.subGuiField("condition.select_quest",
            () -> new GuiQuestSelection(questId),
            gui -> {
                GuiQuestSelection sel = (GuiQuestSelection) gui;
                if (sel.selectedQuest != null) {
                    questId = sel.selectedQuest.id;
                }
            })
            .buttonLabel(() -> {
                if (questId < 0) return "None";
                Quest q = QuestController.Instance.quests.get(questId);
                return q != null ? q.title : "ID:" + questId;
            })
            .clearable(() -> questId = -1));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public String getConditionSummary() {
        String filterLabel = StatCollector.translateToLocal(getFilter().toString());
        String questName = "None";
        if (questId >= 0) {
            Quest q = QuestController.Instance.quests.get(questId);
            questName = q != null ? q.title : "ID:" + questId;
        }
        return "[" + filterLabel + "] Quest: " + questName;
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setInteger("questId", questId);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        questId = nbt.getInteger("questId");
    }

    @Override
    public boolean isConfigured() {
        return questId >= 0;
    }

    public int getQuestId() {
        return questId;
    }

    public void setQuestId(int questId) {
        this.questId = questId;
    }
}
