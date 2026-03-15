/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler.data
 */

/**
 * Represents an instance of a custom attribute.
 * <p>
 * A custom attribute instance encapsulates an {@link IAttributeDefinition}
 * along with its current calculated value (which may be affected by modifiers).
 * </p>
  * @javaFqn noppes.npcs.api.handler.data.ICustomAttribute
*/
export interface ICustomAttribute {
    /**
     * Returns the attribute definition associated with this custom attribute.
     *
     * @return the {@link IAttributeDefinition} instance defining this attribute
     */
    getAttribute(): import('./IAttributeDefinition').IAttributeDefinition;
    /**
     * Returns the current value of this attribute.
     *
     * @return the float value representing the attribute's current state
     */
    getValue(): import('./float').float;
}
