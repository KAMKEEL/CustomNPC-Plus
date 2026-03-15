package noppes.npcs.scripted.wrapper;

import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityAction;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.DataAbilities;
import noppes.npcs.api.ability.IAbility;
import noppes.npcs.api.ability.IDataAbilities;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.List;

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
        List<Ability> live = data.getAbilities();
        IAbility[] copies = new IAbility[live.size()];
        for (int i = 0; i < live.size(); i++) {
            copies[i] = live.get(i).deepCopy();
        }
        return copies;
    }

    @Override
    public void addAbility(IAbility ability) {
        if (ability instanceof Ability) {
            Ability a = (Ability) ability;
            // Validate ability allows NPCs
            if (!a.getAllowedBy().allowsNpc()) {
                return; // Ability is PLAYER_ONLY
            }
            data.addAbility(a);
        }
    }

    @Override
    public void addAbilityReference(String key) {
        // Validate ability exists and allows NPCs (peek avoids deep copy)
        Ability ability = AbilityController.Instance.peekAbility(key);
        if (ability == null) {
            return; // Ability doesn't exist
        }
        if (!ability.getAllowedBy().allowsNpc()) {
            return; // Ability is PLAYER_ONLY
        }
        // Store by UUID/registry key for stable reference
        String canonicalKey = ability.getId() != null ? ability.getId() : key;
        data.addAbilityReference(canonicalKey);
    }

    @Override
    public void removeAbility(String abilityId) {
        data.removeAbility(abilityId);
    }

    @Override
    public IAbility getAbility(String abilityId) {
        Ability a = data.getAbility(abilityId);
        return a != null ? a.deepCopy() : null;
    }

    @Override
    public boolean hasAbility(String abilityId) {
        return data.getAbility(abilityId) != null;
    }

    @Override
    public boolean isAbilityReference(String abilityId) {
        List<AbilityAction> slots = data.getAbilityActions();
        for (int i = 0; i < slots.size(); i++) {
            AbilityAction slot = slots.get(i);
            if (slot.isAbilityReference()) {
                if (slot.getReferenceId().equals(abilityId)) return true;
            } else if (!slot.isReference()) {
                Ability a = slot.getAbility();
                if (a != null && abilityId.equals(a.getId())) return false;
            }
        }
        return false;
    }

    @Override
    public boolean convertToInline(String abilityId) {
        List<AbilityAction> slots = data.getAbilityActions();
        for (int i = 0; i < slots.size(); i++) {
            AbilityAction slot = slots.get(i);
            if (slot.isAbilityReference() && slot.getReferenceId().equals(abilityId)) {
                return data.convertToInline(i);
            }
        }
        return false;
    }

    @Override
    public void clearAbilities() {
        data.clearAbilities();
    }

    @Override
    public IAbility getCurrentAbility() {
        Ability a = data.getCurrentAbility();
        return a != null ? a.deepCopy() : null;
    }

    @Override
    public IAbility getSourceAbility(String abilityId) {
        return data.getAbility(abilityId);
    }

    @Override
    public IAbility getSourceCurrentAbility() {
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
    public void completeCurrentAbility() {
        data.completeCurrentAbility();
    }

    @Override
    public int getGlobalCooldown() {
        return (int) data.getRemainingCooldown();
    }

    @Override
    public void setGlobalCooldown(int ticks) {
        if (ticks <= 0) {
            data.resetCooldown();
        } else {
            data.setCooldownEndTime(npc.worldObj.getTotalWorldTime() + ticks);
        }
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
    public boolean executeAbility(String key) {
        return executeAbility(key, null);
    }

    @Override
    public boolean executeAbility(String key, Object target) {
        // Validate ability exists and allows NPCs (peek avoids deep copy)
        Ability ability = AbilityController.Instance.peekAbility(key);
        if (ability == null) {
            return false; // Ability doesn't exist
        }
        if (!ability.getAllowedBy().allowsNpc()) {
            return false; // Ability is PLAYER_ONLY
        }
        EntityLivingBase targetEntity = resolveTarget(target);
        return data.executeAbility(key, targetEntity);
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
