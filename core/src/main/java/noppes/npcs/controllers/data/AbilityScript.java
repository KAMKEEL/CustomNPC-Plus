package noppes.npcs.controllers.data;

import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import noppes.npcs.constants.ScriptContext;
/**
 * Script handler for ability scripts.
 * Manages a single script container for ability hooks (abilityStart, abilityTick, abilityComplete, etc.).
 */
public class AbilityScript extends SingleScriptHandler implements IScriptHandlerPacket {

    /**
     * The ability ID (UUID) for packet communication. Empty if not bound.
     */
    private String abilityId = "";

    /**
     * Create an unbound AbilityScript (for server-side use).
     */
    public AbilityScript() {
    }

    /**
     * Create an AbilityScript bound to a specific ability (for GUI use).
     *
     * @param abilityId The UUID of the ability
     */
    public AbilityScript(String abilityId) {
        this.abilityId = abilityId != null ? abilityId : "";
    }

    @Override
    public ScriptContext getContext() {
        return ScriptContext.ABILITY;
    }

    @Override
    public IScriptUnit createJaninoScriptUnit() {
        return new EventJaninoScript(ScriptContext.ABILITY);
    }

    @Override
    public String noticeString() {
        return !abilityId.isEmpty() ? "Ability[" + abilityId + "]" : "Ability";
    }

    @Override
    public void requestData() {
        if (!abilityId.isEmpty())
            AbilityScriptPacket.Get(abilityId);
    }

    @Override
    public void sendSavePacket(int index, int totalCount, INbt nbt) {
        if (!abilityId.isEmpty())
            AbilityScriptPacket.Save(abilityId, index, totalCount, nbt);
    }
}
