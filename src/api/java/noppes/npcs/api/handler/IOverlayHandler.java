package noppes.npcs.api.handler;

import noppes.npcs.api.ISkinOverlay;

public interface IOverlayHandler {

    void add(int id, ISkinOverlay overlay);

    ISkinOverlay get(int id);

    boolean has(int id);

    boolean remove(int id);

    int size();

    void clear();
}
