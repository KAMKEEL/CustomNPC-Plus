/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api
 */

/**
 * Represents a custom command that can be registered and executed.
  * @javaFqn noppes.npcs.api.ICommand
*/
export interface ICommand {
    /**
     * Gets the name of this command.
     * @return the command name
     */
    getCommandName(): String;
    /**
     * Gets the usage string for this command.
     * @return the command usage description
     */
    getCommandUsage(): String;
    /**
     * Gets the permission level required to execute this command.
     * @return the permission level (0 = all, 1 = moderator, 2 = gamemaster, 3 = admin, 4 = owner)
     */
    getPermissionLevel(): import('./int').int;
    /**
     * Sets the name of this command.
     * @param commandName the command name to set
     */
    setCommandName(commandName: String): import('./void').void;
    /**
     * Sets the usage string for this command.
     * @param commandUsage the command usage description to set
     */
    setCommandUsage(commandUsage: String): import('./void').void;
    /**
     * Sets the permission level required to execute this command.
     * @param permissionLevel the permission level (0 = all, 1 = moderator, 2 = gamemaster, 3 = admin, 4 = owner)
     */
    setPermissionLevel(permissionLevel: import('./int').int): import('./void').void;
    /**
     * Gets all aliases for this command.
     * @return an array of alias strings
     */
    getAliases(): String[];
    /**
     * Adds one or more aliases for this command.
     * @param aliases the alias(es) to add
     */
    addAliases(...aliases: String[]): import('./void').void;
    /**
     * Checks whether this command has a specific alias.
     * @param alias the alias to check for
     * @return true if the alias exists
     */
    hasAlias(alias: String): import('./boolean').boolean;
    /**
     * Removes an alias from this command.
     * @param alias the alias to remove
     */
    removeAlias(alias: String): import('./void').void;
}
