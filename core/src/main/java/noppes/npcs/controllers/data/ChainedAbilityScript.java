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
    public void sendSavePacket(int index, int totalCount, INbt nbt) {
        if (!chainId.isEmpty())
            ChainedAbilityScriptPacket.Save(chainId, index, totalCount, nbt);
    }
}
