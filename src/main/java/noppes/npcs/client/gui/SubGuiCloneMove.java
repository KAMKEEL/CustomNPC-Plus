package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.controllers.ClientCloneController;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.controllers.data.CloneFolder;

import java.util.ArrayList;

public class SubGuiCloneMove extends SubGuiInterface {
    public boolean cancelled = true;
    private int destTab = 1;
    private String destFolder = null;

    private int[] tabValues;
    private String[] folderValues;

    public SubGuiCloneMove() {
        xSize = 200;
        ySize = 80;
        closeOnEsc = true;
    }

    @Override
    public void initGui() {
        super.initGui();

        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Integer> tabs = new ArrayList<>();
        ArrayList<String> folders = new ArrayList<>();

        for (int i = 1; i <= 15; i++) {
            labels.add("Tab " + i);
            tabs.add(i);
            folders.add(null);
        }

        if (ClientCloneController.Instance != null) {
            for (CloneFolder f : ClientCloneController.Instance.getFolderList()) {
                labels.add(f.name);
                tabs.add(-1);
                folders.add(f.name);
            }
        }

        tabValues = new int[tabs.size()];
        for (int i = 0; i < tabs.size(); i++) tabValues[i] = tabs.get(i);
        folderValues = folders.toArray(new String[0]);

        String[] labelArr = labels.toArray(new String[0]);
        int selectedIndex = 0;
        if (destFolder != null) {
            for (int i = 0; i < folderValues.length; i++) {
                if (destFolder.equals(folderValues[i])) {
                    selectedIndex = i;
                    break;
                }
            }
        } else if (destTab >= 1 && destTab <= 15) {
            selectedIndex = destTab - 1;
        }

        addLabel(new GuiNpcLabel(0, "Move to:", guiLeft + 5, guiTop + 5));
        addButton(new GuiButtonBiDirectional(2, guiLeft + 5, guiTop + 20, 190, 20, labelArr, selectedIndex));
        addButton(new GuiNpcButton(0, guiLeft + 5, guiTop + 50, 90, 20, "gui.done"));
        addButton(new GuiNpcButton(1, guiLeft + 100, guiTop + 50, 90, 20, "gui.cancel"));
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        if (guibutton.id == 0) {
            int index = getButton(2).getValue();
            if (index >= 0 && index < tabValues.length) {
                destTab = tabValues[index];
                destFolder = folderValues[index];
            }
            cancelled = false;
            close();
        }
        if (guibutton.id == 1) {
            cancelled = true;
            close();
        }
    }

    public int getDestTab() {
        return destTab;
    }

    public String getDestFolder() {
        return destFolder;
    }

    @Override
    public void save() {
    }
}
