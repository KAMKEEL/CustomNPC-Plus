package noppes.npcs.scripted;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTBase.NBTPrimitive;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MathHelper;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.scripted.constants.EntityType;

public class ScriptLivingBase extends ScriptEntity{
	protected EntityLivingBase entity;
	
	public ScriptLivingBase(EntityLivingBase entity){
		super(entity);
		this.entity = entity;
	}

	/**
	 * @return The entity's current health
	 */
	public float getHealth(){
		return entity.getHealth();
	}
	
	/**
	 * @param health The new health of this entity
	 */
	public void setHealth(float health){
		entity.setHealth(health);
	}
	
	/**
	 * @return Entity's max health
	 */
	public float getMaxHealth(){
		return entity.getMaxHealth();
	}
	/**
	 * @return Whether or not this entity is attacking something
	 */
	public boolean isAttacking(){
		return entity.getAITarget() != null;
	}
	
	/**
	 * @param living Entity which this entity will attack
	 */
	public void setAttackTarget(ScriptLivingBase living){
		if(living == null)
			entity.setRevengeTarget(null);
		else
			entity.setRevengeTarget(living.entity);
	}
		
	/**
	 * @return The entity which this entity is attacking
	 */
	public ScriptLivingBase getAttackTarget(){
		return (ScriptLivingBase)ScriptController.Instance.getScriptForEntity(entity.getAITarget());
	}
	
	@Override
	public int getType(){
		return EntityType.LIVING;
	}


	@Override
	public boolean typeOf(int type){
		return type == EntityType.LIVING?true:super.typeOf(type);
	}
	/**
	 * @param entity Entity to check
	 * @return Whether or not this entity can see the given entity
	 */
	public boolean canSeeEntity(ScriptEntity entity){
		return this.entity.canEntityBeSeen(entity.entity);
	}
		
	/**
	 * Expert use only
	 * @return Returns the minecraft entity object
	 */
	public EntityLivingBase getMinecraftEntity(){
		return entity;
	}
	
	/**
	 * Makes the entity swing its hand
	 */
	public void swingHand(){
		entity.swingItem();
	}
	
	/**
	 * Works the same as the <a href="http://minecraft.gamepedia.com/Commands#effect">/effect command</a>
	 * @param effect
	 * @param duration The duration in seconds
	 * @param strength The amplifier of the potion effect
	 * @param hideParticles Whether or not you want to hide potion particles
	 */
	public void addPotionEffect(int effect, int duration, int strength, boolean hideParticles){
        if (effect < 0 || effect >= Potion.potionTypes.length || Potion.potionTypes[effect] == null)
        	return;
        
		if(strength < 0)
			strength = 0;
		else if(strength > 255)
			strength = 255;

		if(duration < 0)
			duration = 0;
		else if(duration > 1000000)
			duration = 1000000;
		
		if(!Potion.potionTypes[effect].isInstant())
			duration *= 20;
		
		if(duration == 0)
			entity.removePotionEffect(effect);
		else
			entity.addPotionEffect(new PotionEffect(effect, duration, strength));
		//TODO in 1.8 add hideParticles option
	}

	/**
	 * Clears all potion effects
	 */
	public void clearPotionEffects(){
		entity.clearActivePotions();
	}
	
	/**
	 * @since 1.7.10c
	 * @param effect Potion effect to check
	 * @return Returns -1 if its not active. Otherwise returns the strenght of the potion
	 */
	public int getPotionEffect(int effect){
		PotionEffect pf = entity.getActivePotionEffect(Potion.potionTypes[effect]);
		if(pf == null)
			return -1;
		return pf.getAmplifier();
	}
	
	/**
	 * Note not all Living Entities support this
	 * @since 1.7.10c
	 * @return The item the entity is holding
	 */
	public ScriptItemStack getHeldItem(){
		ItemStack item = entity.getHeldItem();
		if(item == null)
			return null;
		return new ScriptItemStack(item);
	}
	
	/**
	 * Note not all Living Entities support this
	 * @since 1.7.10c
	 * @param item The item to be set
	 */
	public void setHeldItem(ScriptItemStack item){
		entity.setCurrentItemOrArmor(0, item == null?null:item.item);
	}
	
	/**
	 * Note not all Living Entities support this
	 * @param slot Slot of what armor piece to get, 0:boots, 1:pants, 2:body, 3:head
	 * @return The item in the given slot
	 */
	public ScriptItemStack getArmor(int slot){
		ItemStack item = entity.getEquipmentInSlot(slot + 1);
		if(item == null)
			return null;
		return new ScriptItemStack(item);
	}
	
	/**
	 * Note not all Living Entities support this
	 * @since 1.7.10c
	 * @param slot Slot of what armor piece to set, 0:boots, 1:pants, 2:body, 3:head
	 * @param item Item to be set
	 */
	public void setArmor(int slot, ScriptItemStack item){
		entity.setCurrentItemOrArmor(slot + 1, item == null?null:item.item);
	}
}
