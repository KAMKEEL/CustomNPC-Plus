package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.scripted.ScriptParticle;

public class SubGuiScriptParticle extends SubGuiInterface implements ITextfieldListener {

    private ScriptParticle particle;

    public SubGuiScriptParticle(ScriptParticle particle) {
        this.particle = particle;
        setBackground("menubg.png");
        xSize = 350;
        ySize = 216;
        closeOnEsc = true;
    }

    @Override
    public void initGui() {
        super.initGui();
        int y = guiTop + 10;
        addLabel(new GuiNpcLabel(1, "display.texture", guiLeft + 5, y));
        addTextField(new GuiNpcTextField(1, this, fontRendererObj, guiLeft + 70, y - 5, 260, 20, particle.directory));

        y += 30;
        // maxAge (small int textfield)
        addLabel(new GuiNpcLabel(2, "particle.maxAge", guiLeft + 5, y));
        addTextField(new GuiNpcTextField(2, this, fontRendererObj, guiLeft + 70, y - 5, 50, 20, particle.maxAge + ""));
        getTextField(2).integersOnly = true;
        getTextField(2).setMinMaxDefault(1, Integer.MAX_VALUE, particle.maxAge);

        y += 30;
        // Width and Height (small int textfields)
        addLabel(new GuiNpcLabel(3, "effect.editor.width", guiLeft + 5, y));
        addTextField(new GuiNpcTextField(3, this, fontRendererObj, guiLeft + 70, y - 5, 50, 20, particle.width + ""));
        getTextField(3).integersOnly = true;
        getTextField(3).setMinMaxDefault(-1, Integer.MAX_VALUE, particle.width);

        addLabel(new GuiNpcLabel(4, "effect.editor.height", guiLeft + 140, y));
        addTextField(new GuiNpcTextField(4, this, fontRendererObj, guiLeft + 190, y - 5, 50, 20, particle.height + ""));
        getTextField(4).integersOnly = true;
        getTextField(4).setMinMaxDefault(-1, Integer.MAX_VALUE, particle.height);

        y += 30;
        // FacePlayer, Glows, noClip buttons (Yes/No) with width 40
        addLabel(new GuiNpcLabel(5, "particle.facePlayer", guiLeft + 5, y + 5));
        addButton(new GuiNpcButton(5, guiLeft + 70, y, 40, 20, new String[]{"gui.no", "gui.yes"}, particle.facePlayer ? 1 : 0));

        addLabel(new GuiNpcLabel(6, "display.glow", guiLeft + 120, y + 5));
        addButton(new GuiNpcButton(6, guiLeft + 170, y, 40, 20, new String[]{"gui.no", "gui.yes"}, particle.glows ? 1 : 0));

        addLabel(new GuiNpcLabel(7, "particle.noClip", guiLeft + 220, y + 5));
        addButton(new GuiNpcButton(7, guiLeft + 280, y, 40, 20, new String[]{"gui.no", "gui.yes"}, particle.noClip ? 1 : 0));

        y += 30;
        // Scale row: label "model.scale", then "X:" and "Y:" textfields.
        addLabel(new GuiNpcLabel(8, "model.scale", guiLeft + 5, y + 5));
        addLabel(new GuiNpcLabel(9, "X:", guiLeft + 100, y + 5));
        addTextField(new GuiNpcTextField(8, this, fontRendererObj, guiLeft + 120, y - 5, 40, 20, particle.scaleX1 + ""));
        addLabel(new GuiNpcLabel(10, "Y:", guiLeft + 170, y + 5));
        addTextField(new GuiNpcTextField(9, this, fontRendererObj, guiLeft + 190, y - 5, 40, 20, particle.scaleY1 + ""));

        // Done button at bottom right
        addButton(new GuiNpcButton(66, guiLeft + xSize - 50, guiTop + ySize - 30, 45, 20, "gui.done"));
    }

    @Override
    public void unFocused(GuiNpcTextField textfield) {
        if (textfield.id == 1) {
            particle.directory = textfield.getText();
        } else if (textfield.id == 2) {
            particle.maxAge = textfield.getInteger();
        } else if (textfield.id == 3) {
            particle.width = textfield.getInteger();
        } else if (textfield.id == 4) {
            particle.height = textfield.getInteger();
        } else if (textfield.id == 8) {
            // When scaleX1 is modified, update both scaleX1 and scaleX2.
            float value = Float.parseFloat(textfield.getText());
            particle.scaleX1 = value;
            particle.scaleX2 = value;
        } else if (textfield.id == 9) {
            // Update scaleY1 and scaleY2.
            float value = Float.parseFloat(textfield.getText());
            particle.scaleY1 = value;
            particle.scaleY2 = value;
        }
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        if (guibutton.id == 5) {
            particle.facePlayer = (((GuiNpcButton) guibutton).getValue() == 1);
        }
        if (guibutton.id == 6) {
            particle.glows = (((GuiNpcButton) guibutton).getValue() == 1);
        }
        if (guibutton.id == 7) {
            particle.noClip = (((GuiNpcButton) guibutton).getValue() == 1);
        }
        if (guibutton.id == 66) {
            close();
        }
    }

    @Override
    public void close() {
        GuiNpcTextField.unfocus();
        super.close();
    }
}
