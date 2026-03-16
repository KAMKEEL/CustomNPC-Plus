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
 * Script handler for CustomEffect scripts.
 * Manages a single script container for effect hooks (onEffectAdd, onEffectTick, onEffectRemove).
 */
public class EffectScript extends SingleScriptHandler implements IScriptHandlerPacket {

    /**
     * The effect ID for packet communication. -1 if not bound to an effect.
     */
    private int effectId = -1;

    /**
     * Create an unbound EffectScript (for server-side use).
     */
    public EffectScript() {
    }

    /**
     * Create an EffectScript bound to a specific effect (for GUI use).
     *
     * @param effectId The ID of the CustomEffect
     */
    public EffectScript(int effectId) {
        this.effectId = effectId;
    }

    @Override
    public ScriptContext getContext() {
        return ScriptContext.EFFECT;
    }

    @Override
    public IScriptUnit createJaninoScriptUnit() {
        return new EventJaninoScript(ScriptContext.EFFECT);
    }

    @Override
    public String noticeString() {
        return effectId >= 0 ? "CustomEffect[" + effectId + "]" : "CustomEffect";
    }


    @Override
    public void requestData() {
        if (effectId >= 0)
            EffectScriptPacket.Get(effectId);
    }

    @Override
    public void sendSavePacket(int index, int totalCount, INbt nbt) {
        if (effectId >= 0)
            EffectScriptPacket.Save(effectId, index, totalCount, nbt);
    }

}
