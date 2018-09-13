package noppes.npcs.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.entity.EntityProjectile;

import org.lwjgl.opengl.GL11;

public class ItemMachineGun extends ItemNpcInterface{

	public ItemMachineGun(int par1) {
		super(par1);
        this.setMaxDamage(80);
        setCreativeTab(CustomItems.tabWeapon);
	}
    public void onPlayerStoppedUsing(ItemStack stack, World par2World, EntityPlayer player, int count) {
    	if(player.capabilities.isCreativeMode)
    		return;
    	
    	int ticks = getMaxItemUseDuration(stack) - count;
    	int shotsleft = stack.stackTagCompound.getInteger("ShotsLeft") - ticks/6;
    	if(stack.stackTagCompound.getBoolean("Reloading2")){
    		shotsleft = ticks/5;
    		if(ticks > 40)
    			shotsleft = 8;
    		if(shotsleft > 1){
	    		stack.stackTagCompound.setInteger("ShotsLeft", shotsleft);
	    		stack.stackTagCompound.setBoolean("Reloading2", false);
    		}
    	}
    	else if(shotsleft <= 0){
    		stack.stackTagCompound.setBoolean("Reloading2", true);
    		stack.damageItem(1, player);
    	}
    	else
    		stack.stackTagCompound.setInteger("ShotsLeft", shotsleft);
    }

	@Override
    public void onUsingTick(ItemStack stack, EntityPlayer player, int count) 
    {
    	if(player.worldObj.isRemote){
    		return;
    	}
    	
    	int ticks = getMaxItemUseDuration(stack) - count;
    	if(ticks % 6 != 0){
	    	return;
	    }
    	 
    	int shotsleft = stack.stackTagCompound.getInteger("ShotsLeft") - ticks/6;
    	if(!player.capabilities.isCreativeMode || !CustomNpcs.GunsEnabled){
	    	if(stack.stackTagCompound.getBoolean("Reloading2") && hasItem(player, CustomItems.bulletBlack)){
	    		if(ticks > 0 && ticks <= 24){
	    			player.worldObj.playSoundAtEntity(player, "customnpcs:gun.ak47.load", 1.0F,1);
	    		}
	    		return;
	    	}
	    	else if((shotsleft <= 0 || !hasItem(player, CustomItems.bulletBlack)) || !CustomNpcs.GunsEnabled)
	        {
	    		player.worldObj.playSoundAtEntity(player, "customnpcs:gun.empty", 1.0F,1);
	        	return;
	        }
    	}else{
    		stack.stackTagCompound.removeTag("Reloading2");
    	}
    	EntityProjectile projectile = new EntityProjectile(player.worldObj, player, new ItemStack(CustomItems.bulletBlack,1, 0), false);
    	projectile.damage = 4;
    	projectile.setSpeed(40);
    	projectile.shoot(2);
    			
		if(!player.capabilities.isCreativeMode)
			consumeItem(player, CustomItems.bulletBlack);
    	
		player.worldObj.playSoundAtEntity(player, "customnpcs:gun.pistol.shot", 0.9F, itemRand.nextFloat() * 0.3F + 0.8F);
		player.worldObj.spawnEntityInWorld(projectile);
    	
    	
    }
	@Override
	public void renderSpecial(){
		GL11.glRotatef(-6, 0, 0, 1f);
        GL11.glScalef(0.8f, 0.7f,0.7f);
    	GL11.glTranslatef(0.2f, 0.0f, 0.2F);
	}
    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
	@Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
		if(stack.stackTagCompound == null)
			stack.stackTagCompound = new NBTTagCompound();
		if(!player.capabilities.isCreativeMode && !hasItem(player, CustomItems.bulletBlack))
			stack.stackTagCompound.setBoolean("Reloading2", true);
		player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
        return stack;
    }
	@Override
    public int getMaxItemUseDuration(ItemStack par1ItemStack)
    {
        return 72000;
    }

	public EnumAction getItemUseAction(ItemStack stack)
    {	
		if(stack.stackTagCompound == null || !stack.stackTagCompound.getBoolean("Reloading2"))
			return EnumAction.bow;
		
		return EnumAction.block;
    }
}
