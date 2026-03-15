/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: net.minecraft.block.state
 */

/**
 * This code is owned by Minecraft
  * @javaFqn net.minecraft.block.state.IBlockState
*/
export interface IBlockState {
    getPropertyNames(): import('../properties/IProperty').IProperty[];
    getProperties(): ImmutableMap<import('../properties/IProperty').IProperty, import('./Comparable').Comparable>;
    getBlock(): import('../Block').Block;
}
