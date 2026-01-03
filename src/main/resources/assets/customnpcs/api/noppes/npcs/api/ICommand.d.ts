/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api
 */

export interface ICommand {

    // Methods
    getCommandName(): string;
    getCommandUsage(): string;
    getPermissionLevel(): number;
    setCommandName(commandName: string): void;
    setCommandUsage(commandUsage: string): void;
    setPermissionLevel(permissionLevel: number): void;
    getAliases(): string[];
    addAliases(aliases: ): void;
    hasAlias(alias: string): boolean;
    removeAlias(alias: string): void;

}
