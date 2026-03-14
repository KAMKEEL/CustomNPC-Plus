package noppes.npcs.api.handler.data;

import noppes.npcs.api.INbt;

import java.util.Map;

public interface ISlot {
    int getId();

    String getName();

    void setName(String name);

    long getLastLoaded();

    void setLastLoaded(long time);

    boolean isTemporary();

    void setTemporary(boolean temporary);

    Map<String, INbt> getComponents();

    void setComponentData(String key, INbt data);

    INbt getComponentData(String key);

    INbt toNBT();
}
