package noppes.npcs.client.gui.roles;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.select.GuiSoundSelection;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;

public class SubGuiNpcConversationLine extends SubGuiInterface implements ITextfieldListener, ISubGuiListener {
    public String line;
    public String sound;

    public SubGuiNpcConversationLine(String line, String sound) {
        this.line = line;
        this.sound = sound;
        setBackground("menubg.png");
        xSize = 256;
        ySize = 216;
        closeOnEsc = true;
    }

    public void initGui() {
        super.initGui();

        addLabel(new GuiNpcLabel(0, "Line", guiLeft + 4, guiTop + 10));
        addTextField(new GuiNpcTextField(0, this, fontRendererObj, guiLeft + 4, guiTop + 22, 200, 20, line));

        addButton(new GuiNpcButton(1, guiLeft + 4, guiTop + 55, 90, 20, "Select Sound"));
        addButton(new GuiNpcButton(2, guiLeft + 96, guiTop + 55, 20, 20, "X"));
        addLabel(new GuiNpcLabel(1, sound, guiLeft + 4, guiTop + 81));

        addButton(new GuiNpcButton(66, guiLeft + 162, guiTop + 192, 90, 20, "gui.done"));
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        line = textfield.getText();
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        int id = guibutton.id;
        if (id == 1) {
            setSubGui(new GuiSoundSelection(sound));
        }
        if (id == 2) {
            sound = "";
            initGui();
        }
        if (id == 66) {
            close();
        }
    }

    @Override
    public void subGuiClosed(SubGuiInterface subgui) {
        GuiSoundSelection gss = (GuiSoundSelection) subgui;
        if (gss.selectedResource != null) {
            sound = gss.selectedResource.toString();
            initGui();
        }
    }

}
