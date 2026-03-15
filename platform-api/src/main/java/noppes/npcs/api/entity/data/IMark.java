package noppes.npcs.api.entity.data;

import noppes.npcs.api.handler.data.IAvailability;

/**
 * Represents a mark that can be applied to an entity.
 * A mark holds a type, a color, and availability conditions.
 */
public interface IMark {

    /**
     * Returns the availability conditions associated with this mark.
     *
     * @return the availability.
     */
    IAvailability getAvailability();

    /**
     * Returns the color value of this mark.
     *
     * @return the color as an integer.
     */
    int getColor();

    /**
     * Sets the color value of this mark.
     *
     * @param color the new color.
     */
    void setColor(int color);

    /**
     * Returns the type of this mark.
     *
     * @return the mark type as an integer.
     */
    int getType();

    /**
     * Sets the type of this mark.
     *
     * @param type the new type.
     */
    void setType(int type);

    /**
     * Calling this will send the changes you've made to the clients.
     */
    void update();
}
