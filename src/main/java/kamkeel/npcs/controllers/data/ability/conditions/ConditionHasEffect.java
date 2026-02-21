package kamkeel.npcs.controllers.data.ability.conditions;

import kamkeel.npcs.controllers.data.ability.UserType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.gui.advanced.SubGuiCustomEffectSelect;
import noppes.npcs.client.gui.builder.FieldDef;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.controllers.CustomEffectController;
import noppes.npcs.controllers.data.CustomEffect;

import java.util.HashMap;
import java.util.List;

public class ConditionHasEffect extends AbilityCondition {
    private int effectId = -1;
    private int effectIndex = 0;

    public ConditionHasEffect() {
        this.typeId = "condition.cnpc.has_effect";
        this.name = "condition.has_effect";
        this.userType = UserType.PLAYER_ONLY;
    }

    @Override
    protected boolean checkEntity(EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) return false;
        if (effectId < 0) return false;

        EntityPlayer player = (EntityPlayer) entity;
        return CustomEffectController.getInstance().hasEffect(player, effectId, effectIndex);
    }

    @Override
    public void getConditionDefinitions(List<FieldDef> defs) {
        defs.add(FieldDef.subGuiField("condition.select_effect",
            () -> new SubGuiCustomEffectSelect(effectId, effectIndex),
            (SubGuiInterface gui) -> {
                SubGuiCustomEffectSelect sel = (SubGuiCustomEffectSelect) gui;
                if (sel.getSelectedEffectId() >= 0) {
                    effectId = sel.getSelectedEffectId();
                    effectIndex = sel.getSelectedIndex();
                }
            })
            .buttonLabel(() -> {
                if (effectId < 0) return "None";
                CustomEffect effect = getEffect();
                return effect != null ? effect.getName() : "ID:" + effectId;
            })
            .clearable(() -> {
                effectId = -1;
                effectIndex = 0;
            }));
    }

    @Override
    public String getConditionSummary() {
        String filterLabel = StatCollector.translateToLocal(getFilter().toString());
        String effectName = "None";
        if (effectId >= 0) {
            CustomEffect effect = getEffect();
            effectName = effect != null ? effect.getName() : "ID:" + effectId;
        }
        return "[" + filterLabel + "] Effect: " + effectName;
    }

    @Override
    public boolean isConfigured() {
        return effectId >= 0;
    }

    private CustomEffect getEffect() {
        HashMap<Integer, CustomEffect> map = CustomEffectController.getInstance().getEffectMap(effectIndex);
        return map != null ? map.get(effectId) : null;
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setInteger("effectId", effectId);
        nbt.setInteger("effectIndex", effectIndex);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        effectId = nbt.getInteger("effectId");
        effectIndex = nbt.getInteger("effectIndex");
    }

    public int getEffectId() {
        return effectId;
    }

    public void setEffectId(int effectId) {
        this.effectId = effectId;
    }

    public int getEffectIndex() {
        return effectIndex;
    }

    public void setEffectIndex(int effectIndex) {
        this.effectIndex = effectIndex;
    }
}
