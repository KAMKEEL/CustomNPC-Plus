package noppes.npcs.client.gui.model;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.data.ModelData;
import noppes.npcs.entity.data.ModelRotatePart;

public class GuiModelRotate extends GuiModelInterface implements ISliderListener {

    private GuiScreen parent;
    private int type = 6;
    private ModelRotatePart part;

    private GuiNpcSlider rotateX;
    private GuiNpcSlider rotateY;
    private GuiNpcSlider rotateZ;

    public GuiModelRotate(GuiScreen parent, ModelData data, EntityCustomNpc npc) {
        super(npc);
        this.parent = parent;
        this.xOffset = 100;
        ySize = 230;
    }

    @Override
    public void initGui() {
        super.initGui();

        int y = guiTop;
        addLabel(new GuiNpcLabel(27, "movement.rotation", guiLeft + 55, y + 5, 0xFFFFFF));
        addButton(new GuiNpcButton(14, guiLeft + 110, y, 60, 20, new String[]{"gui.enabled", "gui.disabled"}, playerdata.enableRotation ? 0 : 1));
        y += 24;
        if (playerdata.enableRotation) {
            addLabel(new GuiNpcLabel(26, "gui.settings", guiLeft + 55, y + 5, 0xFFFFFF));
            if (type == 6) {
                addButton(new GuiNpcButton(30, guiLeft + 120, y += 14, 60, 20, new String[]{"gui.yes", "gui.no"}, playerdata.rotation.whileStanding ? 0 : 1));
                addLabel(new GuiNpcLabel(30, "puppet.standing", guiLeft + 30, y + 5, 0xFFFFFF));
                addButton(new GuiNpcButton(31, guiLeft + 120, y += 22, 60, 20, new String[]{"gui.yes", "gui.no"}, playerdata.rotation.whileMoving ? 0 : 1));
                addLabel(new GuiNpcLabel(31, "puppet.walking", guiLeft + 30, y + 5, 0xFFFFFF));
                addButton(new GuiNpcButton(32, guiLeft + 120, y += 22, 60, 20, new String[]{"gui.yes", "gui.no"}, playerdata.rotation.whileAttacking ? 0 : 1));
                addLabel(new GuiNpcLabel(32, "puppet.attacking", guiLeft + 30, y + 5, 0xFFFFFF));
                y += 24;
            } else {
                addButton(new GuiNpcButton(6, guiLeft + 110, y, 60, 20, "selectServer.edit"));
                y += 24;
            }

            addLabel(new GuiNpcLabel(20, "model.head", guiLeft + 55, y + 5, 0xFFFFFF));
            if (type == 0) {
                drawSlider(y, playerdata.rotation.head);
                y += 90;
            } else {
                addButton(new GuiNpcButton(0, guiLeft + 110, y, 60, 20, "selectServer.edit"));
                y += 24;
            }

            addLabel(new GuiNpcLabel(21, "model.body", guiLeft + 55, y + 5, 0xFFFFFF));
            if (type == 1) {
                drawSlider(y, playerdata.rotation.body);
                y += 90;
            } else {
                addButton(new GuiNpcButton(1, guiLeft + 110, y, 60, 20, "selectServer.edit"));
                y += 24;
            }

            addLabel(new GuiNpcLabel(22, "model.larm", guiLeft + 55, y + 5, 0xFFFFFF));
            if (type == 2) {
                drawSlider(y, playerdata.rotation.larm);
                y += 90;
            } else {
                addButton(new GuiNpcButton(2, guiLeft + 110, y, 60, 20, "selectServer.edit"));
                y += 24;
            }

            addLabel(new GuiNpcLabel(23, "model.rarm", guiLeft + 55, y + 5, 0xFFFFFF));
            if (type == 3) {
                drawSlider(y, playerdata.rotation.rarm);
                y += 90;
            } else {
                addButton(new GuiNpcButton(3, guiLeft + 110, y, 60, 20, "selectServer.edit"));
                y += 24;
            }

            addLabel(new GuiNpcLabel(24, "model.lleg", guiLeft + 55, y + 5, 0xFFFFFF));
            if (type == 4) {
                drawSlider(y, playerdata.rotation.lleg);
                y += 90;
            } else {
                addButton(new GuiNpcButton(4, guiLeft + 110, y, 60, 20, "selectServer.edit"));
                y += 24;
            }

            addLabel(new GuiNpcLabel(25, "model.rleg", guiLeft + 55, y + 5, 0xFFFFFF));
            if (type == 5) {
                drawSlider(y, playerdata.rotation.rleg);
                y += 90;
            } else {
                addButton(new GuiNpcButton(5, guiLeft + 110, y, 60, 20, "selectServer.edit"));
                y += 24;
            }
        }
    }

