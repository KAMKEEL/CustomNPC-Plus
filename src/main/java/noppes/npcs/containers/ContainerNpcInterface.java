package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.MathHelper;
import noppes.npcs.scripted.interfaces.IContainer;
import noppes.npcs.scripted.ScriptContainer;

public class ContainerNpcInterface extends Container{
	private int posX, posZ;
	public EntityPlayer player;
	public IContainer scriptContainer;

	public ContainerNpcInterface(EntityPlayer player){
    	posX = MathHelper.floor_double(player.posX);
    	posZ = MathHelper.floor_double(player.posZ);
    	player.motionX = 0;
    	player.motionZ = 0;
	}
	@Override
	public boolean canInteractWith(EntityPlayer player) {
        return !player.isDead && posX == MathHelper.floor_double(player.posX) && posZ == MathHelper.floor_double(player.posZ);
	}

	public static IContainer getOrCreateIContainer(ContainerNpcInterface container) {
		if (container.scriptContainer != null) {
			return container.scriptContainer;
		} else {
			return container.scriptContainer = new ScriptContainer(container);
		}
	}
}
