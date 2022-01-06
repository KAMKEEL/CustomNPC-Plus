package noppes.npcs.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.entity.EntityLiving;
import noppes.npcs.entity.EntityNPCInterface;

@Cancelable
public class NPCKillEntityEvent extends Event {

    private EntityLiving entityLiving;
    private EntityNPCInterface npc;

    public NPCKillEntityEvent(EntityLiving entityLiving, EntityNPCInterface npc) {
        this.entityLiving = entityLiving;
        this.npc = npc;
    }

    public EntityLiving getPlayer() {
        return entityLiving;
    }

    public EntityNPCInterface getNPC() {
        return npc;
    }

}
