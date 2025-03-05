package kamkeel.npcs.editorgui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import noppes.npcs.scripted.gui.ScriptGui;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * GuiCustomGuiEditor is the main editor screen.
 * The canvas covers the full scaled resolution.
 * The toolbar is positioned at the bottom.
 * Custom editing buttons (IDs 101–500) are arranged in a single row at the top.
 * Components are sorted by ID so that higher IDs render on top.
 * A SubGuiEditProperty overlay is used (instead of changing screens) for property editing.
 */
public class GuiCustomGuiEditor extends GuiScreen {
    // Full-screen canvas.
    public int canvasX = 0;
    public int canvasY = 0;
    public int canvasWidth;
    public int canvasHeight;

    private List<IEditorComponent> editorComponents = new ArrayList<>();
    private EditorToolBar toolBar;
    private IEditorComponent selectedComponent;
    private CustomGuiEditorController controller;

    // The sub–GUI overlay for property editing; null if none is open.
    private SubGuiEditProperty subGuiOverlay = null;

    // For dragging: offset from mouse to component's top–left.
    private int dragOffsetX, dragOffsetY;
    private boolean isDragging = false;

    public GuiCustomGuiEditor() {
        controller = new CustomGuiEditorController();
        mc = Minecraft.getMinecraft();
    }

    @Override
    public void initGui() {
        ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        canvasWidth = res.getScaledWidth();
        canvasHeight = res.getScaledHeight();
        canvasX = 0;
        canvasY = 0;
        toolBar = new EditorToolBar(canvasX, canvasY + canvasHeight - 40, canvasWidth, 40);
        toolBar.updateDimensions(canvasX, canvasY + canvasHeight - 40, canvasWidth, 40);
    }

    @Override
    public void drawDefaultBackground() {
        // Fill the full screen with a light background.
        drawRect(canvasX, canvasY, canvasX + canvasWidth, canvasY + canvasHeight, 0xFFCCCCCC);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        // Sort components by ID ascending so that higher IDs render on top.
        Collections.sort(editorComponents, new Comparator<IEditorComponent>() {
            @Override
            public int compare(IEditorComponent o1, IEditorComponent o2) {
                int id1 = ((AbstractEditorComponent) o1).getID();
                int id2 = ((AbstractEditorComponent) o2).getID();
                return Integer.compare(id1, id2);
            }
        });
        for (IEditorComponent comp : editorComponents) {
            comp.render(mouseX, mouseY, partialTicks);
        }
        toolBar.render(mouseX, mouseY, partialTicks);
        // If a sub–GUI overlay is open, draw it on top.
        if (subGuiOverlay != null) {
            subGuiOverlay.drawScreen(mouseX, mouseY, partialTicks);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        // If a sub–GUI overlay is open, forward the click to it.
        if (subGuiOverlay != null) {
            subGuiOverlay.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }
        // Check if any custom editing button (IDs 101–500) is clicked.
        for (Object obj : this.buttonList) {
            GuiButton btn = (GuiButton) obj;
            if (btn.id >= 101 && btn.id < 500 && btn.mousePressed(mc, mouseX, mouseY)) {
                this.actionPerformed(btn);
                updateCustomButtons();
                return;
            }
        }
        // Let toolbar buttons handle clicks.
        for (GuiButton btn : toolBar.getButtons()) {
            if (btn.mousePressed(mc, mouseX, mouseY)) {
                toolBar.actionPerformed(btn, this);
                updateCustomButtons();
                return;
            }
        }
        boolean anySelected = false;
        for (IEditorComponent comp : editorComponents) {
            if (comp.mouseClicked(mouseX, mouseY, mouseButton)) {
                selectedComponent = comp;
                anySelected = true;
            } else {
                comp.setSelected(false);
            }
        }
        if (!anySelected) {
            selectedComponent = null;
        } else if (selectedComponent != null) {
            dragOffsetX = mouseX - ((AbstractEditorComponent) selectedComponent).posX;
            dragOffsetY = mouseY - ((AbstractEditorComponent) selectedComponent).posY;
            isDragging = true;
        }
        updateCustomButtons();
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        if (subGuiOverlay != null) {
            subGuiOverlay.mouseMovedOrUp(mouseX, mouseY, state);
            return;
        }
        if (selectedComponent != null) {
            selectedComponent.mouseReleased(mouseX, mouseY, state);
        }
        isDragging = false;
        super.mouseMovedOrUp(mouseX, mouseY, state);
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
        if (subGuiOverlay != null) {
            // Optionally forward dragging events to the overlay.
            subGuiOverlay.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
            return;
        }
        if (isDragging && selectedComponent != null) {
            int newX = mouseX - dragOffsetX;
            int newY = mouseY - dragOffsetY;
            int compW = ((AbstractEditorComponent) selectedComponent).width;
            int compH = ((AbstractEditorComponent) selectedComponent).height;
            if (newX < canvasX) newX = canvasX;
            if (newY < canvasY) newY = canvasY;
            if (newX + compW > canvasWidth) newX = canvasWidth - compW;
            if (newY + compH > canvasHeight) newY = canvasHeight - compH;
            ((AbstractEditorComponent) selectedComponent).posX = newX;
            ((AbstractEditorComponent) selectedComponent).posY = newY;
            selectedComponent.mouseDragged(mouseX, mouseY, mouseButton);
        }
        super.mouseClickMove(mouseX, mouseY, mouseButton, timeSinceLastClick);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (subGuiOverlay != null) {
            subGuiOverlay.keyTyped(typedChar, keyCode);
            return;
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            saveAndClose();
            return;
        }
        if (keyCode == Keyboard.KEY_DELETE || keyCode == Keyboard.KEY_BACK) {
            if (selectedComponent != null) {
                Iterator<IEditorComponent> iter = editorComponents.iterator();
                while (iter.hasNext()) {
                    IEditorComponent comp = iter.next();
                    if (comp == selectedComponent) {
                        iter.remove();
                        selectedComponent = null;
                        break;
                    }
                }
            }
        }
        updateCustomButtons();
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id >= 101 && button.id < 500) {
            if (selectedComponent != null)
                selectedComponent.onEditorButtonPressed(button);
            updateCustomButtons();
        } else {
            toolBar.actionPerformed(button, this);
        }
    }