    private void drawSlider(int y, ModelRotatePart config) {
        part = config;
        addButton(new GuiNpcButton(29, guiLeft + 100, y, 80, 20, new String[]{"gui.enabled", "gui.disabled"}, config.disabled ? 1 : 0));
        y += 22;
        addLabel(new GuiNpcLabel(10, "X", guiLeft, y + 5, 0xFFFFFF));
        rotateX = new GuiNpcSlider(this, 10, guiLeft + 50, y, config.rotationX + 0.5f);
        addSlider(rotateX);
        addButton(new GuiNpcButton(170, guiLeft + 8, y, 40, 20, "Reset"));
        y += 22;
        addLabel(new GuiNpcLabel(11, "Y", guiLeft, y + 5, 0xFFFFFF));
        rotateY = new GuiNpcSlider(this, 11, guiLeft + 50, y, config.rotationY + 0.5f);
        addSlider(rotateY);
        addButton(new GuiNpcButton(171, guiLeft + 8, y, 40, 20, "Reset"));
        y += 22;
        addLabel(new GuiNpcLabel(12, "Z", guiLeft, y + 5, 0xFFFFFF));
        rotateZ = new GuiNpcSlider(this, 12, guiLeft + 50, y, config.rotationZ + 0.5f);
        addSlider(rotateZ);
        addButton(new GuiNpcButton(172, guiLeft + 8, y, 40, 20, "Reset"));
    }


    @Override
    protected void actionPerformed(GuiButton btn) {
        super.actionPerformed(btn);
        if (btn.id < 7) {
            type = btn.id;
            initGui();
        }
        if (!(btn instanceof GuiNpcButton))
            return;

        GuiNpcButton button = (GuiNpcButton) btn;
        if (btn.id == 14) {
            playerdata.enableRotation = button.getValue() == 0;
            initGui();
        }
        if (btn.id == 29) {
            part.disabled = button.getValue() == 1;
        }
        if (btn.id == 30) {
            playerdata.rotation.whileStanding = button.getValue() == 0;
        }
        if (btn.id == 31) {
            playerdata.rotation.whileMoving = button.getValue() == 0;
        }
        if (btn.id == 32) {
            playerdata.rotation.whileAttacking = button.getValue() == 0;
        }
        if (btn.id == 170 || btn.id == 171 || btn.id == 172) {
            if (btn.id == 170) {
                part.rotationX = 0.0f;
                rotateX.sliderValue = 0.5f;
                int percent = (int) ((rotateX.sliderValue) * 360);
                rotateX.setString(percent + "%");
            } else if (btn.id == 171) {
                part.rotationY = 0.0f;
                rotateY.sliderValue = 0.5f;
                int percent = (int) ((rotateY.sliderValue) * 360);
                rotateY.setString(percent + "%");
            } else {
                part.rotationZ = 0.0f;
                rotateZ.sliderValue = 0.5f;
                int percent = (int) ((rotateZ.sliderValue) * 360);
                rotateZ.setString(percent + "%");
            }
            npc.updateHitbox();
        }
    }

    @Override
    public void close() {
        this.mc.displayGuiScreen(parent);
    }

    @Override
    public void mouseDragged(GuiNpcSlider slider) {
        int percent = (int) ((slider.sliderValue) * 360);
        slider.setString(percent + "%");

        if (slider.id == 10)
            part.rotationX = slider.sliderValue - 0.5f;
        if (slider.id == 11)
            part.rotationY = slider.sliderValue - 0.5f;
        if (slider.id == 12)
            part.rotationZ = slider.sliderValue - 0.5f;
        npc.updateHitbox();
    }

    @Override
    public void mousePressed(GuiNpcSlider slider) {

    }

    @Override
    public void mouseReleased(GuiNpcSlider slider) {
    }
}
