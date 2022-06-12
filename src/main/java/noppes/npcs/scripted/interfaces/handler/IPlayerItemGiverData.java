package noppes.npcs.scripted.interfaces.handler;

import noppes.npcs.scripted.interfaces.jobs.IJobItemGiver;

public interface IPlayerItemGiverData {

    long getTime(IJobItemGiver jobItemGiver);

    void setTime(IJobItemGiver jobItemGiver, long day);

    boolean hasInteractedBefore(IJobItemGiver jobItemGiver);

    IJobItemGiver[] getItemGivers();
}
