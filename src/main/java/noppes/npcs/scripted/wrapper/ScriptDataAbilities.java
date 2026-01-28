package noppes.npcs.scripted.wrapper;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityController;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.DataAbilities;
import noppes.npcs.api.ability.IAbility;
import noppes.npcs.api.ability.IDataAbilities;
import noppes.npcs.entity.EntityNPCInterface;

/**
 * Script wrapper for DataAbilities providing API access to NPC abilities.
 */
public class ScriptDataAbilities implements IDataAbilities {

    private final DataAbilities data;
    private final EntityNPCInterface npc;

    public ScriptDataAbilities(EntityNPCInterface npc) {
        this.npc = npc;
        this.data = npc.abilities;
    }

    @Override
    public boolean isEnabled() {
        return data.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        data.enabled = enabled;
    }

    @Override
    public IAbility[] getAbilities() {
        return data.getAbilities().toArray(new IAbility[0]);
    }

    @Override
    public void addAbility(IAbility ability) {
        if (ability instanceof Ability) {
            data.addAbility((Ability) ability);
        }
    }

    @Override
    public void removeAbility(String abilityId) {
        data.removeAbility(abilityId);
    }

    @Override
    public IAbility getAbility(String abilityId) {
        return data.getAbility(abilityId);
    }

    @Override
    public boolean hasAbility(String abilityId) {
        return data.getAbility(abilityId) != null;
    }

    @Override
    public void clearAbilities() {
        data.clearAbilities();
    }

    @Override
    public IAbility getCurrentAbility() {
        return data.getCurrentAbility();
    }

    @Override
    public boolean isExecutingAbility() {
        return data.isExecutingAbility();
    }

    @Override
    public void interruptCurrentAbility() {
        data.interruptCurrentAbility(null, 0);
    }

    @Override
    public int getGlobalCooldown() {
        return (int) data.getRemainingCooldown();
    }

    @Override
    public void setGlobalCooldown(int ticks) {
        // Set the cooldown by manipulating the internal cooldown end time
        // Not directly exposed, but we can reset and let it tick down
        data.resetCooldown();
        // This is a best-effort implementation
    }

    @Override
    public void resetCooldowns() {
        data.resetCooldown();
    }

    @Override
    public boolean forceStartAbility(String abilityId) {
        return forceStartAbility(abilityId, null);
    }

    @Override
    public boolean forceStartAbility(String abilityId, Object target) {
        // Find the ability
        Ability ability = data.getAbility(abilityId);
        if (ability == null) {
            return false;
        }

        // Resolve target
        EntityLivingBase targetEntity = resolveTarget(target);

        // Use the direct force start method
        return data.forceStartAbility(ability, targetEntity);
    }

    @Override
    public boolean executePresetAbility(String presetName) {
        return executePresetAbility(presetName, null);
    }

    @Override
    public boolean executePresetAbility(String presetName, Object target) {
        // Resolve target
        EntityLivingBase targetEntity = resolveTarget(target);

        // Use the direct preset execution method
        return data.executePresetAbility(presetName, targetEntity);
    }

    @Override
    public IAbility createAbility(String typeId) {
        if (AbilityController.Instance == null) {
            return null;
        }
        return AbilityController.Instance.create(typeId);
    }

    /**
     * Resolve a target object to an EntityLivingBase.
     */
    private EntityLivingBase resolveTarget(Object target) {
        if (target == null) {
            // Try to use the NPC's current attack target
            return npc.getAttackTarget();
        }

        if (target instanceof EntityLivingBase) {
            return (EntityLivingBase) target;
        }

        if (target instanceof noppes.npcs.api.entity.IEntityLivingBase) {
            Object mcEntity = ((noppes.npcs.api.entity.IEntityLivingBase<?>) target).getMCEntity();
            if (mcEntity instanceof EntityLivingBase) {
                return (EntityLivingBase) mcEntity;
            }
        }

        return null;
    }
}
