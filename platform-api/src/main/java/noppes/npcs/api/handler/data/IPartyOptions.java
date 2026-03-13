package noppes.npcs.api.handler.data;

/**
 * Configures party-related options for quests, controlling membership requirements,
 * reward distribution, completion rules, and party size limits.
 */
public interface IPartyOptions {

    /** @return true if party participation is allowed. */
    boolean isAllowParty();

    /** @param allowParty true to allow party participation. */
    void setAllowParty(boolean allowParty);

    /** @return true if only party members can participate. */
    boolean isOnlyParty();

    /** @param onlyParty true to require party membership. */
    void setOnlyParty(boolean onlyParty);

    /**
     * @return 0:Leader, 1:All, 2:Valid
     */
    public int getPartyRequirements();

    /**
     * @param partyRequirements 0:Leader, 1:All, 2:Valid
     */
    public void setPartyRequirements(int partyRequirements);

    /**
     * @return 0:Leader, 1:All, 2:Enrolled, 3:Valid
     */
    public int getRewardControl();

    /**
     * @param rewardControl 0:Leader, 1:All, 2:Enrolled, 3:Valid
     */
    public void setRewardControl(int rewardControl);

    /**
     * @return 0:Leader, 1:All, 2:Enrolled, 3:Valid
     */
    public int getCompleteFor();

    /**
     * @param completeFor 0:Leader, 1:All, 2:Enrolled, 3:Valid
     */
    public void setCompleteFor(int completeFor);

    /**
     * @return 0:Leader, 1:All, 2:Enrolled, 3:Valid
     */
    int getExecuteCommandFor();

    /**
     * @param commandFor 0:Leader, 1:All, 2:Enrolled, 3:Valid
     */
    void setExecuteCommandFor(int commandFor);

    /**
     * @return 0:Shard, 1:All, 2:Leader
     */
    public int getObjectiveRequirement();

    /**
     * @param requirement 0:Shard, 1:All, 2:Leader
     */
    public void setObjectiveRequirement(int requirement);

    /** @return the minimum party size required. */
    int getMinPartySize();

    /** @param newSize the minimum party size. */
    void setMinPartySize(int newSize);

    /** @return the maximum party size allowed. */
    int getMaxPartySize();

    /** @param newSize the maximum party size. */
    void setMaxPartySize(int newSize);
}
