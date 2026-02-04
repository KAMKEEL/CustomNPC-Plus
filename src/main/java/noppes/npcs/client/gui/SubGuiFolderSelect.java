package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.SubGuiInterface;

import java.util.ArrayList;
import java.util.List;

public class SubGuiFolderSelect extends SubGuiInterface implements ICustomScrollListener {

    public String selectedFolder;
    private final List<String> folderList;
    private GuiCustomScroll folderScroll;

    public SubGuiFolderSelect(List<String> folders, String currentSelection) {
        this.folderList = new ArrayList<>(folders);
        this.selectedFolder = currentSelection;
        setBackground("menubg.png");
        xSize = 200;
        ySize = 220;
        closeOnEsc = true;
    }

    @Override
    public void initGui() {
        super.initGui();

        folderScroll = new GuiCustomScroll(this, 0);
        folderScroll.setSize(xSize - 8, ySize - 50);
        folderScroll.guiLeft = guiLeft + 4;
        folderScroll.guiTop = guiTop + 4;
        folderScroll.setList(new ArrayList<>(folderList));
        if (selectedFolder != null) {
            folderScroll.setSelected(selectedFolder);
        }
        addScroll(folderScroll);

        int btnW = (xSize - 12) / 2;
        addButton(new GuiNpcButton(0, guiLeft + 4, guiTop + ySize - 44, btnW, 20, "gui.done"));
        addButton(new GuiNpcButton(1, guiLeft + 4 + btnW + 4, guiTop + ySize - 44, btnW, 20, "gui.cancel"));

        getButton(0).enabled = folderScroll.hasSelected();
    }

    @Override
    public void customScrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
        getButton(0).enabled = folderScroll.hasSelected();
    }

    @Override
    public void customScrollDoubleClicked(String selection, GuiCustomScroll guiCustomScroll) {
        selectedFolder = selection;
        close();
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        if (guibutton.id == 0) {
            selectedFolder = folderScroll.getSelected();
            close();
        }
        if (guibutton.id == 1) {
            selectedFolder = null;
            close();
        }
    }

    @Override
    public void save() {
    }
}
