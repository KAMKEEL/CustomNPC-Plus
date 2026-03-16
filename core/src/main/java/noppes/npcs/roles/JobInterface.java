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
public abstract class JobInterface {
    public EntityNPCInterface npc;

    public boolean overrideMainHand = false;
    public boolean overrideOffHand = false;

    public IItemStack mainhand = null;
    public IItemStack offhand = null;

    public JobInterface(EntityNPCInterface npc) {
        this.npc = npc;
    }

    public abstract INbt writeToNBT(INbt INbt);

    public abstract void readFromNBT(INbt INbt);

    public void killed() {
    }

    ;

    public void delete() {
    }

    ;

    public boolean aiShouldExecute() {
        return false;
    }

    public boolean aiContinueExecute() {
        return aiShouldExecute();
    }

    public void aiStartExecuting() {
    }

    public void aiUpdateTask() {
    }

    public void reset() {
    }

    public void resetTask() {
    }


}
