package noppes.npcs.controllers.data;

import noppes.npcs.api.IBlock;

public interface IScriptBlockHandler extends INpcScriptHandler {

    IBlock getBlock();
}
