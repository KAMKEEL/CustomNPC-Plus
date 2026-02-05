package noppes.npcs.client.gui.util;

/**
 * Listener interface for DialogEditorPanel callbacks.
 * Implemented by parent GUIs (like GuiDialogTree) to handle
 * selection requests and actions from the editor panel.
 */
public interface IDialogEditorListener {
    /**
     * Called when user clicks Save button.
     */
    void onSaveRequested();

    /**
     * Called when user clicks Test button.
     */
    void onTestRequested();

    /**
     * Called when user wants to select a quest.
     * @param slot The quest slot being selected (usually 0 for dialog quest)
     */
    void onQuestSelectRequested(int slot);

    /**
     * Called when user wants to select a dialog (for availability).
     * @param slot The dialog requirement slot
     */
    void onDialogSelectRequested(int slot);

    /**
     * Called when user wants to select a faction (for availability).
     * @param slot The faction requirement slot
     */
    void onFactionSelectRequested(int slot);

    /**
     * Called when user wants to select a sound.
     */
    void onSoundSelectRequested();

    /**
     * Called when user wants to pick a color.
     * @param slot 0 for text color, 1 for title color
     * @param currentColor The current color value
     */
    void onColorSelectRequested(int slot, int currentColor);

    /**
     * Called when user wants to edit mail settings.
     */
    void onMailSetupRequested();

    /**
     * Called when user wants to select a target dialog for an option.
     * @param optionSlot The option slot (0-5)
     */
    void onOptionDialogSelectRequested(int optionSlot);
}
