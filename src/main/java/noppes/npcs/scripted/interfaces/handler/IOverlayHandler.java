package noppes.npcs.scripted.interfaces.handler;

import noppes.npcs.scripted.interfaces.ISkinOverlay;

public interface IOverlayHandler {

    void add(int id, ISkinOverlay overlay);

    ISkinOverlay get(int id);

    boolean has(int id);

    boolean remove(int id);

    int size();

    void clear();
}
