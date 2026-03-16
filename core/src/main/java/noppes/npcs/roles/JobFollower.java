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
import noppes.npcs.config.ConfigMain;
import java.util.List;

public class JobFollower extends JobInterface {
    public EntityNPCInterface following = null;
    private int ticks = 40;
    private int range = 20;
    public String name = "";

    public JobFollower(EntityNPCInterface npc) {
        super(npc);
    }

    @Override
    public INbt writeToNBT(INbt compound) {
        compound.setString("FollowingEntityName", name);
        return compound;
    }

    @Override
    public void readFromNBT(INbt compound) {
        name = compound.getString("FollowingEntityName");

    }

    @Override
    public boolean aiShouldExecute() {
        if (npc.isAttacking())
            return false;

        ticks--;
        if (ticks > 0)
            return false;

        ticks = 10;
        following = null;
        List<EntityNPCInterface> list = npc.worldObj.getEntitiesWithinAABB(EntityNPCInterface.class, npc.boundingBox.expand(getRange(), getRange(), getRange()));
        for (EntityNPCInterface IEntity : list) {
            if (IEntity == npc || IEntity.isKilled())
                continue;
            if (IEntity.display.name.equalsIgnoreCase(name)) {
                following = IEntity;
                break;
            }
        }

        return false;
    }

    private int getRange() {
        if (range > ConfigMain.NpcNavRange)
            return ConfigMain.NpcNavRange;
        return range;
    }

    public boolean isFollowing() {
        return following != null;
    }

    public void reset() {
    }

    public void resetTask() {
        following = null;
    }

    public boolean hasOwner() {
        return !name.isEmpty();
    }
}
