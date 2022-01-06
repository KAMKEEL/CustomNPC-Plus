package noppes.npcs.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.entity.EntityNPCInterface;

@Cancelable
public class NPCDeathEvent extends Event {

    private EntityLiving npc;
    private EntityNPCInterface npcInterface;

    private EntityPlayer killer;

    public NPCDeathEvent(EntityLiving npc, EntityPlayer killer) {
        this.npc = npc;
        this.killer = killer;
        this.npcInterface = (EntityNPCInterface) npc;
    }

    public EntityLiving getNPC() {
        return npc;
    }

    public EntityPlayer getKiller() {
        return killer;
    }

    public EntityNPCInterface getInterface() {
        return npcInterface;
    }

}
