//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.controllers;

import java.util.List;
import java.util.Map;
import cpw.mods.fml.common.eventhandler.Event;
import noppes.npcs.EventScriptContainer;
import noppes.npcs.constants.EnumScriptType;

public interface IScriptHandler {
    void runScript(EnumScriptType var1, Event var2);

    boolean isClient();

    boolean getEnabled();

    void setEnabled(boolean var1);

    String getLanguage();

    void setLanguage(String var1);

    List<EventScriptContainer> getScripts();

    String noticeString();

    Map<Long, String> getConsoleText();

    void clearConsole();
}
