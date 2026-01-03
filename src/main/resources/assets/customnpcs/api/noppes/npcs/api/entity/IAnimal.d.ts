/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

export interface IAnimal extends IEntityLiving<T> {

    // Methods
    isBreedingItem(itemStack: import('../item/IItemStack').IItemStack): boolean;
    interact(player: import('./IPlayer').IPlayer): boolean;
    setFollowPlayer(player: import('./IPlayer').IPlayer): void;
    followingPlayer(): import('./IPlayer').IPlayer;
    isInLove(): boolean;
    resetInLove(): void;
    canMateWith(animal: import('./IAnimal').IAnimal): boolean;

}
