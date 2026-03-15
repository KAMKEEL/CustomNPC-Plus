package noppes.npcs.api;

/**
 * Represents a custom command that can be registered and executed.
 */
public interface ICommand {
    /**
     * Gets the name of this command.
     * @return the command name
     */
    String getCommandName();

    /**
     * Gets the usage string for this command.
     * @return the command usage description
     */
    String getCommandUsage();

    /**
     * Gets the permission level required to execute this command.
     * @return the permission level (0 = all, 1 = moderator, 2 = gamemaster, 3 = admin, 4 = owner)
     */
    int getPermissionLevel();

    /**
     * Sets the name of this command.
     * @param commandName the command name to set
     */
    void setCommandName(String commandName);

    /**
     * Sets the usage string for this command.
     * @param commandUsage the command usage description to set
     */
    void setCommandUsage(String commandUsage);

    /**
     * Sets the permission level required to execute this command.
     * @param permissionLevel the permission level (0 = all, 1 = moderator, 2 = gamemaster, 3 = admin, 4 = owner)
     */
    void setPermissionLevel(int permissionLevel);

    /**
     * Gets all aliases for this command.
     * @return an array of alias strings
     */
    String[] getAliases();

    /**
     * Adds one or more aliases for this command.
     * @param aliases the alias(es) to add
     */
    void addAliases(String... aliases);

    /**
     * Checks whether this command has a specific alias.
     * @param alias the alias to check for
     * @return true if the alias exists
     */
    boolean hasAlias(String alias);

    /**
     * Removes an alias from this command.
     * @param alias the alias to remove
     */
    void removeAlias(String alias);
}
