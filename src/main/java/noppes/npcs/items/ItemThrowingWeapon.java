package noppes.npcs.items;

import org.lwjgl.opengl.GL11;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.entity.EntityProjectile;

public class ItemThrowingWeapon extends ItemNpcInterface{
	private boolean rotating = false;
	private int damage = 2;
	private boolean dropItem = false;
	
	public ItemThrowingWeapon(int par1) {
		super(par1);
		setCreativeTab(CustomItems.tabWeapon);
	}

    public void onPlayerStoppedUsing(ItemStack par1ItemStack, World worldObj, EntityPlayer player, int par4) 
    {
    	if(worldObj.isRemote){
    		player.swingItem();
    		return;
    	}
    	EntityProjectile projectile = new EntityProjectile(worldObj, player, new ItemStack(par1ItemStack.getItem(), 1,par1ItemStack.getItemDamage()), false);
    	projectile.damage = damage;
    	projectile.canBePickedUp = !player.capabilities.isCreativeMode && dropItem;
    	projectile.setRotating(rotating);;
    	projectile.setIs3D(true);
    	projectile.setStickInWall(true);
    	projectile.setHasGravity(true);
    	projectile.setSpeed(12);
		
		if(!player.capabilities.isCreativeMode){
			consumeItem(player, this);
		}
		projectile.shoot(1);
    	worldObj.playSoundAtEntity(player, "customnpcs:misc.swosh", 1.0F,1);
		worldObj.spawnEntityInWorld(projectile);
    }
    public ItemThrowingWeapon setRotating(){
    	rotating = true;
    	return this;
    }
    public ItemThrowingWeapon setDamage(int damage){
    	this.damage = damage;
    	return this;
    }
    public ItemThrowingWeapon setDropItem(){
    	dropItem = true;
    	return this;
    }
    
	@Override
	public void renderSpecial(){
		super.renderSpecial();
        GL11.glTranslatef(0.2F, 0.1f, 0.06f);
	}
    
	@Override
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
    {
    	par3EntityPlayer.setItemInUse(par1ItemStack, this.getMaxItemUseDuration(par1ItemStack));
        return par1ItemStack;
    }
	@Override
    public int getMaxItemUseDuration(ItemStack par1ItemStack)
    {
        return 72000;
    }
}
