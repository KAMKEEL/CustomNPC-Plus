package kamkeel.npcs.controllers.data.attribute.requirement;


import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.IWorld;

public interface IRequirementChecker {

    String getKey();

    String getTranslation();

    String getTooltipValue(INbt nbt);

    Object getValue(INbt nbt);

    void apply(INbt nbt, Object value);

    /**
     * Checks the requirement in the provided NBT.
     * Return false if the requirement isn’t met.
     */
    boolean check(IPlayer player, INbt nbt);
}
