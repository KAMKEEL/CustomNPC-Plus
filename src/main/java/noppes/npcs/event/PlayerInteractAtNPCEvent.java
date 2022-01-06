package noppes.npcs.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.entity.EntityNPCInterface;

@Cancelable
public class PlayerInteractAtNPCEvent extends Event {

    private EntityPlayer entityPlayer;
    private EntityNPCInterface npcInterface;

    public PlayerInteractAtNPCEvent(EntityPlayer player, EntityNPCInterface npc){
    	entityPlayer = player;
    	npcInterface = npc;
    }

    public EntityPlayer getPlayer(){
    	return entityPlayer;
    }

    public EntityNPCInterface getNPC(){
    	return npcInterface;
    }

}
