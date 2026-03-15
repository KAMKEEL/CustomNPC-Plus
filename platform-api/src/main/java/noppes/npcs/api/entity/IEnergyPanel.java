package noppes.npcs.api.entity;


/**
 * Represents an energy panel barrier - a flat rectangular shield (Wall or Shield mode).
 */
public interface IEnergyPanel extends IEnergyBarrier {

    float getPanelWidth();

    void setPanelWidth(float width);

    float getPanelHeight();

    void setPanelHeight(float height);

    float getPanelYaw();

    void setPanelYaw(float yaw);

    /**
     * Panel mode: 0=PLACED, 1=HELD, 2=LAUNCHED
     * @return the panel mode ordinal
     */
    int getPanelMode();

    void setPanelMode(int mode);

    boolean isLaunched();

    /** Spawn this panel entity into the world. */
    void spawn();
}
