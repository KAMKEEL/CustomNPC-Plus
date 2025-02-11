package kamkeel.npcs.controllers.data;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public interface IProfileData {

    /**
     * Do not use the tag: - Name, lastLoaded, ID
     *
     * @return The Save Tag utilized in the Profile
     */
    public String getTagName();

    /**
     * @return NBTTagCompound of their Current Data for a given Entity Player
     */
    public NBTTagCompound getCurrentNBT(EntityPlayer player);

    /**
     * Called immediately after setNBT() Replaces existing NBT
     */
    public void save(EntityPlayer player);

    /**
     * Replaces their Current Data operation when changing Profiles
     */
    public void setNBT(EntityPlayer player, NBTTagCompound replace);
}
