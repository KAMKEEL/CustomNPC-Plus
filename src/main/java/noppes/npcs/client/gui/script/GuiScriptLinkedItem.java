package noppes.npcs.client.gui.script;

import kamkeel.npcs.network.packets.request.script.item.LinkedItemScriptPacket;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.gui.global.GuiNPCManageLinked;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.constants.ScriptContext;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.data.LinkedItem;
import noppes.npcs.controllers.data.LinkedItemScript;

public class GuiScriptLinkedItem extends GuiScriptInterface {

    public final LinkedItem linkedItem;

    public GuiScriptLinkedItem(GuiNPCManageLinked parent, LinkedItem linkedItem) {
        super();
        this.parent = parent;
        this.linkedItem = linkedItem;
        this.handler = new LinkedItemScript();
        this.singleContainer = true;

        hookList.add(EnumScriptType.LINKED_ITEM_BUILD.function);
        hookList.add(EnumScriptType.LINKED_ITEM_VERSION.function);
        hookList.add(EnumScriptType.INIT.function);
        hookList.add(EnumScriptType.TICK.function);
        hookList.add(EnumScriptType.TOSSED.function);
        hookList.add(EnumScriptType.PICKEDUP.function);
        hookList.add(EnumScriptType.SPAWN.function);
        hookList.add(EnumScriptType.INTERACT.function);
        hookList.add(EnumScriptType.RIGHT_CLICK.function);
        hookList.add(EnumScriptType.ATTACK.function);
        hookList.add(EnumScriptType.START_USING_ITEM.function);
        hookList.add(EnumScriptType.USING_ITEM.function);
        hookList.add(EnumScriptType.STOP_USING_ITEM.function);
        hookList.add(EnumScriptType.FINISH_USING_ITEM.function);

        LinkedItemScriptPacket.Get(linkedItem.id);
    }

    @Override
    protected ScriptContext getScriptContext() {
        return ScriptContext.ITEM;
    }

    protected void setHandlerContainer(ScriptContainer container) {
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
