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
import noppes.npcs.CustomNpcs;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.controllers.PlayerDataController;
import java.util.ArrayList;
import java.util.List;

public class RolePostman extends RoleInterface {

    public NpcMiscInventory inventory = new NpcMiscInventory(1);
    private List<IPlayer> recentlyChecked = new ArrayList<IPlayer>();
    private List<IPlayer> toCheck;

    public RolePostman(EntityNPCInterface npc) {
        super(npc);
    }

    public boolean aiShouldExecute() {
        if (npc.ticksExisted % 20 != 0)
            return false;

        toCheck = npc.worldObj.getEntitiesWithinAABB(IPlayer.class, npc.boundingBox.expand(10, 10, 10));
        toCheck.removeAll(recentlyChecked);

        List<IPlayer> listMax = npc.worldObj.getEntitiesWithinAABB(IPlayer.class, npc.boundingBox.expand(20, 20, 20));
        recentlyChecked.retainAll(listMax);
        recentlyChecked.addAll(toCheck);

        for (IPlayer player : toCheck) {
            if (PlayerDataController.Instance.hasMail(player))
                player.addChatMessage(new ITextComponent("You've got mail"));
        }
        return false;
    }

    @Override
    public INbt writeToNBT(INbt INbt) {
        INbt.setTag("PostInv", inventory.getToNBT());
        return INbt;
    }

    @Override
    public void readFromNBT(INbt INbt) {
        inventory.setFromNBT(INbt.getCompoundTag("PostInv"));
    }


    @Override
    public void interact(IPlayer player) {
        player.openGui(CustomNpcs.instance, EnumGuiType.PlayerMailman.ordinal(), player.worldObj, 1, 1, 0);
    }

}
