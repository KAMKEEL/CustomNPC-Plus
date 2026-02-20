package kamkeel.npcs.controllers.data.ability.conditions;

import kamkeel.npcs.controllers.data.ability.UserType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.gui.builder.FieldDef;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.Collections;
import java.util.List;

public class ConditionHitCount extends AbilityCondition{
    private int requiredHits = 3;
    private int withinTicks = 60;

    public ConditionHitCount() {
        this.typeId = "condition.cnpc.hit_count";
        this.name = "Hit Count";
        this.userType = UserType.NPC_ONLY;
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

    @Override
    public List<FieldDef> getAbilityDefinitions(List<FieldDef> defs) {
        return Collections.emptyList();
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setInteger("requiredHits", requiredHits);
        nbt.setInteger("withinTicks", withinTicks);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        requiredHits = nbt.getInteger("requiredHits");
        withinTicks = nbt.getInteger("withinTicks");
    }
}
