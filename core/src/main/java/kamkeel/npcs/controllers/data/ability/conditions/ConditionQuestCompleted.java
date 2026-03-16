package kamkeel.npcs.controllers.data.ability.conditions;

import noppes.npcs.constants.ClientOnly;
import kamkeel.npcs.util.IVector3;
import kamkeel.npcs.controllers.data.ability.preview.PreviewEntityHandler;
import kamkeel.npcs.controllers.data.ability.gui.SubGuiAbilityConfig;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityConfigCallback;
import kamkeel.npcs.controllers.data.ability.gui.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.IChainedAbilityFieldProvider;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityFieldProvider;
import noppes.npcs.entity.EntityNPCInterface;
import kamkeel.npcs.entity.EntityEnergyDome;
import kamkeel.npcs.entity.EntityEnergyBarrier;
import kamkeel.npcs.entity.EntityEnergyPanel;
import kamkeel.npcs.entity.EntityAbilityOrb;
import kamkeel.npcs.entity.EntityAbilityLaser;
import kamkeel.npcs.entity.EntityAbilityDisc;
import kamkeel.npcs.entity.EntityAbilityBeam;
import kamkeel.npcs.entity.EntityEnergyProjectile;
import kamkeel.npcs.util.ByteBufUtils;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import kamkeel.npcs.controllers.data.ability.enums.UserType;
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
    protected boolean checkEntity(IEntityLivingBase IEntity) {
        if (!(IEntity instanceof IPlayer)) return false;
        if (questId < 0) return false;

        IPlayer player = (IPlayer) IEntity;
        PlayerData data = PlayerData.get(player);
        if (data == null || data.questData == null) return false;
        return data.questData.hasFinishedQuest(questId);
    }

    @ClientOnly
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

    @ClientOnly
    @Override
    public String getConditionSummary() {
        String filterLabel = PlatformServiceHolder.get().translateToLocal(getFilter().toString());
        String questName = "None";
        if (questId >= 0) {
            Quest q = QuestController.Instance.quests.get(questId);
            questName = q != null ? q.title : "ID:" + questId;
        }
        return "[" + filterLabel + "] Quest: " + questName;
    }

    @Override
    public void writeTypeNBT(INbt nbt) {
        nbt.setInteger("questId", questId);
    }

    @Override
    public void readTypeNBT(INbt nbt) {
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
