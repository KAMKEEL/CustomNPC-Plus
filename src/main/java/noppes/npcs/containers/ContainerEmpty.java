package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class ContainerEmpty extends Container{

	@Override
	public boolean canInteractWith(EntityPlayer var1) {
		return false;
	}

}
