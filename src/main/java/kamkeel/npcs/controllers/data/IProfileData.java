package kamkeel.npcs.controllers.data;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public interface IProfileData {

    /**
     * Do not use the tags: Name, lastLoaded, ID.
     *
     * @return The save tag utilized in the Profile.
     */
    public String getTagName();

    /**
     * @return NBTTagCompound of the current data for a given EntityPlayer.
     */
    public NBTTagCompound getCurrentNBT(EntityPlayer player);

    /**
     * Called immediately after setNBT() to replace existing NBT.
     */
    public void save(EntityPlayer player);

    /**
     * Replaces the current data operation when changing profiles.
     */
    public void setNBT(EntityPlayer player, NBTTagCompound replace);

    /**
     * Returns the priority for this ProfileData's verification check.
     * Lower numbers run first.
     */
    public int getSwitchPriority();

    /**
     * Called before a profile switch occurs.
     * @param player The player switching profiles.
     * @return true if allowed, false if not.
     */
    public boolean verifySwitch(EntityPlayer player);
}
