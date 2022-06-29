package noppes.npcs.scripted.interfaces.handler;

import noppes.npcs.scripted.interfaces.handler.data.INaturalSpawn;

public interface INaturalSpawnsHandler {

    void saveAllData();

    INaturalSpawn[] getSpawns();

    INaturalSpawn[] getSpawns(String biome);
}
