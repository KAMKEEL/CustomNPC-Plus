package noppes.npcs.items;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.constants.EnumNpcToolMaterial;
import noppes.npcs.enchants.EnchantInterface;
import noppes.npcs.entity.EntityProjectile;
import noppes.npcs.util.IProjectileCallback;

import org.lwjgl.opengl.GL11;

public class ItemGun extends ItemNpcInterface implements IProjectileCallback{

	private EnumNpcToolMaterial material;
	public ItemGun(int par1, EnumNpcToolMaterial material) {
		super(par1);
        this.maxStackSize = 1;
		this.material = material;
        this.setMaxDamage(material.getMaxUses());
        setCreativeTab(CustomItems.tabWeapon);
	}
	@Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldObj, EntityPlayer player, int par4) {
    	if(worldObj.isRemote)
    		return;

        if (!hasBullet(player, stack) || !CustomNpcs.GunsEnabled){
    		worldObj.playSoundAtEntity(player, "customnpcs:gun.empty", 1.0F,1);
        	return;
        }
    	int ticks = getMaxItemUseDuration(stack) - par4;
    	if(ticks < 10){
    		return;
    	}
    	stack.damageItem(1, player);
		ItemBullet bullet = (ItemBullet) getBullet();
		int damage = (bullet.getBulletDamage() + material.getDamageVsEntity() + 1) / 2 + 5;
		damage += damage * EnchantInterface.getLevel(EnchantInterface.Damage, stack) * 0.5f;
    	EntityProjectile projectile = new EntityProjectile(worldObj, player, new ItemStack(getBullet()), false);
    	projectile.damage = damage;
    	projectile.callback = this;
    	projectile.callbackItem = stack;
    	projectile.setSpeed(40);
    	projectile.shoot(material.getDamageVsEntity() + 1);
		
		if(!player.capabilities.isCreativeMode && !hasInfinite(stack))
			consumeItem(player, getBullet());
    	
		worldObj.playSoundAtEntity(player, "customnpcs:gun.pistol.shot", 1.0F,itemRand.nextFloat() * 0.3F + 0.8F);
		worldObj.spawnEntityInWorld(projectile);
    }    

	@Override
    public void onUsingTick(ItemStack stack, EntityPlayer player, int count) 
    {
    	int ticks = getMaxItemUseDuration(stack) - count;
    	if(ticks == 8 && !player.worldObj.isRemote){
    		player.worldObj.playSoundAtEntity(player, "customnpcs:gun.pistol.trigger", 1.0F,
    				1.0F / (player.worldObj.rand.nextFloat() * 0.4F + 0.8F));
    	}
    }

	@Override
	public void renderSpecial(){
        GL11.glScalef(0.7f, 0.7f,0.7f);
    	GL11.glTranslatef(0.3f, 0.3f, 0.1f);
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
	private boolean hasBullet(EntityPlayer player, ItemStack stack){
		if(player.capabilities.isCreativeMode || hasInfinite(stack))
			return true;
		return hasItem(player, getBullet());
	}
	private Item getBullet(){
		switch(material){
		case EMERALD:
			return CustomItems.bulletEmerald;
		case DIA:
			return CustomItems.bulletDiamond;
		case IRON:
				return CustomItems.bulletIron;
		case BRONZE:
				return CustomItems.bulletBronze;
		case GOLD:
				return CustomItems.bulletGold;
		case STONE:
				return CustomItems.bulletStone;
		case WOOD:
				return CustomItems.bulletWood;
		default:
			return CustomItems.bulletBlack;
		}
	}
	
	public boolean hasInfinite(ItemStack stack){
		return EnchantInterface.getLevel(EnchantInterface.Infinite, stack) > 0;
	}
	
    @Override
    public int getItemEnchantability(){
        return this.material.getEnchantability();
    }

	public EnumAction getItemUseAction(ItemStack par1ItemStack)
    {
        return EnumAction.bow;
    }
	@Override
	public boolean onImpact(EntityProjectile entityProjectile, EntityLivingBase entity, ItemStack itemstack) {
		int confusion = EnchantInterface.getLevel(EnchantInterface.Confusion, itemstack);
		if(confusion > 0){
			if(entity.getRNG().nextInt(4) > confusion)
				entity.addPotionEffect(new PotionEffect(Potion.confusion.id, 100));
		}
		int poison = EnchantInterface.getLevel(EnchantInterface.Poison, itemstack);
		if(poison > 0){
			if(entity.getRNG().nextInt(4) > poison)
				entity.addPotionEffect(new PotionEffect(Potion.poison.id, 100));
		}
		return false;
	}
}
