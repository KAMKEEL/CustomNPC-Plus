package noppes.npcs.api.handler.data;

/**
 * Quest objective interface for kill-type quests.
 * Configures the target type for kill counting.
 */
public interface IQuestKill extends IQuestInterface {

    /**
     * Sets the target matching type.
     *
     * @param type 0: by entity name, 1: by faction.
     */
    void setTargetType(int type);

    /**
     * @return the target matching type (0: entity name, 1: faction).
     */
    int getTargetType();
}
