package noppes.npcs.client.gui.script;

import kamkeel.npcs.network.packets.request.script.item.LinkedItemScriptPacket;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.handler.IScriptHookHandler;
import noppes.npcs.client.gui.global.GuiNPCManageLinked;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.constants.ScriptContext;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptHookController;
import noppes.npcs.controllers.data.IScriptUnit;
import noppes.npcs.controllers.data.LinkedItem;
import noppes.npcs.controllers.data.LinkedItemScript;

import java.util.ArrayList;

public class GuiScriptLinkedItem extends GuiScriptInterface {

    public final LinkedItem linkedItem;

    public GuiScriptLinkedItem(GuiNPCManageLinked parent, LinkedItem linkedItem) {
        super();
        this.parent = parent;
        this.linkedItem = linkedItem;
        this.handler = new LinkedItemScript();
        this.singleContainer = true;

        this.hookList = new ArrayList<>(ScriptHookController.Instance.getAllHooks(IScriptHookHandler.CONTEXT_LINKED_ITEM));

        LinkedItemScriptPacket.Get(linkedItem.id);
    }

    @Override
    protected ScriptContext getScriptContext() {
        return ScriptContext.ITEM;
    }

    protected void setHandlerContainer(IScriptUnit container) {
        ((LinkedItemScript) handler).container = container;
    }

    @Override
    public void setGuiData(NBTTagCompound compound) {
        setGuiDataWithOldContainer(compound);
    }

    protected void sendSavePacket(int index, int totalCount, NBTTagCompound scriptNBT) {
        LinkedItemScriptPacket.Save(linkedItem.id, index, totalCount, scriptNBT);
    }

    @Override
    public void save() {
        saveWithPackets();
    }
}
