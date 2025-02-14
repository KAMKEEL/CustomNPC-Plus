package noppes.npcs.controllers;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import noppes.npcs.CustomNpcs;
import noppes.npcs.controllers.data.EffectScript;
import noppes.npcs.controllers.data.INpcScriptHandler;
import noppes.npcs.controllers.data.LinkedItem;

import java.io.File;
import java.util.HashMap;

//TODO: load this data from somewhere, use in GUIs
public class LinkedItemController {
    private static LinkedItemController Instance;
    private int lastUsedID = 0;

    private final HashMap<Integer, LinkedItem> linkedItems = new HashMap<>();
    private final HashMap<Integer, EffectScript> linkedItemsScripts = new HashMap<>();

    private LinkedItemController() {
    }

    public static LinkedItemController Instance() {
        if (Instance == null) {
            Instance = new LinkedItemController();
        }
        return Instance;
    }

    public int getUnusedId() {
        for (int id : this.linkedItems.keySet()) {
            if (id > this.lastUsedID)
                this.lastUsedID = id;
        }
        this.lastUsedID++;
        return this.lastUsedID;
    }

    private File getDir() {
        return new File(CustomNpcs.getWorldSaveDirectory(), "linkeditems");
    }

    public LinkedItem createItem(String name) {
        return new LinkedItem(name);
    }

    public void add(LinkedItem linkedItem) {
        if (linkedItem != null) {
            String name = linkedItem.getName();
            if (name != null && !name.isEmpty()) {
                int linkedItemId = linkedItem.getId();
                int id = this.linkedItems.containsKey(linkedItemId) ? linkedItemId : this.getUnusedId();
                this.linkedItems.put(id, linkedItem);
                linkedItem.setId(id);
                this.addScript(id);
            }
        }
    }

    @SideOnly(Side.SERVER)
    private void addScript(int id) {
        this.linkedItemsScripts.put(id, new EffectScript());
    }

    public void remove(int id) {
        this.linkedItems.remove(id);
        this.removeScript(id);
    }

    @SideOnly(Side.SERVER)
    private void removeScript(int id) {
        this.linkedItemsScripts.remove(id);
    }

    public LinkedItem get(int id) {
        return this.linkedItems.get(id);
    }

    public boolean contains(int id) {
        return this.linkedItems.containsKey(id);
    }

    public boolean contains(LinkedItem linkedItem) {
        return this.linkedItems.containsValue(linkedItem);
    }

    public INpcScriptHandler getScriptHandler(int id) {
        return this.linkedItemsScripts.get(id);
    }
}
