package noppes.npcs.api.handler;

import noppes.npcs.api.jobs.IJobItemGiver;

public interface IPlayerItemGiverData {

    long getTime(IJobItemGiver jobItemGiver);

    void setTime(IJobItemGiver jobItemGiver, long day);

    boolean hasInteractedBefore(IJobItemGiver jobItemGiver);

    IJobItemGiver[] getItemGivers();
}
