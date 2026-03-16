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
import noppes.npcs.NoppesUtilServer;
import java.util.ArrayList;
import java.util.List;

public class JobHealer extends JobInterface {
    private long healTicks = 0;
    public int range = 5;
    public int speed = 5;

    public JobHealer(EntityNPCInterface npc) {
        super(npc);
    }

    @Override
    public INbt writeToNBT(INbt INbt) {
        INbt.setInteger("HealerRange", range);
        INbt.setInteger("HealerSpeed", speed);
        return INbt;
    }

    @Override
    public void readFromNBT(INbt INbt) {
        range = INbt.getInteger("HealerRange");
        speed = INbt.getInteger("HealerSpeed");
    }

    private List<IEntityLivingBase> toHeal = new ArrayList<IEntityLivingBase>();

    public boolean aiShouldExecute() {
        healTicks++;
        if (healTicks < speed * 10)
            return false;

        for (Object plObj : npc.worldObj.getEntitiesWithinAABB(IEntityLivingBase.class, npc.boundingBox.expand(range, range / 2.0, range))) {
            IEntityLivingBase IEntity = (IEntityLivingBase) plObj;

            if (IEntity instanceof IPlayer) {
                IPlayer player = (IPlayer) IEntity;
                if (player.getHealth() < player.getMaxHealth() && !npc.faction.isAggressiveToPlayer(player))
                    toHeal.add(player);
            }
            if (IEntity instanceof EntityNPCInterface) {
                EntityNPCInterface npc = (EntityNPCInterface) IEntity;
                if (npc.getHealth() < npc.getMaxHealth() && !this.npc.faction.isAggressiveToNpc(npc))
                    toHeal.add(npc);
            }

        }

        healTicks = 0;
        return !toHeal.isEmpty();
    }

    public void aiStartExecuting() {
        for (IEntityLivingBase IEntity : toHeal) {
            float heal = IEntity.getMaxHealth() / 20; //heal 5% of max health
            heal(IEntity, heal > 0 ? heal : 1);
        }
        toHeal.clear();
    }

    public void heal(IEntityLivingBase IEntity, float amount) {
        IEntity.heal(amount);
        NoppesUtilServer.spawnParticle(IEntity, "heal", IEntity.dimension);
    }
}
