package noppes.npcs.api.roles;

import noppes.npcs.api.entity.IPlayer;

public interface IRoleFollower extends IRole {

    /**
     * @since 1.7.10c
     * @param player Player who is set as the owner. If null given everything resets
     */
    void setOwner(IPlayer player);

    /**
     * @since 1.7.10c
     * @return Returns the followers owner. Returns null if he has no owner or the owner is offline
     */
    IPlayer getOwner();

    /**
     * @since 1.7.10c
     * @return Returns whether or not the follower has an owner
     */
    boolean hasOwner();

    boolean isFollowing();

    void setIsFollowing(boolean following);

    /**
     * @since 1.7.10c
     * @return Returns days left
     */
    int getDaysLeft();

    /**
     * @since 1.7.10c
     * @param days The days you want to add to the days remaining
     */
    void addDaysLeft(int days);

    /**
     * @since 1.7.10c
     * @return Returns whether or not the follower is set to infinite days
     */
    boolean getInfiniteDays();

    /**
     * @since 1.7.10c
     * @param infinite Sets whether the days hired are infinite
     */
    void setInfiniteDays(boolean infinite);

    /**
     * @since 1.7.10c
     * @return Return whether the gui is disabled
     */
    boolean getGuiDisabled();

    /**
     * @since 1.7.10c
     * @param disabled Set the gui to be disabled or not
     */
    void setGuiDisabled(boolean disabled);
}
