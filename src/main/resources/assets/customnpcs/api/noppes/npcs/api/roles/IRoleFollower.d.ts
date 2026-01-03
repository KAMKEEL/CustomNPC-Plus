/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.roles
 */

export interface IRoleFollower extends IRole {

    // Methods
    setOwner(player: import('../entity/IPlayer').IPlayer): void;
    getOwner(): import('../entity/IPlayer').IPlayer;
    hasOwner(): boolean;
    isFollowing(): boolean;
    setIsFollowing(following: boolean): void;
    getDaysLeft(): number;
    addDaysLeft(days: number): void;
    getInfiniteDays(): boolean;
    setInfiniteDays(infinite: boolean): void;
    getGuiDisabled(): boolean;
    setGuiDisabled(disabled: boolean): void;
    setRate(index: number, amount: number): void;
    getRate(index: number): number;
    setDialogHire(dialogHire: string): void;
    getDialogHire(): string;
    setDialogFarewell(dialogFarewell: string): void;
    getDialogFarewell(): string;

}
