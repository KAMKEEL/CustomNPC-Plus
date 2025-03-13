package kamkeel.npcs.controllers.data.profile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

public interface IProfileData {

    /**
     * Do not use the tags: Name, lastLoaded, ID.
     *
     * @return The save tag utilized in the Profile.
     */
    String getTagName();

    /**
     * @return NBTTagCompound of the current data for a given EntityPlayer.
     */
    NBTTagCompound getCurrentNBT(EntityPlayer player);

    /**
     * Called immediately after setNBT() to replace existing NBT.
     */
    void save(EntityPlayer player);

    /**
     * Replaces the current data operation when changing profiles.
     */
    void setNBT(EntityPlayer player, NBTTagCompound replace);

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
    ProfileOperation verifySwitch(EntityPlayer player);

    /**
     * NEW: Builds and returns a list of informational strings about this profile data,
     * based on the provided NBTTagCompound from the slot.
     *
     * @param player   The player in question.
     * @param compound The NBT data from the slot under this profile data's tag.
     * @return A list of InfoEntry with information
     */
    List<ProfileInfoEntry> getInfo(EntityPlayer player, NBTTagCompound compound);
}
