package noppes.npcs.controllers.data;

import kamkeel.npcs.network.packets.request.script.AbilityScriptPacket;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.constants.ScriptContext;
import noppes.npcs.janino.EventJaninoScript;

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
    public void sendSavePacket(int index, int totalCount, NBTTagCompound nbt) {
        if (!abilityId.isEmpty())
            AbilityScriptPacket.Save(abilityId, index, totalCount, nbt);
    }
}
