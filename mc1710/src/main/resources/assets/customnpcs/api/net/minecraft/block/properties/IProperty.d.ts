/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: net.minecraft.block.properties
 */

/**
 * This code is owned by Minecraft
  * @javaFqn net.minecraft.block.properties.IProperty
*/
export interface IProperty<T extends Comparable /* net.minecraft.block.properties.Comparable */> {
    getName(): String;
    getAllowedValues(): T[];
    getValueClass(): Class<T>;
    /**
     * Get the name for the given value.
     */
    getName(value: T): String;
}
