package noppes.npcs.api.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.IPos;
import noppes.npcs.api.handler.data.INaturalSpawn;

public interface ICustomNPCsEvent {

    @Cancelable
    interface CNPCNaturalSpawnEvent {
        INaturalSpawn getNaturalSpawn();

        void setAttemptPosition(IPos attemptPosition);

        IPos getAttemptPosition();

        boolean animalSpawnPassed();

        boolean monsterSpawnPassed();

        boolean liquidSpawnPassed();

        boolean airSpawnPassed();
    }
}
