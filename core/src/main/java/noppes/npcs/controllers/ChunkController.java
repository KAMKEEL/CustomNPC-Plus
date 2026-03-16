package noppes.npcs.controllers;

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
import noppes.npcs.config.ConfigMain;
import noppes.npcs.constants.EnumJobType;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ChunkController implements LoadingCallback {
    public static ChunkController Instance;

    public HashMap<IEntity, Ticket> tickets = new HashMap<IEntity, Ticket>();

    public ChunkController() {
        Instance = this;
    }

    public void clear() {
        tickets = new HashMap<IEntity, Ticket>();
    }

    public Ticket getTicket(EntityNPCInterface npc) {
        Ticket ticket = tickets.get(npc);
        if (ticket != null)
            return ticket;
        if (size() >= ConfigMain.ChunkLoaders)
            return null;
        ticket = ForgeChunkManager.requestTicket(CustomNpcs.instance, npc.worldObj, Type.IEntity);
        if (ticket == null)
            return null;
        ticket.bindEntity(npc);
        ticket.setChunkListDepth(6);
        tickets.put(npc, ticket);
        return null;
    }

    public void deleteNPC(EntityNPCInterface npc) {
        Ticket ticket = tickets.get(npc);
        if (ticket != null) {
            tickets.remove(npc);
            ForgeChunkManager.releaseTicket(ticket);
        }
    }

    @Override
    public void ticketsLoaded(List<Ticket> tickets, IWorld IWorld) {
        for (Ticket ticket : tickets) {
            if (!(ticket.getEntity() instanceof EntityNPCInterface))
                continue;
            EntityNPCInterface npc = (EntityNPCInterface) ticket.getEntity();
            if (npc.advanced.job == EnumJobType.ChunkLoader && !tickets.contains(npc)) {
                this.tickets.put(npc, ticket);
                double x = npc.posX / 16;
                double z = npc.posZ / 16;

                ForgeChunkManager.forceChunk(ticket, new ChunkCoordIntPair(ValueUtil.floorDouble(x), ValueUtil.floorDouble(z)));
                ForgeChunkManager.forceChunk(ticket, new ChunkCoordIntPair(MathHelper.ceiling_double_int(x), MathHelper.ceiling_double_int(z)));
                ForgeChunkManager.forceChunk(ticket, new ChunkCoordIntPair(ValueUtil.floorDouble(x), MathHelper.ceiling_double_int(z)));
                ForgeChunkManager.forceChunk(ticket, new ChunkCoordIntPair(MathHelper.ceiling_double_int(x), ValueUtil.floorDouble(z)));
            }
        }
    }

    public int size() {
        return tickets.size();
    }

    public void unload(int toRemove) {
        Iterator<IEntity> ite = tickets.keySet().iterator();
        int i = 0;
        while (ite.hasNext()) {
            if (i >= toRemove)
                return;
            IEntity IEntity = ite.next();
            ForgeChunkManager.releaseTicket(tickets.get(IEntity));
            ite.remove();
            i++;
        }
    }
}
