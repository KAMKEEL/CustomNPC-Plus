package noppes.npcs.client.gui.util;

import noppes.npcs.controllers.data.Tag;
import noppes.npcs.controllers.data.TagMap;

import java.util.HashMap;
import java.util.UUID;

public interface IClonerGui {
    int getShowingClones();

    HashMap<UUID, Tag> getTags();

    TagMap getTagMap();
}
