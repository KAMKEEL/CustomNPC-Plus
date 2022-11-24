package noppes.npcs.api.event;

import noppes.npcs.api.IPos;
import noppes.npcs.api.handler.data.INaturalSpawn;

public interface CustomNPCsEvent {
    interface CNPCNaturalSpawnEvent {
        INaturalSpawn getNaturalSpawn();

        IPos getAttemptPosition();

        boolean animalSpawnPassed();

        boolean monsterSpawnPassed();

        boolean liquidSpawnPassed();

        boolean airSpawnPassed();
    }
}
