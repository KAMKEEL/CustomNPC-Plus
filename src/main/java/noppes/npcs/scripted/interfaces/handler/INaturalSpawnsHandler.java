package noppes.npcs.scripted.interfaces.handler;

import noppes.npcs.scripted.interfaces.handler.data.INaturalSpawn;

public interface INaturalSpawnsHandler {

    void save();

    INaturalSpawn[] getSpawns();

    INaturalSpawn[] getSpawns(String biome);

    INaturalSpawn createSpawn();
}
