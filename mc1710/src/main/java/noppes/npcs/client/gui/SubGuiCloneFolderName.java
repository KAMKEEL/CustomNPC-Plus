package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.controllers.data.CloneFolder;

public class SubGuiCloneFolderName extends SubGuiInterface {
    private String originalName;
    public boolean cancelled = true;
    private String folderName = "";

    public SubGuiCloneFolderName(String existingName) {
        this.originalName = existingName;
        this.folderName = existingName;
        xSize = 200;
        ySize = 80;
        this.closeOnEsc = true;
    }

    @Override
    public void initGui() {
        super.initGui();

        String title = isRename() ? "Rename Folder" : "New Folder";
        addLabel(new GuiNpcLabel(0, title, guiLeft + 5, guiTop + 5));
        addTextField(new GuiNpcTextField(0, this, fontRendererObj, guiLeft + 5, guiTop + 20, 190, 20, folderName));

        addButton(new GuiNpcButton(0, guiLeft + 5, guiTop + 50, 90, 20, "gui.done"));
        addButton(new GuiNpcButton(1, guiLeft + 100, guiTop + 50, 90, 20, "gui.cancel"));
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        if (guibutton.id == 0) {
            // Done
            folderName = getTextField(0).getText().trim();
            if (CloneFolder.isValidName(folderName)) {
                cancelled = false;
                close();
            }
        }
        if (guibutton.id == 1) {
            // Cancel
            cancelled = true;
            close();
        }
    }

    public boolean isRename() {
        return originalName != null && !originalName.isEmpty();
    }

    public String getFolderName() {
        return folderName;
    }

    public String getOriginalName() {
        return originalName;
    }

    @Override
    public void save() {
    }
}
