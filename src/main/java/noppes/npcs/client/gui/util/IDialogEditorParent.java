package noppes.npcs.client.gui.util;

import java.util.HashMap;

public interface IDialogEditorParent {
    HashMap<String, Integer> getDialogData();
    GuiCustomScroll getDialogScroll();
    String getDialogQuestName();
    void setDialogQuestName(String name);
}
