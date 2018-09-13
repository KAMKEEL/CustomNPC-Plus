package noppes.npcs.items;

import org.lwjgl.opengl.GL11;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.constants.EnumNpcToolMaterial;
import noppes.npcs.enchants.EnchantInterface;
import noppes.npcs.entity.EntityMagicProjectile;
import noppes.npcs.entity.EntityProjectile;
import noppes.npcs.util.IProjectileCallback;

public class ItemStaff extends ItemNpcInterface implements IProjectileCallback{

	private EnumNpcToolMaterial material;
	public ItemStaff(int par1, EnumNpcToolMaterial material) {
		super(par1);
		this.material = material;
		setCreativeTab(CustomItems.tabWeapon);
	}
	public void renderSpecial(){
        GL11.glScalef(1f, 1.14f, 1f);
        GL11.glTranslatef(0.14f, -0.3f, 0.08f);
   }
	@Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldObj, EntityPlayer player, int par4) {
    	if(worldObj.isRemote){
    		return;
    	}
    	if(stack.stackTagCompound == null)
    		return;
		Entity entity = ((WorldServer)player.worldObj).getEntityByID(stack.stackTagCompound.getInteger("MagicProjectile"));
		if(entity == null || !(entity instanceof EntityProjectile))
			return;
		EntityProjectile item = (EntityProjectile) entity;
		item.callback = this;
		item.callbackItem = stack;
		item.explosive = true;
		item.explosiveDamage = false;
		item.explosiveRadius = 1;
		item.prevRotationYaw = item.rotationYaw = player.rotationYaw;
		item.prevRotationPitch = item.rotationPitch = player.rotationPitch;
		item.shoot(2);

		player.worldObj.playSoundAtEntity(player, "customnpcs:magic.shot", 1.0F,1);
    	
	}

	@Override
    public void onUsingTick(ItemStack stack, EntityPlayer player, int count) 
    {
    	int tick = getMaxItemUseDuration(stack) - count;
    	if(player.worldObj.isRemote){
    		spawnParticle(stack, player);
	    	return;
    	}
    	int chargeTime = 20 + material.getHarvestLevel() * 8;
    	if(tick == chargeTime){
    		if(!player.capabilities.isCreativeMode && !hasInfinite(stack)){
    			if(!hasItem(player, CustomItems.mana))
    				return;
    			consumeItem(player, CustomItems.mana);
    		}
    		player.worldObj.playSoundAtEntity(player, "customnpcs:magic.charge", 1.0F,1);
    		if(stack.stackTagCompound == null){
    			stack.stackTagCompound = new NBTTagCompound();
    		}
    		int damage = 6 + material.getDamageVsEntity() + player.worldObj.rand.nextInt(4);
    		damage += damage * EnchantInterface.getLevel(EnchantInterface.Damage, stack) * 0.5f;
        	EntityProjectile projectile = new EntityMagicProjectile(player.worldObj, player, getProjectile(stack), false);
        	projectile.damage = damage;
        	projectile.setSpeed(25);
        	double dx = -MathHelper.sin((float) ((player.rotationYaw / 180F) * Math.PI)) * MathHelper.cos((float) ((player.rotationPitch / 180F) * Math.PI));
        	double dz = MathHelper.cos((float) ((player.rotationYaw / 180F) * Math.PI)) * MathHelper.cos((float) ((player.rotationPitch / 180F) * Math.PI));
        	projectile.setPosition(player.posX + dx * 0.8, player.posY + 1.5 - player.rotationPitch/80, player.posZ + dz * 0.8);
        	player.worldObj.spawnEntityInWorld(projectile);
        	stack.stackTagCompound.setInteger("MagicProjectile", projectile.getEntityId());//entityid
    	}
    	if(tick > chargeTime && stack.stackTagCompound != null){
    		Entity entity = ((WorldServer)player.worldObj).getEntityByID(stack.stackTagCompound.getInteger("MagicProjectile"));
    		if(entity == null || !(entity instanceof EntityProjectile))
    			return;
    		EntityProjectile item = (EntityProjectile) entity;
    		item.ticksInAir = 0;

        	double dx = -MathHelper.sin((float) ((player.rotationYaw / 180F) * Math.PI)) * MathHelper.cos((float) ((player.rotationPitch / 180F) * Math.PI));
        	double dz = MathHelper.cos((float) ((player.rotationYaw / 180F) * Math.PI)) * MathHelper.cos((float) ((player.rotationPitch / 180F) * Math.PI));
        	item.setPosition(player.posX + dx * 0.8, player.posY + 1.5 - player.rotationPitch/80, player.posZ + dz * 0.8);
    	}
    	
    }
	@Override
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer){
    	par3EntityPlayer.setItemInUse(par1ItemStack, this.getMaxItemUseDuration(par1ItemStack));
        return par1ItemStack;
    }
	@Override
    public int getMaxItemUseDuration(ItemStack par1ItemStack){
        return 72000;
    }

	@Override
	public EnumAction getItemUseAction(ItemStack par1ItemStack){
        return EnumAction.bow;
    }
	
	public ItemStack getProjectile(ItemStack stack){
		if(stack.getItem() == CustomItems.staffWood){
			return new ItemStack(CustomItems.spellNature);
		}
		if(stack.getItem() == CustomItems.staffStone || stack.getItem() == CustomItems.staffDemonic){
			return new ItemStack(CustomItems.spellDark);
		}
		if(stack.getItem() == CustomItems.staffIron || stack.getItem() == CustomItems.staffMithril){
			return new ItemStack(CustomItems.spellHoly);
		}
		if(stack.getItem() == CustomItems.staffBronze){
			return new ItemStack(CustomItems.spellLightning);
		}
		if(stack.getItem() == CustomItems.staffGold){
			return new ItemStack(CustomItems.spellFire);
		}
		if(stack.getItem() == CustomItems.staffDiamond || stack.getItem() == CustomItems.staffFrost){
			return new ItemStack(CustomItems.spellIce);
		}
		if(stack.getItem() == CustomItems.staffEmerald){
			return new ItemStack(CustomItems.spellArcane);
		}
		return new ItemStack(CustomItems.orb,1,stack.getItemDamage());
	}
	public void spawnParticle(ItemStack stack, EntityPlayer player){
		if(stack.getItem() == CustomItems.staffWood){
			CustomNpcs.proxy.spawnParticle(player,"Spell",5,2);
			CustomNpcs.proxy.spawnParticle(player,"Spell",12,2);
		}
		else if(stack.getItem() == CustomItems.staffStone || stack.getItem() == CustomItems.staffDemonic){
			CustomNpcs.proxy.spawnParticle(player,"Spell",0x563357,2);
			CustomNpcs.proxy.spawnParticle(player,"Spell",0x432744,2);
		}
		else if(stack.getItem() == CustomItems.staffBronze){
			CustomNpcs.proxy.spawnParticle(player,"Spell",0x83F7F6,2);
			CustomNpcs.proxy.spawnParticle(player,"Spell",0x5CF0FF,2);
		}
		else if(stack.getItem() == CustomItems.staffIron || stack.getItem() == CustomItems.staffMithril){
			CustomNpcs.proxy.spawnParticle(player,"Spell",0xFCFFC9,2);
			CustomNpcs.proxy.spawnParticle(player,"Spell",0xEFFF97,2);
		}
		else if(stack.getItem() == CustomItems.staffGold){
			CustomNpcs.proxy.spawnParticle(player,"Spell",1,2);
			CustomNpcs.proxy.spawnParticle(player,"Spell",14,2);
		}
		else if(stack.getItem() == CustomItems.staffDiamond || stack.getItem() == CustomItems.staffFrost){
			CustomNpcs.proxy.spawnParticle(player,"Spell",0x94DFED,2);
			CustomNpcs.proxy.spawnParticle(player,"Spell",0x44B6FF,2);
		}
		else if(stack.getItem() == CustomItems.staffEmerald){
			CustomNpcs.proxy.spawnParticle(player,"Spell",0xFFC3E7,2);
			CustomNpcs.proxy.spawnParticle(player,"Spell",0xFB92FF,2);
		}
	}
	
    @Override
    public int getItemEnchantability(){
        return this.material.getEnchantability();
    }
    @Override
    public boolean isItemTool(ItemStack par1ItemStack){
        return true;
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

	public boolean hasInfinite(ItemStack stack){
		return EnchantInterface.getLevel(EnchantInterface.Infinite, stack) > 0;
	}
}
