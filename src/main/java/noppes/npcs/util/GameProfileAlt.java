package noppes.npcs.util;

import java.util.UUID;

import noppes.npcs.entity.EntityNPCInterface;

import com.mojang.authlib.GameProfile;

public class GameProfileAlt extends GameProfile{
	private static final UUID id = UUID.randomUUID();
	public EntityNPCInterface npc;
	public GameProfileAlt() {
		super(null, "customnpc");
	}

	@Override
    public String getName() {
    	if(npc == null)
    		return super.getName();
        return npc.getCommandSenderName();
    }
    
    @Override
    public UUID getId(){
    	if(npc == null)
    		return id;
    	return npc.getPersistentID();
    }
}
