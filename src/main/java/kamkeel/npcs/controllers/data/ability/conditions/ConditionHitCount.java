package kamkeel.npcs.controllers.data.ability.conditions;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.gui.builder.FieldDef;
import noppes.npcs.entity.EntityNPCInterface;

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
    protected boolean checkEntity(EntityLivingBase entity) {
        // Not used — this condition overrides check() directly
        return false;
    }

    @Override
    public boolean check(EntityLivingBase caster, EntityLivingBase target) {
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

    @SideOnly(Side.CLIENT)
    @Override
    public void getConditionDefinitions(List<FieldDef> defs) {
        defs.add(FieldDef.intField("condition.required_hits", this::getRequiredHits, this::setRequiredHits).min(1));
        defs.add(FieldDef.intField("condition.within_ticks", this::getWithinTicks, this::setWithinTicks).min(0));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public String getConditionSummary() {
        return "[Caster] Hit " + requiredHits + " times in " + withinTicks + " ticks";
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setInteger("requiredHits", requiredHits);
        nbt.setInteger("withinTicks", withinTicks);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        requiredHits = Math.max(0, nbt.getInteger("requiredHits"));
        withinTicks = Math.max(0, nbt.getInteger("withinTicks"));
    }
}
