package noppes.npcs.roles;

import noppes.npcs.constants.ClientOnly;
import kamkeel.npcs.util.IVector3;
import kamkeel.npcs.controllers.data.ability.preview.PreviewEntityHandler;
import kamkeel.npcs.controllers.data.ability.gui.SubGuiAbilityConfig;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityConfigCallback;
import kamkeel.npcs.controllers.data.ability.gui.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.IChainedAbilityFieldProvider;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityFieldProvider;
import noppes.npcs.entity.EntityNPCInterface;
import kamkeel.npcs.entity.EntityEnergyDome;
import kamkeel.npcs.entity.EntityEnergyBarrier;
import kamkeel.npcs.entity.EntityEnergyPanel;
import kamkeel.npcs.entity.EntityAbilityOrb;
import kamkeel.npcs.entity.EntityAbilityLaser;
import kamkeel.npcs.entity.EntityAbilityDisc;
import kamkeel.npcs.entity.EntityAbilityBeam;
import kamkeel.npcs.entity.EntityEnergyProjectile;
import kamkeel.npcs.util.ByteBufUtils;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import noppes.npcs.NBTTags;
import java.util.ArrayList;
import java.util.List;

public class JobGuard extends JobInterface {

    public boolean attacksAnimals = false;
    public boolean attackHostileMobs = true;
    public boolean attackCreepers = false;

    public List<String> targets = new ArrayList<String>();
    public boolean specific = false;

    public JobGuard(EntityNPCInterface npc) {
        super(npc);
    }

    @Override
    public INbt writeToNBT(INbt INbt) {
        INbt.setBoolean("GuardAttackAnimals", attacksAnimals);
        INbt.setBoolean("GuardAttackMobs", attackHostileMobs);
        INbt.setBoolean("GuardAttackCreepers", attackCreepers);
        INbt.setBoolean("GuardSpecific", specific);

        INbt.setTag("GuardTargets", NBTTags.nbtStringList(targets));
        return INbt;
    }

    @Override
    public void readFromNBT(INbt INbt) {
        attacksAnimals = INbt.getBoolean("GuardAttackAnimals");
        attackHostileMobs = INbt.getBoolean("GuardAttackMobs");
        attackCreepers = INbt.getBoolean("GuardAttackCreepers");
        specific = INbt.getBoolean("GuardSpecific");

        targets = NBTTags.getStringList(INbt.getTagList("GuardTargets", 10));
    }

    public boolean isEntityApplicable(IEntity IEntity) {
        if (IEntity instanceof IPlayer || IEntity instanceof EntityNPCInterface)
            return false;
        if (specific && targets.contains("IEntity." + EntityList.getEntityString(IEntity) + ".name"))
            return true;

        if (IEntity instanceof EntityAnimal) {
            if (!attacksAnimals || IEntity instanceof EntityTameable && ((EntityTameable) IEntity).getOwner() != null)
                return false;
            return true;
        } else if (IEntity instanceof EntityCreeper) {
            return attackCreepers;
        } else if (IEntity instanceof IMob || IEntity instanceof EntityDragon) {
            return attackHostileMobs;
        }
        return false;
    }
}
