package noppes.npcs.api.jobs;

import noppes.npcs.api.entity.ICustomNpc;

public interface IJobFollower extends IJob {

    String getFollowingName();

    void setFollowingName(String name);

    ICustomNpc getFollowingNpc();

    boolean isFollowing();
}
