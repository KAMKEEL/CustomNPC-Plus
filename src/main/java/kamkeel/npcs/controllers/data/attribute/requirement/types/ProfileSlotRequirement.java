package kamkeel.npcs.controllers.data.attribute.requirement.types;

import kamkeel.npcs.controllers.data.attribute.requirement.IRequirementChecker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.data.PlayerData;

public class ProfileSlotRequirement implements IRequirementChecker {

    @Override
    public String getKey() {
        return "cnpc_profile_slot";
    }

    @Override
    public String getTranslation() {
        return "profile.slot";
    }

    @Override
    public String getTooltipValue(NBTTagCompound nbt) {
        if (nbt.hasKey(getKey())) {
            int profileSlot = nbt.getInteger(getKey());
            return String.valueOf(profileSlot);
        }
        return "null";
    }

    @Override
    public void apply(NBTTagCompound nbt, Object value) {
        if (value instanceof Integer) {
            nbt.setInteger(getKey(), (Integer) value);
        }
    }

    @Override
    public Object getValue(NBTTagCompound nbt) {
        if (nbt.hasKey(getKey())) {
            return nbt.getInteger(getKey());
        }
        return null;
    }

    @Override
    public boolean check(EntityPlayer player, NBTTagCompound nbt) {
        if (nbt.hasKey(getKey())) {
            int profileSlot = nbt.getInteger(getKey());
            return PlayerData.get(player).profileSlot == profileSlot;
        }
        return true;
    }
}
