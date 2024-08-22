package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.data.Frame;

public class SubGuiAnimationFrame extends SubGuiInterface implements ITextfieldListener {
    private final Frame editingFrame;

    public SubGuiAnimationFrame(Frame editingFrame) {
        this.editingFrame = editingFrame;
        setBackground("smallbg.png");
        this.closeOnEsc = true;
    }

    @Override
    public void initGui() {
        super.initGui();
        //
        //customized - button, enables all the following options.
        this.addLabel(new GuiNpcLabel(10, "animation.customized", guiLeft + 5, guiTop + 16));
        this.addButton(new GuiNpcButton(10, guiLeft + 65, guiTop + 10, 30, 20, new String[]{"gui.yes", "gui.no"}, editingFrame.isCustomized() ? 0 : 1));
        if (editingFrame.isCustomized()) {
            //
            //speed - textfield
            this.addLabel(new GuiNpcLabel(11, "stats.speed", guiLeft + 5, guiTop + 36));
            this.addTextField(new GuiNpcTextField(11, this, guiLeft + 65, guiTop + 32, 30, 15, editingFrame.speed + ""));
            this.getTextField(11).floatsOnly = true;
            this.getTextField(11).setMinMaxDefaultFloat(0, Float.MAX_VALUE, 1.0F);
            //
            //smooth - button
            this.addLabel(new GuiNpcLabel(12, "animation.smoothing", guiLeft + 5, guiTop + 58));
            this.addButton(new GuiNpcButton(12, guiLeft + 65, guiTop + 52, 60, 20, new String[]{"animation.smooth", "animation.linear", "gui.none"}, editingFrame.smooth));
            //
            //ticks - button
            this.addLabel(new GuiNpcLabel(13, "animation.tickType", guiLeft + 5, guiTop + 78));
            this.addButton(new GuiNpcButton(13, guiLeft + 65, guiTop + 72, 75, 20, new String[]{"animation.renderTicks", "animation.mcTicks"}, editingFrame.renderTicks ? 0 : 1));
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        super.actionPerformed(guibutton);
        int value = ((GuiNpcButton)guibutton).getValue();

        if (guibutton.id == 10 && editingFrame != null) {
            editingFrame.setCustomized(!editingFrame.isCustomized());
        } else if (guibutton.id == 12 && editingFrame != null) {
            editingFrame.smooth = (byte) value;
        } else if (guibutton.id == 13 && editingFrame != null) {
            editingFrame.renderTicks = value == 0;
        }

        initGui();
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        if (textfield.id == 11 && this.editingFrame != null) {
            this.editingFrame.speed = textfield.getFloat();
        }
    }
}
