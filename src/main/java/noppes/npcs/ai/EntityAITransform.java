package noppes.npcs.ai;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.Server;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAITransform extends EntityAIBase {

	private EntityNPCInterface npc;
	public EntityAITransform(EntityNPCInterface npc){
		this.npc = npc;
		setMutexBits(AiMutex.PASSIVE);
	}
	
	@Override
	public boolean shouldExecute() {
		if(npc.isKilled() || npc.isAttacking() || npc.transform.editingModus)
			return false;

		return npc.worldObj.getWorldTime() % 24000 < 12000?npc.transform.isActive:!npc.transform.isActive;
	}
	
    public void startExecuting(){
    	npc.transform.transform(!npc.transform.isActive);
    }
}
