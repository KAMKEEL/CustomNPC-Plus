package noppes.npcs.items;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.constants.EnumParticleType;
import noppes.npcs.entity.EntityProjectile;

import org.lwjgl.opengl.GL11;

public class ItemSlingshot extends ItemNpcInterface{

	public ItemSlingshot(int par1) {
		super(par1);
        this.maxStackSize = 1;
        this.setMaxDamage(384);
        setCreativeTab(CustomItems.tabWeapon);
	}
	@Override
    public void onPlayerStoppedUsing(ItemStack par1ItemStack, World worldObj, EntityPlayer player, int par4) {
    	if(worldObj.isRemote)
    		return;
    	int ticks = getMaxItemUseDuration(par1ItemStack) - par4;
    	
    	if(ticks < 6){//prevent rightclick spamming
    		return;
    	}
        if (!player.capabilities.isCreativeMode && !consumeItem(player, Item.getItemFromBlock(Blocks.cobblestone)))
        {
        	return;
        }
        par1ItemStack.damageItem(1, player);
    	EntityProjectile projectile = new EntityProjectile(worldObj, player, new ItemStack(Blocks.cobblestone), false);
    	projectile.damage = 4;
		projectile.punch = 1;
    	projectile.setRotating(true);
    	if(ticks > 24){
    		projectile.setParticleEffect(EnumParticleType.Crit);
    		projectile.punch = 2;
    	}
    	projectile.setHasGravity(true);
    	projectile.setSpeed(14);
    	projectile.shoot(1);
		    	
		worldObj.playSoundAtEntity(player, "random.bow", 1.0F,itemRand.nextFloat() * 0.3F + 0.8F);
		worldObj.spawnEntityInWorld(projectile);
    }

	@Override
	public void renderSpecial(){
		GL11.glRotatef(90, 0, 1, 0);
        GL11.glScalef(0.5f, 0.5f,0.5f);
    	GL11.glTranslatef(0f, 0.5f, 0);
	}
    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
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
	
	public EnumAction getItemUseAction(ItemStack par1ItemStack)
    {
        return EnumAction.bow;
    }
}
