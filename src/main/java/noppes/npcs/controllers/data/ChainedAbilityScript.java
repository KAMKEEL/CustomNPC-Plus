package noppes.npcs.controllers.data;

import kamkeel.npcs.network.packets.request.script.ChainedAbilityScriptPacket;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.constants.ScriptContext;
import noppes.npcs.janino.EventJaninoScript;

/**
 * Script handler for chained ability scripts.
 * Manages a single script container for chained ability hooks.
 */
public class ChainedAbilityScript extends SingleScriptHandler implements IScriptHandlerPacket {

    /**
     * The chained ability ID (UUID) for packet communication. Empty if not bound.
     */
    private String chainId = "";

    /**
     * Create an unbound ChainedAbilityScript (for server-side use).
     */
    public ChainedAbilityScript() {
    }

    /**
     * Create a ChainedAbilityScript bound to a specific chained ability (for GUI use).
     *
     * @param chainId The UUID of the chained ability
     */
    public ChainedAbilityScript(String chainId) {
        this.chainId = chainId != null ? chainId : "";
    }

    @Override
    public IScriptUnit createJaninoScriptUnit() {
        return new EventJaninoScript(ScriptContext.CHAINED_ABILITY);
    }

    @Override
    public ScriptContext getContext() {
        return ScriptContext.CHAINED_ABILITY;
    }

    @Override
    public String noticeString() {
        return !chainId.isEmpty() ? "ChainedAbility[" + chainId + "]" : "ChainedAbility";
    }

    @Override
    public void requestData() {
        if (!chainId.isEmpty())
            ChainedAbilityScriptPacket.Get(chainId);
    }

    @Override
    public void sendSavePacket(int index, int totalCount, NBTTagCompound nbt) {
        if (!chainId.isEmpty())
            ChainedAbilityScriptPacket.Save(chainId, index, totalCount, nbt);
    }
}
