package kamkeel.npcs.editorgui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import java.util.List;

/**
 * IEditorComponent defines the methods that all editor wrappers must implement.
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
     * Add custom editing buttons (IDs 101–500) to the provided list.
     */
    default void addEditorButtons(List<GuiButton> buttonList) { }

    /**
     * Called when one of the custom editing buttons is pressed.
     */
    default void onEditorButtonPressed(GuiButton button) { }

    default NBTTagCompound toEditorNBT() {
        return new NBTTagCompound();
    }
}
