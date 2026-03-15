package noppes.npcs.api.handler.data;

/**
 * Quest objective interface for location-based quests.
 * Players must visit up to three named locations to complete the quest.
 */
public interface IQuestLocation extends IQuestInterface {

    /** @param loc1 the name of the first location objective. */
    void setLocation1(String loc1);

    /** @return the name of the first location objective. */
    String getLocation1();

    /** @param loc2 the name of the second location objective. */
    void setLocation2(String loc2);

    /** @return the name of the second location objective. */
    String getLocation2();

    /** @param loc3 the name of the third location objective. */
    void setLocation3(String loc3);

    /** @return the name of the third location objective. */
    String getLocation3();
}
