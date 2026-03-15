package noppes.npcs.api.handler.data;

public interface IDialogOption {
    /**
     * @return The slot index of this dialog option
     */
    int getSlot();

    /**
     * @return The display name/title of this dialog option
     */
    String getName();

    /**
     * @return The option type ordinal: 0 = QuitOption, 1 = DialogOption, 2 = Disabled, 3 = RoleOption, 4 = CommandBlock
     */
    int getType();
}
