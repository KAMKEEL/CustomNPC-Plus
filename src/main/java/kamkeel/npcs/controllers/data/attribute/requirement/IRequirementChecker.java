package kamkeel.npcs.controllers.data.attribute.requirement;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public interface IRequirementChecker {

    public String getKey();

    public String getTranslation();

    String getTooltipValue(NBTTagCompound nbt);

    Object getValue(NBTTagCompound nbt);

    void apply(NBTTagCompound nbt, Object value);

    /**
     * Checks the requirement in the provided NBT.
     * Return false if the requirement isn’t met.
     */
    boolean check(EntityPlayer player, NBTTagCompound nbt);
}
