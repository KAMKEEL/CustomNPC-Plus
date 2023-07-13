package noppes.npcs.controllers.data;

import cpw.mods.fml.common.eventhandler.Event;
import noppes.npcs.constants.EnumScriptType;

public interface INpcScriptHandler extends IScriptHandler {
    void callScript(EnumScriptType var1, Event var2);
}
