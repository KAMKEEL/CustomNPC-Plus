package noppes.npcs.client.gui.model.custom;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.client.gui.util.GuiNPCStringSlot;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.SubGuiInterface;

import java.util.List;
import java.util.function.Consumer;

public class GuiStringSelection extends SubGuiInterface {
    public GuiNPCStringSlot slot;
    public Consumer<String> action;
    public String title;
    public List<String> options;

    public GuiStringSelection(GuiScreen parent, String title, List<String> options, Consumer<String> action) {
        drawDefaultBackground = false;
        this.parent = parent;
        this.action = action;
        this.title = title;
        this.options = options;
    }

    @Override
    public void initGui() {
        super.initGui();
        addLabel(new GuiNpcLabel(0, title, width / 2 - (this.fontRendererObj.getStringWidth(title) / 2), 20, 0xffffff));
        options.sort(String.CASE_INSENSITIVE_ORDER);
        slot = new GuiNPCStringSlot(options, this, false, 18);
        slot.registerScrollButtons(4, 5);
        this.addButton(new GuiNpcButton(2, width / 2 - 100, height - 44, 98, 20, "gui.back"));
    }

    @Override
    public void drawScreen(int i, int j, float f) {
        slot.drawScreen(i, j, f);
        super.drawScreen(i, j, f);
    }

    @Override
    public void doubleClicked() {
        action.accept(slot.selected);
        close();
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        int id = guibutton.id;
        if (id == 2) {
            close();
        }
    }
}