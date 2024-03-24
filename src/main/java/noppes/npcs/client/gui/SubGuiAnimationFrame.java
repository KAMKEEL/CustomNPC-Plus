package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.data.Frame;

public class SubGuiAnimationFrame extends SubGuiInterface implements ITextfieldListener {
    private final Frame editingFrame;

    public SubGuiAnimationFrame(Frame editingFrame) {
        this.editingFrame = editingFrame;
        setBackground("smallbg.png");
    }

    @Override
    public void initGui() {
        super.initGui();
        //
        //customized - button, enables all the following options.
        this.addLabel(new GuiNpcLabel(10, "animation.customized", guiLeft + 5, guiTop + 16));
        this.addButton(new GuiNpcButton(10, guiLeft + 75, guiTop + 10, 30, 20, new String[]{"gui.yes", "gui.no"}, editingFrame.isCustomized() ? 0 : 1));
        if (editingFrame.isCustomized()) {
            //
            //speed - textfield
            this.addLabel(new GuiNpcLabel(11, "stats.speed", guiLeft + 5, guiTop + 42));
            this.addTextField(new GuiNpcTextField(11, this, guiLeft + 75, guiTop + 37, 30, 15, editingFrame.speed + ""));
            this.getTextField(11).floatsOnly = true;
            this.getTextField(11).setMinMaxDefaultFloat(0, Float.MAX_VALUE, 1.0F);
            //
            //smooth - button
            this.addLabel(new GuiNpcLabel(12, "animation.smoothing", guiLeft + 5, guiTop + 63));
            this.addButton(new GuiNpcButton(12, guiLeft + 75, guiTop + 55, 60, 20, new String[]{"animation.smooth", "animation.linear", "gui.none"}, editingFrame.smooth));
            //
            //tick duration - textfield
            this.addLabel(new GuiNpcLabel(13, "animation.tickDuration", guiLeft + 5, guiTop + 84));
            this.addTextField(new GuiNpcTextField(13, this, guiLeft + 75, guiTop + 79, 40, 15, editingFrame.tickDuration + ""));
            this.getTextField(13).integersOnly = true;
            this.getTextField(13).setMinMaxDefault(1, Integer.MAX_VALUE, 50);
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        super.actionPerformed(guibutton);
        if (this.editingFrame == null) {
            return;
        }

        int value = ((GuiNpcButton)guibutton).getValue();
        if (guibutton.id == 10) {
            editingFrame.setCustomized(!editingFrame.isCustomized());
        } else if (guibutton.id == 12) {
            editingFrame.smooth = (byte) value;
        }

        initGui();
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        if (this.editingFrame == null) {
            return;
        }

        if (textfield.id == 11) {
            this.editingFrame.speed = textfield.getFloat();
        } else if (textfield.id == 13) {
            this.editingFrame.tickDuration = textfield.getInteger();
        }
    }
}
