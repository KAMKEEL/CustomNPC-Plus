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
import java.util.List;

public class ConditionHitCount extends AbilityCondition {
    private int requiredHits = 3;
    private int withinTicks = 60;

    public ConditionHitCount() {
        this.typeId = "condition.cnpc.hit_count";
        this.name = "condition.hit_count";
        this.conditionFilter = ConditionFilter.CASTER;
    }

    @Override
    protected boolean checkEntity(IEntityLivingBase IEntity) {
        // Not used — this condition overrides check() directly
        return false;
    }

    @Override
    public boolean check(IEntityLivingBase caster, IEntityLivingBase target) {
        if (caster instanceof EntityNPCInterface) {
            EntityNPCInterface npc = (EntityNPCInterface) caster;
            if (npc.abilities != null) {
                return npc.abilities.getRecentHitCount(withinTicks) >= requiredHits;
            }
        }

        // For players, hit count tracking is not yet supported
        return false;
    }

    public int getRequiredHits() {
        return requiredHits;
    }

    public void setRequiredHits(int requiredHits) {
        this.requiredHits = Math.max(0, requiredHits);
    }

    public int getWithinTicks() {
        return withinTicks;
    }

    public void setWithinTicks(int withinTicks) {
        this.withinTicks = Math.max(0, withinTicks);
    }

    @ClientOnly
    @Override
    public void getConditionDefinitions(List<FieldDef> defs) {
        defs.add(FieldDef.intField("condition.required_hits", this::getRequiredHits, this::setRequiredHits).min(1));
        defs.add(FieldDef.intField("condition.within_ticks", this::getWithinTicks, this::setWithinTicks).min(0));
    }

    @ClientOnly
    @Override
    public String getConditionSummary() {
        return "[Caster] Hit " + requiredHits + " times in " + withinTicks + " ticks";
    }

    @Override
    public void writeTypeNBT(INbt nbt) {
        nbt.setInteger("requiredHits", requiredHits);
        nbt.setInteger("withinTicks", withinTicks);
    }

    @Override
    public void readTypeNBT(INbt nbt) {
        requiredHits = Math.max(0, nbt.getInteger("requiredHits"));
        withinTicks = Math.max(0, nbt.getInteger("withinTicks"));
    }
}
