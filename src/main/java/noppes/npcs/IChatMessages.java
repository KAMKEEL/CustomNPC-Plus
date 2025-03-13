package noppes.npcs;

import noppes.npcs.entity.EntityNPCInterface;

public interface IChatMessages {

    void addMessage(String message, EntityNPCInterface npc);

    void renderMessages(double par3, double par5, double par7, float scale, boolean inRange);

}
