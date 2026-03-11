package noppes.npcs.controllers;

import java.util.LinkedHashMap;

public class APIRegistry {
    public static final APIRegistry Instance = new APIRegistry();

    private final LinkedHashMap<String, String> entries = new LinkedHashMap<>();

    public void register(String name, String url) {
        entries.put(name, url);
    }

    public void unregister(String name) {
        entries.remove(name);
    }

    public LinkedHashMap<String, String> getEntries() {
        return entries;
    }

    public int size() {
        return entries.size();
    }
}
