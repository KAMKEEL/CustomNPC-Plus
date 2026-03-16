package kamkeel.npcs.controllers.data.profile;


import java.util.List;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.IWorld;

public interface IProfileData {

    /**
     * Do not use the tags: Name, lastLoaded, ID.
     *
     * @return The save tag utilized in the Profile.
     */
    String getTagName();

    /**
     * @return INbt of the current data for a given IPlayer.
     */
    INbt getCurrentNBT(IPlayer player);

    /**
     * Called immediately after setNBT() to replace existing NBT.
     */
    void save(IPlayer player);

    /**
     * Replaces the current data operation when changing profiles.
     */
    void setNBT(IPlayer player, INbt replace);

    /**
     * Returns the priority for this ProfileData's verification check.
     * Lower numbers run first.
     */
    int getSwitchPriority();

    /**
     * Called before a profile switch occurs.
     *
     * @param player The player switching profiles.
     * @return true if allowed, false if not.
     */
    ProfileOperation verifySwitch(IPlayer player);

    /**
     * NEW: Builds and returns a list of informational strings about this profile data,
     * based on the provided INbt from the slot.
     *
     * @param player   The player in question.
     * @param compound The NBT data from the slot under this profile data's tag.
     * @return A list of InfoEntry with information
     */
    List<ProfileInfoEntry> getInfo(IPlayer player, INbt compound);
}
