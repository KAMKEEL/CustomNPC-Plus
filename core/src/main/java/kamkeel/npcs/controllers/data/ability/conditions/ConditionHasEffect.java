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
    protected boolean checkEntity(IEntityLivingBase IEntity) {
        if (!(IEntity instanceof IPlayer)) return false;
        if (effectId < 0) return false;

        IPlayer player = (IPlayer) IEntity;
        return CustomEffectController.getInstance().hasEffect(player, effectId, effectIndex);
    }

    @ClientOnly
    @Override
    public void getConditionDefinitions(List<FieldDef> defs) {
        defs.add(FieldDef.subGuiField("condition.select_effect",
            () -> new SubGuiCustomEffectSelect(effectId, effectIndex),
            gui -> {
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

    @ClientOnly
    @Override
    public String getConditionSummary() {
        String filterLabel = PlatformServiceHolder.get().translateToLocal(getFilter().toString());
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
    public void writeTypeNBT(INbt nbt) {
        nbt.setInteger("effectId", effectId);
        nbt.setInteger("effectIndex", effectIndex);
    }

    @Override
    public void readTypeNBT(INbt nbt) {
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
