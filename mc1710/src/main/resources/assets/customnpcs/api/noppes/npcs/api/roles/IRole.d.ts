/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.roles
 */

/**
 * Base interface for all NPC roles (Trader, Follower, Bank, Mailman, Transporter).
  * @javaFqn noppes.npcs.api.roles.IRole
*/
export interface IRole {
    /** @return the NPC this role is assigned to. */
    getNpc(): import('../../../../net/minecraft/entity/INpc').INpc;
    /**
     * @return the role type ordinal.
     *         0: None, 1: Trader, 2: Follower, 3: Bank, 4: Transporter, 5: Mailman.
     */
    getType(): import('./int').int;
    readonly npc: import('./EntityNPCInterface').EntityNPCInterface;
    readonly role: import('./RoleInterface').RoleInterface;
}
