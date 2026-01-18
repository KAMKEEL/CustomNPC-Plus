package noppes.npcs.controllers.data;

import kamkeel.npcs.network.packets.request.script.EffectScriptPacket;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.handler.IScriptHookHandler;
import noppes.npcs.scripted.event.player.PlayerEvent;

/**
 * Script handler for CustomEffect scripts.
 * Manages a single script container for effect hooks (onEffectAdd, onEffectTick, onEffectRemove).
 */
public class EffectScript extends SingleScriptHandler implements IScriptHandlerPacket {
    
    /** The effect ID for packet communication. -1 if not bound to an effect. */
    private int effectId = -1;
    
    /**
     * Create an unbound EffectScript (for server-side use).
     */
    public EffectScript() {
    }
    
    /**
     * Create an EffectScript bound to a specific effect (for GUI use).
     * @param effectId The ID of the CustomEffect
     */
    public EffectScript(int effectId) {
        this.effectId = effectId;
    }
    
    @Override
    public String getHookContext() {
        return IScriptHookHandler.CONTEXT_EFFECT;
    }
    
    /**
     * Convenience method to call a script with a typed event.
     */
    public void callScript(ScriptType type, PlayerEvent.EffectEvent event) {
        callScript(type.function, event);
    }
    
    @Override
    public void requestData() {
        if (effectId >= 0) 
            EffectScriptPacket.Get(effectId);
    }
    
    @Override
    public void sendSavePacket(int index, int totalCount, NBTTagCompound nbt) {
        if (effectId >= 0) 
            EffectScriptPacket.Save(effectId, index, totalCount, nbt);
    }

    public enum ScriptType {
        OnEffectAdd("onEffectAdd"),
        OnEffectTick("onEffectTick"),
        OnEffectRemove("onEffectRemove");

        public final String function;

        ScriptType(String functionName) {
            this.function = functionName;
        }
    }
}
