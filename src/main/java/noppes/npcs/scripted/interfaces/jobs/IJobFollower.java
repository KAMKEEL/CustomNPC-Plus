package noppes.npcs.scripted.interfaces.jobs;

import noppes.npcs.scripted.interfaces.entity.ICustomNpc;

public interface IJobFollower extends IJob {

    String getFollowingName();

    void setFollowingName(String name);

    ICustomNpc getFollowingNpc();

    boolean isFollowing();
}
