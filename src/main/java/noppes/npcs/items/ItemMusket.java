package noppes.npcs.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.constants.EnumParticleType;
import noppes.npcs.entity.EntityProjectile;

import org.lwjgl.opengl.GL11;

public class ItemMusket extends ItemNpcInterface{

	public ItemMusket(int par1) {
		super(par1);
        this.setMaxDamage(129);
        setCreativeTab(CustomItems.tabWeapon);
	}
    public void onPlayerStoppedUsing(ItemStack stack, World par2World, EntityPlayer player, int count) {
    	if(player.worldObj.isRemote)
    		return;
    	
    	if(!stack.stackTagCompound.getBoolean("IsLoaded2") && !player.capabilities.isCreativeMode || !CustomNpcs.GunsEnabled) {
    		player.worldObj.playSoundAtEntity(player, "customnpcs:gun.empty", 1.0F,1);
    		return;
    	}
    	
		if(stack.stackTagCompound.getBoolean("Reloading2") && !player.capabilities.isCreativeMode) {
			stack.stackTagCompound.setBoolean("Reloading2", false);
			return;
		}
		stack.damageItem(1, player);
		EntityProjectile projectile = new EntityProjectile(player.worldObj, player, new ItemStack(CustomItems.bulletBlack,1, 0), false);
		projectile.damage = 16;
		projectile.setSpeed(50);
		projectile.setParticleEffect(EnumParticleType.Smoke);
		projectile.shoot(2);
		
		if(!player.capabilities.isCreativeMode)
			consumeItem(player, CustomItems.bulletBlack);
		
		player.worldObj.playSoundAtEntity(player, "random.explode", 0.9F, itemRand.nextFloat() * 0.3F + 1.8F);
		player.worldObj.playSoundAtEntity(player, "ambient.weather.thunder", 2.0F, itemRand.nextFloat() * 0.3F + 1.8F);
		player.worldObj.spawnEntityInWorld(projectile);
		stack.stackTagCompound.setBoolean("IsLoaded2", false);

    }

	@Override
    public void onUsingTick(ItemStack stack, EntityPlayer player, int count) 
    {
    	if(player.worldObj.isRemote){
    		return;
    	}
    	
    	int ticks = getMaxItemUseDuration(stack) - count;

    	if(!player.capabilities.isCreativeMode){
	    	if(stack.stackTagCompound.getBoolean("Reloading2") && hasItem(player, CustomItems.bulletBlack)){
	    		if(ticks == 60){
	    			player.worldObj.playSoundAtEntity(player, "customnpcs:gun.ak47.load", 1.0F,1);
	    			stack.stackTagCompound.setBoolean("IsLoaded2", true);
	    		}
	    		return;
	    	}
    	}
    }
    
	@Override
	public void renderSpecial(){
		GL11.glRotatef(-6, 0, 0, 1f);
        GL11.glScalef(0.7f, 0.7f,0.7f);
    	GL11.glTranslatef(0.4f, 0.0f, 0.2F);
	}
    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
	@Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
		if(stack.stackTagCompound == null)
			stack.stackTagCompound = new NBTTagCompound();
		if(!player.capabilities.isCreativeMode && hasItem(player, CustomItems.bulletBlack) && !stack.stackTagCompound.getBoolean("IsLoaded2"))
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