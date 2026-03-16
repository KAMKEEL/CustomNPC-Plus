package noppes.npcs.roles.companion;

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
public class CompanionGuard extends CompanionJobInterface {
    public boolean isStanding = false;

    @Override
    public INbt getNBT() {
        INbt compound = new INbt();
        compound.setBoolean("CompanionGuardStanding", isStanding);
        return compound;
    }

    @Override
    public void setNBT(INbt compound) {
        isStanding = compound.getBoolean("CompanionGuardStanding");
    }

    public boolean isEntityApplicable(IEntity IEntity) {

        if (IEntity instanceof IPlayer || IEntity instanceof EntityNPCInterface)
            return false;

        else if (IEntity instanceof EntityCreeper) {
            return false;
        } else if (IEntity instanceof IMob) {
            return true;
        }
        return false;
    }

    public boolean isSelfSufficient() {
        return isStanding;
    }
}
