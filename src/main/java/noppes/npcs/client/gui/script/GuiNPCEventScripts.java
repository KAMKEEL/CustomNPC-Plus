package noppes.npcs.client.gui.script;

import noppes.npcs.controllers.data.DataScript;
import noppes.npcs.controllers.data.IScriptHandlerPacket;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCEventScripts extends GuiScriptInterface {
    public GuiNPCEventScripts(EntityNPCInterface npc) {
        this.handler = new DataScript(npc);
        if (this.handler instanceof IScriptHandlerPacket)
            ((IScriptHandlerPacket) this.handler).requestData();
    }
}
