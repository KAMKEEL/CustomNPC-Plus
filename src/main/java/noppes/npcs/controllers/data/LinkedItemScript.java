package noppes.npcs.controllers.data;

import kamkeel.npcs.network.packets.request.script.item.LinkedItemScriptPacket;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.constants.ScriptContext;

/**
 * Script handler for LinkedItem scripts.
 * Manages a single script container for linked item hooks.
 */
public class LinkedItemScript extends SingleScriptHandler implements IScriptHandlerPacket {
    
    /** The linked item ID for packet communication. -1 if not bound. */
    private int linkedItemId = -1;
    
    /**
     * Create an unbound LinkedItemScript (for server-side use).
     */
    public LinkedItemScript() {
    }
    
    /**
     * Create a LinkedItemScript bound to a specific linked item (for GUI use).
     * @param linkedItemId The ID of the LinkedItem
     */
    public LinkedItemScript(int linkedItemId) {
        this.linkedItemId = linkedItemId;
    }
    
    @Override
    public ScriptContext getContext() {
        return ScriptContext.LINKED_ITEM;
    }
    
    @Override
    public String noticeString() {
        return "LinkedItem";
    }
    
    @Override
    public void requestData() {
        if (linkedItemId >= 0) 
            LinkedItemScriptPacket.Get(linkedItemId);
    }
    
    @Override
    public void sendSavePacket(int index, int totalCount, NBTTagCompound nbt) {
        if (linkedItemId >= 0) 
            LinkedItemScriptPacket.Save(linkedItemId, index, totalCount, nbt);
        
    }
}
