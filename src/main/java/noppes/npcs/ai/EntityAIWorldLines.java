package noppes.npcs.ai;

import net.minecraft.entity.ai.EntityAIBase;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIWorldLines extends EntityAIBase {

    private EntityNPCInterface npc;
    private int cooldown = 100;

    public EntityAIWorldLines(EntityNPCInterface npc) {
        this.npc = npc;
        this.setMutexBits(AiMutex.PASSIVE);
    }

    @Override
    public boolean shouldExecute() {
        if (cooldown > 0) {
            cooldown--;
        }
        return !npc.isAttacking() && !npc.isKilled() && npc.advanced.hasWorldLines() && npc.getRNG().nextInt(1800) == 1;
    }

    public void startExecuting() {
        cooldown = 100;
        npc.saySurrounding(npc.advanced.getWorldLine());
    }

}
