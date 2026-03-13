package noppes.npcs.api.handler.data;

import noppes.npcs.api.IPos;

/**
 * Represents a transport destination that players can teleport to.
 */
public interface ITransportLocation {

    /** @return the unique transport location ID. */
    int getId();

    /** @param name the location display name. */
    void setName(String name);

    /** @return the location display name. */
    String getName();

    /** @param dimension the dimension ID where this transport is located. */
    void setDimension(int dimension);

    /** @return the dimension ID. */
    int getDimension();

    /**
     * Sets the transport type.
     *
     * @param type the type ordinal.
     */
    void setType(int type);

    /** @return the transport type ordinal. */
    int getType();

    /**
     * Sets the transport destination coordinates.
     *
     * @param x the x coordinate.
     * @param y the y coordinate.
     * @param z the z coordinate.
     */
    void setPosition(int x, int y, int z);

    /**
     * Sets the transport destination position.
     *
     * @param pos the position.
     */
    void setPosition(IPos pos);

    /** @return the x coordinate. */
    double getX();

    /** @return the y coordinate. */
    double getY();

    /** @return the z coordinate. */
    double getZ();

    /** Saves this transport location to disk. */
    void save();
}
