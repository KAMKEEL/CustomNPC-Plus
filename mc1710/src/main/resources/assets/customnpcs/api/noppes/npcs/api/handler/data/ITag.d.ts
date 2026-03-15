/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * Represents a tag that can be applied to NPCs for categorization and filtering.
  * @javaFqn noppes.npcs.api.handler.data.ITag
*/
export interface ITag {
    /** @return the unique UUID string for this tag. */
    getUuid(): String;
    /** @return the tag display name. */
    getName(): String;
    /** @param name the tag display name. */
    setName(name: String): import('./void').void;
    /** @param c the tag color as a packed RGB integer. */
    setColor(c: import('./int').int): import('./void').void;
    /** @return the unique tag ID. */
    getId(): import('./int').int;
    /** @return the tag color as a packed RGB integer. */
    getColor(): import('./int').int;
    /** @return true if this tag is hidden from display. */
    getIsHidden(): import('./boolean').boolean;
    /** @param hidden true to hide this tag from display. */
    setIsHidden(hidden: import('./boolean').boolean): import('./void').void;
    /** Saves this tag to disk. */
    save(): import('./void').void;
}
