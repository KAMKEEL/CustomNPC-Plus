package noppes.npcs.client.gui.model;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.data.ModelData;
import noppes.npcs.entity.data.ModelScalePart;

public class GuiModelScale extends GuiModelInterface implements ISliderListener {

    private final GuiScreen parent;
    private int type = 0;
    private final int offset = 10;

    private GuiNpcSlider scaleWidth;
    private GuiNpcSlider scaleHeight;
    private GuiNpcSlider scaleDepth;

    public GuiModelScale(GuiScreen parent, ModelData data, EntityCustomNpc npc) {
        super(npc);
        this.parent = parent;
        this.xOffset = 100;
        ySize = 230;
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop + 2;

        addLabel(new GuiNpcLabel(20, "Head", guiLeft + 55, y + 5, 0xFFFFFF));
        if (type == 0) {
            drawSlider(y, playerdata.modelScale.head);
            y += 88;
        } else {
            addButton(new GuiNpcButton(0, guiLeft + 110, y, 60, 20, "Edit"));
            y += 24;
        }

        addLabel(new GuiNpcLabel(21, "Body", guiLeft + 55, y + 5, 0xFFFFFF));
        if (type == 1) {
            drawSlider(y, playerdata.modelScale.body);
            y += 88;
        } else {
            addButton(new GuiNpcButton(1, guiLeft + 110, y, 60, 20, "Edit"));
            y += 24;
        }

        addLabel(new GuiNpcLabel(22, "Arms", guiLeft + 55, y + 5, 0xFFFFFF));
        if (type == 2) {
            drawSlider(y, playerdata.modelScale.arms);
            y += 88;
        } else {
            addButton(new GuiNpcButton(2, guiLeft + 110, y, 60, 20, "Edit"));
            y += 24;
        }

        addLabel(new GuiNpcLabel(23, "Legs", guiLeft + 55, y + 5, 0xFFFFFF));
        if (type == 3) {
            drawSlider(y, playerdata.modelScale.legs);
            y += 88;
        } else {
            addButton(new GuiNpcButton(3, guiLeft + 110, y, 60, 20, "Edit"));
            y += 24;
        }
    }

    private void drawSlider(int y, ModelScalePart config) {
        y += 20;
        addLabel(new GuiNpcLabel(10, "Width", guiLeft - 25 + offset, y + 5, 0xFFFFFF));
        scaleWidth = new GuiNpcSlider(this, 10, guiLeft + 50 + offset, y, config.scaleX - 0.5f);
        addSlider(scaleWidth);
        addButton(new GuiNpcButton(170, guiLeft + 8 + offset, y, 40, 20, "Reset"));
        y += 22;
        addLabel(new GuiNpcLabel(11, "Height", guiLeft - 25 + offset, y + 5, 0xFFFFFF));
        scaleHeight = new GuiNpcSlider(this, 11, guiLeft + 50 + offset, y, config.scaleY - 0.5f);
        addSlider(scaleHeight);
        addButton(new GuiNpcButton(171, guiLeft + 8 + offset, y, 40, 20, "Reset"));
        y += 22;
        addLabel(new GuiNpcLabel(12, "Depth", guiLeft - 25 + offset, y + 5, 0xFFFFFF));
        scaleDepth = new GuiNpcSlider(this, 12, guiLeft + 50 + offset, y, config.scaleZ - 0.5f);
        addSlider(scaleDepth);
        addButton(new GuiNpcButton(172, guiLeft + 8 + offset, y, 40, 20, "Reset"));
    }


    @Override
    protected void actionPerformed(GuiButton btn) {
        super.actionPerformed(btn);

        if (btn.id < 4) {
            type = btn.id;
            initGui();
        } else {
            // Resetting Type
            ModelScalePart config = playerdata.modelScale.head;
            if (type == 1)
                config = playerdata.modelScale.body;
            else if (type == 2)
                config = playerdata.modelScale.arms;
            else if (type == 3)
                config = playerdata.modelScale.legs;

            if (btn.id == 170) {
                config.scaleX = 1.0f;
                scaleWidth.sliderValue = 0.5f;
                int percent = (int) (50 + scaleWidth.sliderValue * 100);
                scaleWidth.setString(percent + "%");
                npc.updateHitbox();
            } else if (btn.id == 171) {
                config.scaleY = 1.0f;
                scaleHeight.sliderValue = 0.5f;
                int percent = (int) (50 + scaleHeight.sliderValue * 100);
                scaleHeight.setString(percent + "%");
                npc.updateHitbox();
            } else if (btn.id == 172) {
                config.scaleZ = 1.0f;
                scaleDepth.sliderValue = 0.5f;
                int percent = (int) (50 + scaleDepth.sliderValue * 100);
                scaleDepth.setString(percent + "%");
                npc.updateHitbox();
            }
        }


    }

    @Override
    public void close() {
        this.mc.displayGuiScreen(parent);
    }

    @Override
    public void mouseDragged(GuiNpcSlider slider) {
        int percent = (int) (50 + slider.sliderValue * 100);
        slider.setString(percent + "%");
        ModelScalePart config = playerdata.modelScale.head;
        if (type == 1)
            config = playerdata.modelScale.body;
        else if (type == 2)
            config = playerdata.modelScale.arms;
        else if (type == 3)
            config = playerdata.modelScale.legs;

        if (slider.id == 10)
            config.scaleX = slider.sliderValue + 0.5f;
        if (slider.id == 11)
            config.scaleY = slider.sliderValue + 0.5f;
        if (slider.id == 12)
            config.scaleZ = slider.sliderValue + 0.5f;
        npc.updateHitbox();
    }

    @Override
    public void mousePressed(GuiNpcSlider slider) {
        allowRotate = false;
    }

    @Override
    public void mouseReleased(GuiNpcSlider slider) {
        allowRotate = true;
    }
}
