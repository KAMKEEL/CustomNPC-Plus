package noppes.npcs.scripted.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.Vec3;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.ScriptDamageSource;
import noppes.npcs.scripted.constants.EntityType;
import noppes.npcs.api.IBlock;
import noppes.npcs.api.IPos;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;

public class ScriptLivingBase<T extends EntityLivingBase> extends ScriptEntity<T> implements IEntityLivingBase {
	protected T entity;
	
	public ScriptLivingBase(T entity){
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

	public void hurt(float damage){
		entity.attackEntityFrom(DamageSource.generic,damage);
	}

	public void hurt(float damage, IEntity source) {
		if(source.getType() == 1)//if player
			entity.attackEntityFrom(new EntityDamageSource("player",source.getMCEntity()),damage);
		else
			entity.attackEntityFrom(new EntityDamageSource(source.getTypeName(),source.getMCEntity()),damage);
	}

	public void hurt(float damage, IDamageSource damageSource) {
		entity.attackEntityFrom(damageSource.getMCDamageSource(),damage);
	}
	
	/**
	 * @return Entity's max health
	 */
	public double getMaxHealth(){
		return entity.getEntityAttribute(SharedMonsterAttributes.maxHealth).getAttributeValue();
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
	public void setAttackTarget(IEntityLivingBase living){
		if(living == null)
			entity.setRevengeTarget(null);
		else
			entity.setRevengeTarget(living.getMCEntity());
	}
		
	/**
	 * @return The entity which this entity is attacking
	 */
	public IEntityLivingBase getAttackTarget(){
		return (IEntityLivingBase)NpcAPI.Instance().getIEntity(entity.getAITarget());
	}
	
	@Override
	public int getType(){
		return EntityType.LIVING;
	}


	@Override
	public boolean typeOf(int type){
		return type == EntityType.LIVING || super.typeOf(type);
	}
	/**
	 * @param entity Entity to check
	 * @return Whether or not this entity can see the given entity
	 */
	public boolean canSeeEntity(IEntity entity){
		return this.entity.canEntityBeSeen(entity.getMCEntity());
	}

	public IPos getLookVector() {
		Vec3 lookVec = entity.getLookVec();
		return NpcAPI.Instance().getIPos(lookVec.xCoord,lookVec.yCoord,lookVec.zCoord);
	}

	public IBlock getLookingAtBlock(int maxDistance, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision) {
		Vec3 lookVec = entity.getLookVec();
		return getWorld().rayCastBlock(
				new double[] {entity.posX, entity.posY+entity.getEyeHeight(), entity.posZ},
				new double[] {lookVec.xCoord, lookVec.yCoord, lookVec.zCoord},
				maxDistance, stopOnBlock, stopOnLiquid, stopOnCollision);
	}

	public IBlock getLookingAtBlock(int maxDistance) {
		return getLookingAtBlock(maxDistance, true, false, false);
	}

	public IPos getLookingAtPos(int maxDistance, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision) {
		Vec3 lookVec = entity.getLookVec();
		return getWorld().rayCastPos(
				new double[] {entity.posX, entity.posY+entity.getEyeHeight(), entity.posZ},
				new double[] {lookVec.xCoord, lookVec.yCoord, lookVec.zCoord},
				maxDistance, stopOnBlock, stopOnLiquid, stopOnCollision);
	}

	public IPos getLookingAtPos(int maxDistance) {
		return getLookingAtPos(maxDistance, true, false, false);
	}

	public IEntity[] getLookingAtEntities(int maxDistance, double offset, double range, boolean stopOnBlock, boolean stopOnLiquid, boolean stopOnCollision) {
		Vec3 lookVec = entity.getLookVec();
		double[] startPos = new double[] {entity.posX, entity.posY+entity.getEyeHeight(), entity.posZ};
		double[] lookVector = new double[] {lookVec.xCoord, lookVec.yCoord, lookVec.zCoord};

		return getWorld().rayCastEntities(startPos,lookVector,maxDistance,offset,range,stopOnBlock,stopOnLiquid,stopOnCollision);
	}

	public IEntity[] getLookingAtEntities(int maxDistance, double offset, double range) {
		return getLookingAtEntities(maxDistance,offset,range,true,false,true);
	}
		
	/**
	 * Expert use only
	 * @return Returns the minecraft entity object
	 */
	public T getMCEntity(){
		return entity;
	}

	public T getMinecraftEntity(){
		return getMCEntity();
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

		if(duration < 0)
			duration = 0;
		
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
	public IItemStack getHeldItem(){
		return NpcAPI.Instance().getIItemStack(entity.getHeldItem());
	}
	
	/**
	 * Note not all Living Entities support this
	 * @since 1.7.10c
	 * @param item The item to be set
	 */
	public void setHeldItem(IItemStack item){
		entity.setCurrentItemOrArmor(0, item == null?null:item.getMCItemStack());
	}
	
	/**
	 * Note not all Living Entities support this
	 * @param slot Slot of what armor piece to get, 0:boots, 1:pants, 2:body, 3:head
	 * @return The item in the given slot
	 */
	public IItemStack getArmor(int slot){
		return NpcAPI.Instance().getIItemStack(entity.getEquipmentInSlot(slot + 1));
	}
	
	/**
	 * Note not all Living Entities support this
	 * @since 1.7.10c
	 * @param slot Slot of what armor piece to set, 0:boots, 1:pants, 2:body, 3:head
	 * @param item Item to be set
	 */
	public void setArmor(int slot, IItemStack item){
		entity.setCurrentItemOrArmor(slot + 1, item == null?null:item.getMCItemStack());
	}

	public boolean isChild() {
		return this.entity.isChild();
	}

	public void renderBrokenItemStack(IItemStack itemStack)
	{
		this.entity.renderBrokenItemStack(itemStack.getMCItemStack());
	}

	public boolean isOnLadder()
	{
		return this.entity.isOnLadder();
	}

	public int getTotalArmorValue()
	{
		return this.entity.getTotalArmorValue();
	}

	public int getArrowCountInEntity() {
		return this.entity.getArrowCountInEntity();
	}

	public void setArrowCountInEntity(int count)
	{
		this.entity.setArrowCountInEntity(count);
	}

	public void dismountEntity(IEntity entity) {
		this.entity.dismountEntity(entity.getMCEntity());
	}

	public void setAIMoveSpeed(float speed)
	{
		this.entity.setAIMoveSpeed(speed);
	}

	public float getAIMoveSpeed() {
		return this.entity.getAIMoveSpeed();
	}

	public void setAbsorptionAmount(float amount)
	{
		this.entity.setAbsorptionAmount(amount);
	}

	public float getAbsorptionAmount() {
		return this.entity.getAbsorptionAmount();
	}
}
