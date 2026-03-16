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
import noppes.npcs.api.handler.data.ITransportLocation;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.controllers.TransportController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerTransportData;
import noppes.npcs.controllers.data.TransportLocation;
import java.util.List;

public class RoleTransporter extends RoleInterface {

    public int transportId = -1;
    public String name;

    public RoleTransporter(EntityNPCInterface npc) {
        super(npc);
    }


    @Override
    public INbt writeToNBT(INbt INbt) {
        INbt.setInteger("TransporterId", transportId);
        return INbt;
    }

    @Override
    public void readFromNBT(INbt INbt) {
        transportId = INbt.getInteger("TransporterId");
        TransportLocation loc = getLocation();
        if (loc != null) {
            name = loc.name;
        }
    }

    private int ticks = 10;

    @Override
    public boolean aiShouldExecute() {
        ticks--;
        if (ticks > 0)
            return false;
        ticks = 10;

        if (!hasTransport())
            return false;

        TransportLocation loc = getLocation();
        if (loc.type != 0)
            return false;

        List<IPlayer> inRange = npc.worldObj.getEntitiesWithinAABB(IPlayer.class, npc.boundingBox.expand(6D, 6D, 6D));
        for (IPlayer player : inRange) {
            if (!npc.canSee(player))
                continue;
            unlock(player, loc);
        }
        return false;

    }

    @Override
    public void aiStartExecuting() {

    }

    @Override
    public void interact(IPlayer player) {
        if (hasTransport()) {
            TransportLocation loc = getLocation();
            if (loc.type == 2) {
                unlock(player, loc);
            }
            NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerTransporter, npc);
        }
    }

    public void unlock(IPlayer player, ITransportLocation loc) {
        PlayerTransportData data = PlayerData.get(player).transportData;
        if (data.transports.contains(transportId))
            return;
        data.transports.add(transportId);
        player.addChatMessage(new ITextComponent("transporter.unlock", loc.getName()));
    }

    public TransportLocation getLocation() {
        if (npc.isRemote())
            return null;
        return TransportController.getInstance().getTransport(transportId);
    }

    public boolean hasTransport() {
        TransportLocation loc = getLocation();
        return loc != null && loc.id == transportId;
    }

    public void setTransport(ITransportLocation location) {
        transportId = location.getId();
        name = location.getName();
    }

}
