package noppes.npcs.entity;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class EntityDialogNpc extends EntityNPCInterface{

	public EntityDialogNpc(World world) {
		super(world);
	}


	@Override
    public boolean isInvisibleToPlayer(EntityPlayer player){
        return true;
    }

	@Override
	public boolean isInvisible(){
		return true;
	}

	@Override
	public void onUpdate(){
		
	}
    
	@Override
	public boolean interact(EntityPlayer player) {
		return false;
	}
}
