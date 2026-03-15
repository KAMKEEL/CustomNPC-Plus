/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.jobs
 */

/**
 * @javaFqn noppes.npcs.api.jobs.IJobFollower
 */
export interface IJobFollower extends import('./IJob').IJob {
    getFollowingName(): String;
    setFollowingName(name: String): import('./void').void;
    getFollowingNpc(): import('../entity/ICustomNpc').ICustomNpc;
    isFollowing(): import('./boolean').boolean;
}
