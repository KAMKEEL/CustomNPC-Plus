package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.data.Animation;

public class SubGuiAnimationOptions extends SubGuiInterface implements ITextfieldListener {
    private final Animation animation;

    public SubGuiAnimationOptions(Animation animation) {
        this.animation = animation;
        setBackground("smallbg.png");
    }

    @Override
    public void initGui() {
        super.initGui();
        //
        //ticks - button
        this.addLabel(new GuiNpcLabel(10, "animation.tickDuration", guiLeft + 5, guiTop + 16));
        this.addTextField(new GuiNpcTextField(10, this, guiLeft + 80, guiTop + 12, 40, 15, animation.tickDuration + ""));
        this.getTextField(10).integersOnly = true;
        this.getTextField(10).setMinMaxDefault(1, Integer.MAX_VALUE, 50);
        //
        //whileStanding - button
        this.addLabel(new GuiNpcLabel(11, "animation.whileStanding", guiLeft + 5, guiTop + 36));
        this.addButton(new GuiNpcButton(11, guiLeft + 80, guiTop + 30, 30, 20, new String[]{"gui.yes", "gui.no"}, animation.whileStanding ? 0 : 1));
        //
        //whileAttacking - button
        this.addLabel(new GuiNpcLabel(12, "animation.whileAttacking", guiLeft + 5, guiTop + 56));
        this.addButton(new GuiNpcButton(12, guiLeft + 80, guiTop + 50, 30, 20, new String[]{"gui.yes", "gui.no"}, animation.whileAttacking ? 0 : 1));
        //
        //whileMoving - button
        this.addLabel(new GuiNpcLabel(13, "animation.whileMoving", guiLeft + 5, guiTop + 76));
        this.addButton(new GuiNpcButton(13, guiLeft + 80, guiTop + 70, 30, 20, new String[]{"gui.yes", "gui.no"}, animation.whileMoving ? 0 : 1));
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        super.actionPerformed(guibutton);
        int value = ((GuiNpcButton)guibutton).getValue();

        if (guibutton.id == 11) {
            animation.whileStanding = value == 0;
        } else if (guibutton.id == 12) {
            animation.whileAttacking = value == 0;
        } else if (guibutton.id == 13) {
            animation.whileMoving = value == 0;
        }

        initGui();
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        if (textfield.id == 10) {
            animation.tickDuration = textfield.getInteger();
        }
    }
}
