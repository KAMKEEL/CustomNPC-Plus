package noppes.npcs.entity;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class EntityMagicProjectile extends EntityProjectile{
	private EntityPlayer player;
	private ItemStack equiped;
	public EntityMagicProjectile(World par1World,
			EntityPlayer player, ItemStack item, boolean isNPC) {
		super(par1World, player, item, isNPC);
		
		this.player = player;
		this.equiped = player.inventory.getCurrentItem();
	}

	@Override
	public void onUpdate(){
		if(player.inventory.getCurrentItem() != equiped)
			this.setDead();
		super.onUpdate();
	}
	
	@Override
    public String getCommandSenderName(){
        return StatCollector.translateToLocal("entity.throwableitem.name");
    }
}
