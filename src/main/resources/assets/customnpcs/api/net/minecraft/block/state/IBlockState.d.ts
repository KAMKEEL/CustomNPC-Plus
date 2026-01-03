/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: net.minecraft.block.state
 */

export interface IBlockState {

    // Methods
    getPropertyNames(): Array<IProperty;
    getValue(property: IProperty<T): <T extends Comparable<T T;
    withProperty(property: IProperty<T, value: V): <T extends Comparable<T, V extends T IBlockState;
    cycleProperty(property: IProperty<T): <T extends Comparable<T IBlockState;
    getProperties(): ImmutableMap<IProperty, Comparable;
    getBlock(): Block;

}
