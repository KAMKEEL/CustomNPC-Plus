/**
 * Generated from Java interface net.minecraft.block.properties.IProperty
 * Source: CustomNPC+ JavaDoc (Minecraft 1.7.10)
 */

export interface IProperty<T> {
    /** Returns the property name */
    getName(): string;

    /** Returns the allowed values for this property */
    getAllowedValues(): Array<T>;

    /** Returns the runtime value class for this property (approximation) */
    getValueClass(): Function;

    /** Returns the name for a given value */
    getName(value: T): string;
}
