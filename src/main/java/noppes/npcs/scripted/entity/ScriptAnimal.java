package noppes.npcs.scripted.entity;

import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.scripted.constants.EntityType;
import noppes.npcs.api.entity.IAnimal;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.item.IItemStack;

public class ScriptAnimal<T extends EntityAnimal> extends ScriptLiving<T> implements IAnimal {
	protected T entity;

	public ScriptAnimal(T entity) {
		super(entity);
		this.entity = entity;
	}
	
	@Override
	public int getType(){
		return EntityType.ANIMAL;
	}

	/**
	 * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
	 * the animal type)
	 */
	public boolean isBreedingItem(IItemStack itemStack) {
		return this.entity.isBreedingItem(itemStack.getMCItemStack());
	}

	/**
	 * Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
	 */
	public boolean interact(IPlayer player) {
		return this.entity.interact((EntityPlayer) player.getMCEntity());
	}

	public boolean isInLove() {
		return this.entity.isInLove();
	}

	public void resetInLove() {
		this.entity.resetInLove();
	}

	public boolean canMateWith(IAnimal animal) {
		return this.entity.canMateWith((EntityAnimal) animal.getMCEntity());
	}

	@Override
	public boolean typeOf(int type){
		return type == EntityType.ANIMAL || super.typeOf(type);
	}
}