    /**
     * Updates custom editing buttons (IDs 101–500) so they appear in a single row at the top.
     */
    private void updateCustomButtons() {
        for (int i = buttonList.size() - 1; i >= 0; i--) {
            GuiButton btn = (GuiButton) buttonList.get(i);
            if (btn.id >= 101 && btn.id < 500) {
                buttonList.remove(i);
            }
        }
        if (selectedComponent != null) {
            List<GuiButton> customButtons = new ArrayList<>();
            selectedComponent.addEditorButtons(customButtons);
            int startX = 5;
            int startY = 5;
            int buttonWidth = 30;
            int buttonHeight = 20;
            int spacing = 5;
            int currentX = startX;
            for (GuiButton btn : customButtons) {
                btn.xPosition = currentX;
                btn.yPosition = startY;
                btn.width = buttonWidth;
                btn.height = buttonHeight;
                currentX += buttonWidth + spacing;
            }
            buttonList.addAll(customButtons);
        }
    }

    /**
     * Adds a new editor component.
     */
    public void addEditorComponent(IEditorComponent comp) {
        editorComponents.add(comp);
    }

    /**
     * Builds a ScriptGui from the editor components, saves it, and closes the editor.
     */
    public void saveAndClose() {
        ScriptGui newGui = new ScriptGui();
        for (IEditorComponent comp : editorComponents) {
            newGui.updateComponent((noppes.npcs.api.gui.ICustomGuiComponent) comp.toScriptComponent());
        }
        CustomGuiEditorController controller = new CustomGuiEditorController();
        controller.saveGui(new File("customgui.json"), newGui);
        Minecraft.getMinecraft().displayGuiScreen(null);
    }

    /**
     * Sets the current sub–GUI overlay (for property editing) and forwards events to it.
     */
    public void setSubGuiOverlay(SubGuiEditProperty overlay) {
        this.subGuiOverlay = overlay;
    }

    /**
     * Clears the sub–GUI overlay.
     */
    public void clearSubGuiOverlay() {
        this.subGuiOverlay = null;
    }
}
