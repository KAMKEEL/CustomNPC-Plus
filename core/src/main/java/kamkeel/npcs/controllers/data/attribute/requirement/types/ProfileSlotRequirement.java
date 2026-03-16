package kamkeel.npcs.controllers.data.attribute.requirement.types;

import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import kamkeel.npcs.controllers.data.attribute.requirement.IRequirementChecker;
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
    public String getTooltipValue(INbt nbt) {
        if (nbt.hasKey(getKey())) {
            int profileSlot = nbt.getInteger(getKey());
            return String.valueOf(profileSlot);
        }
        return "null";
    }

    @Override
    public void apply(INbt nbt, Object value) {
        if (value instanceof Integer) {
            nbt.setInteger(getKey(), (Integer) value);
        }
    }

    @Override
    public Object getValue(INbt nbt) {
        if (nbt.hasKey(getKey())) {
            return nbt.getInteger(getKey());
        }
        return null;
    }

    @Override
    public boolean check(IPlayer player, INbt nbt) {
        if (nbt.hasKey(getKey())) {
            int profileSlot = nbt.getInteger(getKey());
            return PlayerData.get(player).profileSlot == profileSlot;
        }
        return true;
    }
}
