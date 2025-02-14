package noppes.npcs.controllers;

import noppes.npcs.controllers.data.LinkedItem;

import java.util.HashMap;

//TODO: load this data from somewhere, use in GUIs, create script wrapper
public class LinkedItemController {
    private static LinkedItemController Instance;
    private final HashMap<String, LinkedItem> linkedItems = new HashMap<>();

    private LinkedItemController() {
    }

    public static LinkedItemController Instance() {
        if (Instance == null) {
            Instance = new LinkedItemController();
        }
        return Instance;
    }

    public void add(LinkedItem linkedItem) {
        if (linkedItem != null) {
            String name = linkedItem.getName();
            if (name != null && !name.isEmpty()) {
                this.linkedItems.put(name.toLowerCase(), linkedItem);
            }
        }
    }

    public LinkedItem get(String name) {
        return name != null && !name.isEmpty() ? this.linkedItems.get(name.toLowerCase()) : null;
    }
}
