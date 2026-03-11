package noppes.npcs.controllers.data;

import noppes.npcs.api.IBlock;

public interface IScriptBlockHandler extends IScriptHandler {

    IBlock getBlock();
}
