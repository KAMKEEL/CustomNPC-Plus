/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * @javaFqn noppes.npcs.api.handler.data.IDialogCategory
 */
export interface IDialogCategory {
    /**
     * @return A list of all dialogs in this category
     */
    dialogs(): import('./IDialog').IDialog[];
    /**
     * @return The name of this dialog category
     */
    getName(): String;
    /**
     * Creates a new empty dialog in this category.
     * @return The newly created {@link IDialog}
     */
    create(): import('./IDialog').IDialog;
    /**
     * @return The unique ID of this dialog category
     */
    getId(): import('./int').int;
    id: import('./int').int;
    title: String;
    dialogs: Java.java.util.HashMap<Integer, import('./Dialog').Dialog>;
}
