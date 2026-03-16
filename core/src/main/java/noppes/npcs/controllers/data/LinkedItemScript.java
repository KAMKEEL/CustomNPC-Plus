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
 * Script handler for LinkedItem scripts.
 * Manages a single script container for linked item hooks.
 */
public class LinkedItemScript extends SingleScriptHandler implements IScriptHandlerPacket {

    /**
     * The linked item ID for packet communication. -1 if not bound.
     */
    private int linkedItemId = -1;

    /**
     * Create an unbound LinkedItemScript (for server-side use).
     */
    public LinkedItemScript() {
    }

    /**
     * Create a LinkedItemScript bound to a specific linked item (for GUI use).
     *
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
    public IScriptUnit createJaninoScriptUnit() {
        return new EventJaninoScript(ScriptContext.LINKED_ITEM);
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
    public void sendSavePacket(int index, int totalCount, INbt nbt) {
        if (linkedItemId >= 0)
            LinkedItemScriptPacket.Save(linkedItemId, index, totalCount, nbt);

    }
}
