package noppes.npcs.items;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.entity.EntityProjectile;

import org.lwjgl.opengl.GL11;

public class ItemCrossbow extends ItemNpcInterface{

	public ItemCrossbow(int par1) {
		super(par1);
        this.setMaxDamage(129);
        setCreativeTab(CustomItems.tabWeapon);
	}
    public void onPlayerStoppedUsing(ItemStack stack, World par2World, EntityPlayer player, int count) {
    	if(player.worldObj.isRemote)
    		return;
    	
    	if(stack.stackTagCompound.getInteger("IsLoaded") == 1 || player.capabilities.isCreativeMode) {
    		if(stack.stackTagCompound.getInteger("Reloading") == 1 && !player.capabilities.isCreativeMode) {
    			stack.stackTagCompound.setInteger("Reloading", 0);
    			return;
    		}
    		stack.damageItem(1, player);
    		EntityProjectile projectile = new EntityProjectile(player.worldObj, player, new ItemStack(Items.arrow, 1, 0), false);
    		projectile.damage = 10;
    		projectile.setSpeed(20);
    		projectile.setHasGravity(true);
    		projectile.shoot(2);
    		
    		if(!player.capabilities.isCreativeMode)
    			consumeItem(player, CustomItems.crossbowBolt);
    		
    		player.worldObj.playSoundAtEntity(player, "random.bow", 0.9F, itemRand.nextFloat() * 0.3F + 0.8F);
    		player.worldObj.spawnEntityInWorld(projectile);
    		stack.stackTagCompound.setInteger("IsLoaded", 0);
    	}
    }

	@Override
    public void onUsingTick(ItemStack stack, EntityPlayer player, int count) 
    {
    	if(player.worldObj.isRemote){
    		return;
    	}
    	
    	int ticks = getMaxItemUseDuration(stack) - count;

    	if(!player.capabilities.isCreativeMode){
	    	if(stack.stackTagCompound.getInteger("Reloading") == 1 && hasItem(player, CustomItems.crossbowBolt)){
	    		if(ticks == 20){
	    			player.worldObj.playSoundAtEntity(player, "random.click", 1.0F,1);
	    			stack.stackTagCompound.setInteger("IsLoaded", 1);
	    		}
	    		return;
	    	}
    	}
    }
    
	@Override
	public void renderSpecial(){
		GL11.glRotatef(96, 1f, 0, 0);
		GL11.glRotatef(-10, 0, 1f, 0);
        GL11.glScalef(0.8f, 0.8f,0.8f);
    	GL11.glTranslatef(0.5f, -0.7f, -0.4F);
	}
    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
	@Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
		if(stack.stackTagCompound == null)
			stack.stackTagCompound = new NBTTagCompound();
		if(!player.capabilities.isCreativeMode && hasItem(player, CustomItems.crossbowBolt) && stack.stackTagCompound.getInteger("IsLoaded") == 0)
			stack.stackTagCompound.setInteger("Reloading", 1);
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
		if(stack.stackTagCompound == null || stack.stackTagCompound.getInteger("Reloading") == 0)
			return EnumAction.bow;
		
		return EnumAction.block;
    }
}