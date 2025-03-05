package kamkeel.npcs.editorgui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import java.util.List;

/**
 * IEditorComponent defines the methods that all editor wrappers must implement.
 * It handles rendering (with optional editing buttons), mouse events, updating,
 * conversion to the underlying ScriptGui component, and adding custom editor buttons.
 */
public interface IEditorComponent {
    void render(int mouseX, int mouseY, float partialTicks);
    boolean mouseClicked(int mouseX, int mouseY, int mouseButton);
    void mouseDragged(int mouseX, int mouseY, int mouseButton);
    void mouseReleased(int mouseX, int mouseY, int state);
    void update();
    Object toScriptComponent();
    void setSelected(boolean selected);
    boolean isSelected();

    /**
     * Adds custom editing buttons to the provided list.
     * (IDs 101–500 are reserved for custom editing.)
     */
    default void addEditorButtons(List<GuiButton> buttonList) { }

    /**
     * Called when a custom editing button is pressed.
     */
    default void onEditorButtonPressed(GuiButton button) { }

    default NBTTagCompound toEditorNBT() {
        return new NBTTagCompound();
    }
}
