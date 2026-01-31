package somehussar.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import somehussar.gui.annotationHandling.field.EditableField;

public class ImmediateTextField extends GuiNpcTextField{
    private final EditableField editableField;
//
//    private final GuiNpcTextField encapsulatedField;

    public ImmediateTextField(int id, EditableField field, int xPos, int yPos) {
        this(id, field, Minecraft.getMinecraft().fontRenderer, xPos, yPos);
    }
    public ImmediateTextField(int id, EditableField field, FontRenderer fontRenderer, int xPos, int yPos) {
        super(id, null, fontRenderer, xPos, yPos, 0, 0, null);

        this.editableField = field;
    }

    @Override
    public void drawTextBox(int mousX, int mousY) {

    }

    @Override
    public void mouseClicked(int xPos, int yPos, int k) {
        xPos -= this.xPosition;
        yPos -= this.yPosition;
    }
}
