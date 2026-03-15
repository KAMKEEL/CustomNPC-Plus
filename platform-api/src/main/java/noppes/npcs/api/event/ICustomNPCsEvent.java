package noppes.npcs.api.event;

import noppes.npcs.api.Cancelable;
import noppes.npcs.api.IPos;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.handler.data.INaturalSpawn;

/**
 * Base interface for all CustomNPC+ script events.
 */
public interface ICustomNPCsEvent {

    /** @return the script hook name that triggered this event. */
    String getHookName();

    /**
     * @hookName onCNPCNaturalSpawn
     */
    @Cancelable
    interface CNPCNaturalSpawnEvent extends ICustomNPCsEvent {
        INaturalSpawn getNaturalSpawn();

        void setAttemptPosition(IPos attemptPosition);

        IPos getAttemptPosition();

        boolean animalSpawnPassed();

        boolean monsterSpawnPassed();

        boolean liquidSpawnPassed();

        boolean airSpawnPassed();
    }

    interface ScriptedCommandEvent extends ICustomNPCsEvent {
        IWorld getSenderWorld();

        IPos getSenderPosition();

        String getSenderName();

        void setReplyMessage(String message);

        String getId();

        String[] getArgs();
    }
}
