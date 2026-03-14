package noppes.npcs.api.handler.data;

import noppes.npcs.platform.nbt.INBTCompound;

import java.util.Map;

public interface ISlot {
    int getId();

    String getName();

    void setName(String name);

    long getLastLoaded();

    void setLastLoaded(long time);

    boolean isTemporary();

    void setTemporary(boolean temporary);

    Map<String, INBTCompound> getComponents();

    void setComponentData(String key, INBTCompound data);

    INBTCompound getComponentData(String key);

    INBTCompound toNBT();
}
