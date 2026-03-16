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
import noppes.npcs.controllers.ChunkController;
import java.util.ArrayList;
import java.util.List;

public class JobChunkLoader extends JobInterface {

    private List<ChunkCoordIntPair> chunks = new ArrayList<ChunkCoordIntPair>();
    private int ticks = 20;
    private long playerLastSeen = 0;

    public JobChunkLoader(EntityNPCInterface npc) {
        super(npc);
    }

    @Override
    public INbt writeToNBT(INbt compound) {
        compound.setLong("ChunkPlayerLastSeen", playerLastSeen);
        return compound;
    }

    @Override
    public void readFromNBT(INbt compound) {
        playerLastSeen = compound.getLong("ChunkPlayerLastSeen");
    }

    @Override
    public boolean aiShouldExecute() {
        ticks--;
        if (ticks > 0)
            return false;
        ticks = 20;

        List players = npc.worldObj.getEntitiesWithinAABB(IPlayer.class, npc.boundingBox.expand(48, 48, 48));
        if (!players.isEmpty())
            playerLastSeen = System.currentTimeMillis();

        //unload after 10 min
        if (System.currentTimeMillis() > playerLastSeen + 600000) {
            ChunkController.Instance.deleteNPC(npc);
            chunks.clear();
            return false;
        }
        Ticket ticket = ChunkController.Instance.getTicket(npc);
        if (ticket == null) //Only null when too many active chunkloaders already
            return false;
        double x = npc.posX / 16;
        double z = npc.posZ / 16;

        List<ChunkCoordIntPair> list = new ArrayList<ChunkCoordIntPair>();
        list.add(new ChunkCoordIntPair(ValueUtil.floorDouble(x), ValueUtil.floorDouble(z)));
        list.add(new ChunkCoordIntPair(MathHelper.ceiling_double_int(x), MathHelper.ceiling_double_int(z)));
        list.add(new ChunkCoordIntPair(ValueUtil.floorDouble(x), MathHelper.ceiling_double_int(z)));
        list.add(new ChunkCoordIntPair(MathHelper.ceiling_double_int(x), ValueUtil.floorDouble(z)));

        for (ChunkCoordIntPair chunk : list) {
            if (!chunks.contains(chunk)) {
                ForgeChunkManager.forceChunk(ticket, chunk);
            } else
                chunks.remove(chunk);
        }

        for (ChunkCoordIntPair chunk : chunks)
            ForgeChunkManager.unforceChunk(ticket, chunk);

        this.chunks = list;
        return false;
    }

    @Override
    public void reset() {
        ChunkController.Instance.deleteNPC(npc);
        chunks.clear();
        playerLastSeen = 0;
    }

    public void delete() {
    }
}
