/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.jobs
 */

export interface IJobFollower extends IJob {

    // Methods
    getFollowingName(): string;
    setFollowingName(name: string): void;
    getFollowingNpc(): import('../entity/ICustomNpc').ICustomNpc;
    isFollowing(): boolean;

}
