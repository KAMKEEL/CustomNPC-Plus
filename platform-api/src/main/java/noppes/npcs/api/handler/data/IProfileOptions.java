package noppes.npcs.api.handler.data;

public interface IProfileOptions {

    boolean hasProfileOptions();

    void setProfileOptions(boolean enable);

    /**
     * @param profileType 0:Individual, 1:Shared
     */
    void setCooldownControl(int profileType);

    /**
     * @return 0:Individual, 1:Shared
     */
    int getCooldownControl();

    /**
     * @param profileType 0:Individual, 1:Shared
     */
    void setCompleteControl(int profileType);

    /**
     * @return 0:Individual, 1:Shared
     */
    int getCompleteControl();
}
