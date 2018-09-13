package noppes.npcs;

import noppes.npcs.entity.EntityNPCInterface;

public interface IChatMessages {

	public void addMessage(String message, EntityNPCInterface npc);
	public void renderMessages(double par3, double par5, double par7, float scale);
	
}
