/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * @javaFqn noppes.npcs.api.handler.data.IDialogOption
 */
export interface IDialogOption {
    /**
     * @return The slot index of this dialog option
     */
    getSlot(): import('./int').int;
    /**
     * @return The display name/title of this dialog option
     */
    getName(): String;
    /**
     * @return The option type ordinal: 0 = QuitOption, 1 = DialogOption, 2 = Disabled, 3 = RoleOption, 4 = CommandBlock
     */
    getType(): import('./int').int;
}
