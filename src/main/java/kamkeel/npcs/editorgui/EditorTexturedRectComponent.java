package kamkeel.npcs.editorgui;

import noppes.npcs.scripted.gui.ScriptGuiTexturedRect;
import noppes.npcs.client.gui.custom.components.CustomGuiTexturedRect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import java.util.List;

/**
 * EditorTexturedRectComponent wraps a ScriptGuiTexturedRect.
 * It renders the actual textured rectangle and provides editing buttons for width, height, scale, alpha,
 * rotation, texture string, texture X/Y offsets, and ID.
 */
public class EditorTexturedRectComponent extends AbstractEditorComponent {
    private ScriptGuiTexturedRect texturedRect;

    public EditorTexturedRectComponent(ScriptGuiTexturedRect rect) {
        super(rect.getPosX(), rect.getPosY(), rect.getWidth(), rect.getHeight());
        this.texturedRect = rect;
        this.id = rect.getID();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        CustomGuiTexturedRect renderRect = CustomGuiTexturedRect.fromComponent(texturedRect);
        renderRect.x = this.posX;
        renderRect.y = this.posY;
        renderRect.scale = texturedRect.getScale();
        renderRect.alpha = texturedRect.getAlpha();
        renderRect.rotation = texturedRect.getRotation();
        renderRect.onRender(Minecraft.getMinecraft(), mouseX, mouseY, 0, partialTicks);
        renderSelectionOutline();
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if(mouseX >= posX && mouseX < posX + width &&
           mouseY >= posY && mouseY < posY + height) {
            selected = true;
            return true;
        }
        selected = false;
        return false;
    }

    @Override
    public void mouseDragged(int mouseX, int mouseY, int mouseButton) {
        if(selected) {
            texturedRect.setPos(posX, posY);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) { }

    @Override
    public Object toScriptComponent() {
        return texturedRect;
    }

    @Override
    public void addEditorButtons(List<GuiButton> buttonList) {
        buttonList.add(new GuiButton(401, 0, 0, 30, 20, "W+"));
        buttonList.add(new GuiButton(402, 0, 0, 30, 20, "W-"));
        buttonList.add(new GuiButton(403, 0, 0, 30, 20, "H+"));
        buttonList.add(new GuiButton(404, 0, 0, 30, 20, "H-"));
        buttonList.add(new GuiButton(405, 0, 0, 30, 20, "Sc"));
        buttonList.add(new GuiButton(406, 0, 0, 30, 20, "Al"));
        buttonList.add(new GuiButton(407, 0, 0, 30, 20, "Rt"));
        buttonList.add(new GuiButton(408, 0, 0, 30, 20, "Tex"));
        buttonList.add(new GuiButton(409, 0, 0, 30, 20, "TxX"));
        buttonList.add(new GuiButton(410, 0, 0, 30, 20, "TxY"));
        buttonList.add(new GuiButton(411, 0, 0, 30, 20, "ID"));
    }

    @Override
    public void onEditorButtonPressed(GuiButton button) {
        switch(button.id) {
            case 401:
                width += 5;
                texturedRect.setSize(width, height);
                break;
            case 402:
                width = Math.max(10, width - 5);
                texturedRect.setSize(width, height);
                break;
            case 403:
                height += 5;
                texturedRect.setSize(width, height);
                break;
            case 404:
                height = Math.max(10, height - 5);
                texturedRect.setSize(width, height);
                break;
            case 405:
                Minecraft.getMinecraft().displayGuiScreen(new SubGuiEditProperty("Scale", Float.toString(texturedRect.getScale()), new IPropertyEditorCallback() {
                    @Override
                    public void propertyUpdated(String newValue) {
                        try {
                            float newScale = Float.parseFloat(newValue);
                            texturedRect.setScale(newScale);
                        } catch (NumberFormatException e) { }
                    }
                }, new ISubGuiCallback() {
                    @Override
                    public void onSubGuiClosed() { }
                }));
                break;
            case 406:
                Minecraft.getMinecraft().displayGuiScreen(new SubGuiEditProperty("Alpha", Float.toString(texturedRect.getAlpha()), new IPropertyEditorCallback() {
                    @Override
                    public void propertyUpdated(String newValue) {
                        try {
                            float newAlpha = Float.parseFloat(newValue);
                            texturedRect.setAlpha(newAlpha);
                        } catch (NumberFormatException e) { }
                    }
                }, new ISubGuiCallback() {
                    @Override
                    public void onSubGuiClosed() { }
                }));
                break;
            case 407:
                Minecraft.getMinecraft().displayGuiScreen(new SubGuiEditProperty("Rotation", Float.toString(texturedRect.getRotation()), new IPropertyEditorCallback() {
                    @Override
                    public void propertyUpdated(String newValue) {
                        try {
                            float newRot = Float.parseFloat(newValue);
                            texturedRect.setRotation(newRot);
                        } catch (NumberFormatException e) { }
                    }
                }, new ISubGuiCallback() {
                    @Override
                    public void onSubGuiClosed() { }
                }));
                break;
            case 408:
                Minecraft.getMinecraft().displayGuiScreen(new SubGuiEditProperty("Texture", texturedRect.getTexture(), new IPropertyEditorCallback() {
                    @Override
                    public void propertyUpdated(String newValue) {
                        texturedRect.setTexture(newValue);
                    }
                }, new ISubGuiCallback() {
                    @Override
                    public void onSubGuiClosed() { }
                }));
                break;
            case 409:
                Minecraft.getMinecraft().displayGuiScreen(new SubGuiEditProperty("TxX", Integer.toString(texturedRect.getTextureX()), new IPropertyEditorCallback() {
                    @Override
                    public void propertyUpdated(String newValue) {
                        try {
                            int newX = Integer.parseInt(newValue);
                            texturedRect.setTextureOffset(newX, texturedRect.getTextureY());
                        } catch (NumberFormatException e) { }
                    }
                }, new ISubGuiCallback() {
                    @Override
                    public void onSubGuiClosed() { }
                }));
                break;
            case 410:
                Minecraft.getMinecraft().displayGuiScreen(new SubGuiEditProperty("TxY", Integer.toString(texturedRect.getTextureY()), new IPropertyEditorCallback() {
                    @Override
                    public void propertyUpdated(String newValue) {
                        try {
                            int newY = Integer.parseInt(newValue);
                            texturedRect.setTextureOffset(texturedRect.getTextureX(), newY);
                        } catch (NumberFormatException e) { }
                    }
                }, new ISubGuiCallback() {
                    @Override
                    public void onSubGuiClosed() { }
                }));
                break;
            case 411:
                Minecraft.getMinecraft().displayGuiScreen(new SubGuiEditProperty("ID", Integer.toString(texturedRect.getID()), new IPropertyEditorCallback() {
                    @Override
                    public void propertyUpdated(String newValue) {
                        try {
                            int newID = Integer.parseInt(newValue);
                            texturedRect.setID(newID);
                        } catch (NumberFormatException e) { }
                    }
                }, new ISubGuiCallback() {
                    @Override
                    public void onSubGuiClosed() { }
                }));
                break;
        }
    }
}
