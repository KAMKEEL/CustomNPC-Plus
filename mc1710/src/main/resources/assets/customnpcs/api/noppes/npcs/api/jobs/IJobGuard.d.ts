/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.jobs
 */

/**
 * @javaFqn noppes.npcs.api.jobs.IJobGuard
 */
export interface IJobGuard extends import('./IJob').IJob {
    attackCreepers(): import('./boolean').boolean;
    attackCreepers(value: import('./boolean').boolean): import('./void').void;
    attacksAnimals(): import('./boolean').boolean;
    attacksAnimals(value: import('./boolean').boolean): import('./void').void;
    attackHostileMobs(): import('./boolean').boolean;
    attackHostileMobs(value: import('./boolean').boolean): import('./void').void;
}
