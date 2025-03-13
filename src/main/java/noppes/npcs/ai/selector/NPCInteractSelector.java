package noppes.npcs.ai.selector;

import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import noppes.npcs.entity.EntityNPCInterface;

public class NPCInteractSelector implements IEntitySelector {
    private final EntityNPCInterface npc;

    public NPCInteractSelector(EntityNPCInterface npc) {
        this.npc = npc;
    }

    @Override
    public boolean isEntityApplicable(Entity entity) {
        if (entity == npc || !(entity instanceof EntityNPCInterface) || !npc.isEntityAlive())
            return false;
        EntityNPCInterface selected = (EntityNPCInterface) entity;
        return !selected.isAttacking() && !npc.getFaction().isAggressiveToNpc(selected) && npc.ais.stopAndInteract;
    }

}
