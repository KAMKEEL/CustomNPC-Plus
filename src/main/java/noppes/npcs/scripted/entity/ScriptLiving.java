package noppes.npcs.scripted.entity;

import net.minecraft.entity.EntityLiving;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.IEntityLivingBase;

public class ScriptLiving<T extends EntityLiving> extends ScriptLivingBase<T> implements IEntityLiving {

	private T entity;
	public ScriptLiving(T entity) {
		super(entity);
		this.entity = entity;
	}

	@Override
	public boolean isAttacking(){
		return super.isAttacking() || entity.getAttackTarget() != null;
	}

	@Override
	public void setAttackTarget(IEntityLivingBase living){
		if(living == null)
			entity.setAttackTarget(null);
		else
			entity.setAttackTarget(living.getMCEntity());
		super.setAttackTarget(living);
	}

	@Override
	public IEntityLivingBase getAttackTarget(){
		IEntityLivingBase base = (IEntityLivingBase) NpcAPI.Instance().getIEntity(entity.getAttackTarget());
		return (base != null)? base: super.getAttackTarget();
	}
	
	/**
	 * Start path finding toward this target
	 * @param x Destination x position
	 * @param y Destination x position
	 * @param z Destination x position
	 * @param speed Walking speed of the entity 0.7 is default
	 */
	public void navigateTo(double x, double y, double z, double speed){
		entity.getNavigator().tryMoveToXYZ(x, y, z, speed);
	}
	
	/**
	 * Stop navigating wherever this npc was walking to
	 */
	public void clearNavigation(){
		entity.getNavigator().clearPathEntity();
	}
	
	/**
	 * @return Whether or not this entity is navigating somewhere
	 */
	public boolean isNavigating(){
		return !entity.getNavigator().noPath();
	}
	
	@Override
	public boolean canSeeEntity(IEntity entity){
		return this.entity.getEntitySenses().canSee(entity.getMCEntity());
	}

	public void playLivingSound() {
		this.entity.playLivingSound();
	}

	public void spawnExplosionParticle()
	{
		this.entity.spawnExplosionParticle();
	}

	public void setMoveForward(float speed)
	{
		this.entity.setMoveForward(speed);
	}

	public void faceEntity(IEntity entity, float pitch, float yaw)
	{
		this.entity.faceEntity(entity.getMCEntity(), pitch, yaw);
	}

	public boolean canPickUpLoot()
	{
		return this.entity.canPickUpLoot();
	}

	public void setCanPickUpLoot(boolean pickUp)
	{
		this.entity.setCanPickUpLoot(pickUp);
	}

	public boolean isPersistent()
	{
		return this.entity.isNoDespawnRequired();
	}

	public void enablePersistence() {
		this.entity.func_110163_bv();
	}

	public void setCustomNameTag(String text)
	{
		this.entity.setCustomNameTag(text);
	}

	public String getCustomNameTag()
	{
		return this.entity.getCustomNameTag();
	}

	public boolean hasCustomNameTag()
	{
		return this.entity.hasCustomNameTag();
	}

	public void setAlwaysRenderNameTag(boolean alwaysRender)
	{
		this.entity.setAlwaysRenderNameTag(alwaysRender);
	}

	public boolean getAlwaysRenderNameTag()
	{
		return this.entity.getAlwaysRenderNameTag();
	}

	public void clearLeashed(boolean sendPacket, boolean dropLeash)
	{
		this.entity.clearLeashed(sendPacket,dropLeash);
	}

	public boolean allowLeashing() {
		return this.entity.allowLeashing();
	}

	public boolean getLeashed() {
		return this.entity.getLeashed();
	}

	public IEntity getLeashedTo() {
		return NpcAPI.Instance().getIEntity(entity.getLeashedToEntity());
	}

	public void setLeashedTo(IEntity entity, boolean sendPacket) {
		this.entity.setLeashedToEntity(entity.getMCEntity(), sendPacket);
	}
}
