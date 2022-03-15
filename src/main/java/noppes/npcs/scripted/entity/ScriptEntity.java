package noppes.npcs.scripted.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTBase.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.*;
import noppes.npcs.scripted.constants.EntityType;
import noppes.npcs.scripted.interfaces.IEntity;
import noppes.npcs.scripted.interfaces.INbt;
import noppes.npcs.scripted.interfaces.IPos;

public class ScriptEntity<T extends Entity> implements IEntity {
	protected T entity;
	private Map<String,Object> tempData = new HashMap<String,Object>();

	public ScriptEntity(T entity){
		this.entity = entity;
	}

	/**
	 * @param directory The particle's directory. Use only forward slashes when writing a directory. Example: "customnpcs:textures/particle/tail.png"
	 * @param HEXcolor The particle's color as a HEX integer
	 * @param amount The amount of particles to spawn
	 * @param maxAge The particle's maximum age in MC ticks

	 * @param x The particle's x position
	 * @param y The particle's y position
	 * @param z The particle's z position

	 * @param motionX The particle's speed in the x axis
	 * @param motionY The particle's speed in the y axis
	 * @param motionZ The particle's speed in the z axis
	 * @param gravity The particle's gravity

	 * @param scale1 The particle's starting scale
	 * @param scale2 The particle's ending scale
	 * @param scaleRate Multiplier for the particle's scale growth rate
	 * @param scaleRateStart The time at which the particle begins growing/shrinking

	 * @param alpha1 The particle's starting transparency
	 * @param alpha2 The particle's ending transparency
	 * @param scaleRate Multiplier for the particle's transparency growth rate
	 * @param alphaRateStart The time at which the particle begins appearing/fading
	 */
	public void spawnParticle(String directory, int HEXcolor, int amount, int maxAge,
							  double x, double y, double z,
							  double motionX, double motionY, double motionZ, float gravity,
							  float scale1, float scale2, float scaleRate, int scaleRateStart,
							  float alpha1, float alpha2, float alphaRate, int alphaRateStart,
							  float rotation1, float rotation2, float rotationRate, int rotationRateStart,
							  float rotationX1, float rotationX2, float rotationXRate, int rotationXRateStart,
							  float rotationY1, float rotationY2, float rotationYRate, int rotationYRateStart,
							  float rotationZ1, float rotationZ2, float rotationZRate, int rotationZRateStart
	) {
		int entityID = entity.getEntityId();
		NoppesUtilServer.spawnScriptedParticle(directory, HEXcolor, amount, maxAge,
				x, y, z,
				motionX, motionY, motionZ, gravity,
				scale1, scale2, scaleRate, scaleRateStart,
				alpha1, alpha2, alphaRate, alphaRateStart,
				rotationX1, rotationX2, rotationXRate, rotationXRateStart,
				rotationY1, rotationY2, rotationYRate, rotationYRateStart,
				rotationZ1, rotationZ2, rotationZRate, rotationZRateStart,
				entityID
		);
	}
	public void spawnParticle(ScriptEntityParticle entityParticle) { entityParticle.spawnOnEntity(this); }

	public double getYOffset() { return entity.getYOffset(); }

	/**
	 * @return The entities width
	 */
	public double getWidth(){ return entity.width; }

	/**
	 * @return The entities height
	 */
	public double getHeight(){ return entity.height; }

	/**
	 * @return The entities x position
	 */
	public double getX(){
		return entity.posX;
	}

	/**
	 * @param x The entities x position
	 */
	public void setX(double x){
		entity.setPosition(x, entity.posY, entity.posZ);;
	}

	/**
	 * @return The entities y position
	 */
	public double getY(){
		return entity.posY;
	}

	/**
	 * @param y The entities y position
	 */
	public void setY(double y){
		entity.setPosition(entity.posX, y, entity.posZ);
	}

	/**
	 * @return The entities x position
	 */
	public double getZ(){
		return entity.posZ;
	}

	/**
	 * @param z The entities x position
	 */
	public void setZ(double z){
		entity.setPosition(entity.posX, entity.posY, z);;
	}

	/**
	 * @return The entities z motion
	 */
	public double getMotionX(){
		return entity.motionX;
	}

	/**
	 * @param x The entities x motion
	 */
	public void setMotionX(double x){
		entity.motionX = x;
		entity.velocityChanged = true;
	}

	/**
	 * @return The entities x motion
	 */
	public double getMotionY(){
		return entity.motionY;
	}

	/**
	 * @param y The entities y motion
	 */
	public void setMotionY(double y){
		entity.motionY = y;
		entity.velocityChanged = true;
	}

	/**
	 * @return The entities y motion
	 */
	public double getMotionZ(){
		return entity.motionZ;
	}

	/**
	 * @param z The entities z motion
	 */
	public void setMotionZ(double z){
		entity.motionZ = z;
		entity.velocityChanged = true;
	}

	public void setMotion(double x, double y, double z){
		this.setMotionX(x);
		this.setMotionY(y);
		this.setMotionZ(z);
	}

	public boolean isAirborne(){
		return entity.isAirBorne;
	}

	/**
	 * @return The block x position
	 */
	public int getBlockX(){
		return MathHelper.floor_double(entity.posX);
	}

	/**
	 * @return The block y position
	 */
	public int getBlockY(){
		return MathHelper.floor_double(entity.posY);
	}

	/**
	 * @return The block z position
	 */
	public int getBlockZ(){
		return MathHelper.floor_double(entity.posZ);
	}

	/**
	 * @param x The x position
	 * @param y The y position
	 * @param z The z position
	 */
	public void setPosition(double x, double y, double z){
		entity.setPosition(x, y, z);
	}

	public IPos getPos() {
		return new ScriptBlockPos(new BlockPos(entity.posX,entity.posY,entity.posZ));
	}

	public void setPos(IPos pos) {
		this.entity.setPosition((double)((float)pos.getX() + 0.5F), (double)pos.getY(), (double)((float)pos.getZ() + 0.5F));
	}

	/**
	 * @param range The search range for entities around this entity
	 * @return Array of entities within range
	 */
	public ScriptEntity[] getSurroundingEntities(int range){
		List<Entity> entities = entity.worldObj.getEntitiesWithinAABB(Entity.class, entity.boundingBox.expand(range, range, range));
		List<ScriptEntity> list = new ArrayList<ScriptEntity>();
		for(Entity living : entities){
			if(living != entity)
				list.add(ScriptController.Instance.getScriptForEntity(living));
		}
		return list.toArray(new ScriptEntity[list.size()]);
	}

	/**
	 * @param range The search range for entities around this entity
	 * @param type The EntityType you want to find
	 * @return Array of entities within range
	 */
	public ScriptEntity[] getSurroundingEntities(int range, int type){
		Class cls = Entity.class;
		if(type == EntityType.LIVING)
			cls = EntityLivingBase.class;
		else if(type == EntityType.PLAYER)
			cls = EntityPlayer.class;
		else if(type == EntityType.ANIMAL)
			cls = EntityAnimal.class;
		else if(type == EntityType.MONSTER)
			cls = EntityMob.class;
		else if(type == EntityType.NPC)
			cls = EntityNPCInterface.class;

		List<Entity> entities = entity.worldObj.getEntitiesWithinAABB(cls, entity.boundingBox.expand(range, range, range));
		List<ScriptEntity> list = new ArrayList<ScriptEntity>();
		for(Entity living : entities){
			if(living != entity)
				list.add(ScriptController.Instance.getScriptForEntity(living));
		}
		return list.toArray(new ScriptEntity[list.size()]);
	}


	/**
	 * @return Whether the entity is alive or not
	 */
	public boolean isAlive(){
		return entity.isEntityAlive();
	}

	/**
	 * @param key Get temp data for this key
	 * @return Returns the stored temp data
	 */
	public Object getTempData(String key){
		return tempData.get(key);
	}

	/**
	 * Tempdata gets cleared when the entity gets unloaded or the world restarts
	 * @param key The key for the data stored
	 * @param value The data stored
	 */
	public void setTempData(String key, Object value){
		tempData.put(key, value);
	}

	/**
	 * @param key The key thats going to be tested against the temp data
	 * @return Whether or not temp data containes the key
	 */
	public boolean hasTempData(String key){
		return tempData.containsKey(key);
	}

	/**
	 * @param key The key for the temp data to be removed
	 */
	public void removeTempData(String key){
		tempData.remove(key);
	}

	/**
	 * Remove all tempdata
	 */
	public void clearTempData(){
		tempData.clear();
	}

	/**
	 * @param key The key of the data to be returned
	 * @return Returns the stored data
	 */
	public Object getStoredData(String key){
		NBTTagCompound compound = getStoredCompound();
		if(!compound.hasKey(key))
			return null;
		NBTBase base = compound.getTag(key);
		if(base instanceof NBTPrimitive)
			return ((NBTPrimitive)base).func_150286_g();
		return ((NBTTagString)base).func_150285_a_();
	}

	/**
	 * Stored data persists through world restart. Unlike tempdata only Strings and Numbers can be saved
	 * @param key The key for the data stored
	 * @param value The data stored. This data can be either a Number or a String. Other data is not stored
	 */
	public void setStoredData(String key, Object value){
		NBTTagCompound compound = getStoredCompound();
		if(value instanceof Number){
			compound.setDouble(key, ((Number) value).doubleValue());
		}
		else if(value instanceof String)
			compound.setString(key, (String)value);
		saveStoredCompound(compound);
	}

	/**
	 * @param key The key of the data to be checked
	 * @return Returns whether or not the stored data contains the key
	 */
	public boolean hasStoredData(String key){
		return getStoredCompound().hasKey(key);
	}

	/**
	 * @param key The key of the data to be removed
	 */
	public void removeStoredData(String key){
		NBTTagCompound compound = getStoredCompound();
		compound.removeTag(key);
		saveStoredCompound(compound);
	}

	/**
	 * Remove all stored data
	 */
	public void clearStoredData(){
		entity.getEntityData().removeTag("CNPCStoredData");
	}

	private NBTTagCompound getStoredCompound(){
		NBTTagCompound compound = entity.getEntityData().getCompoundTag("CNPCStoredData");
		if(compound == null)
			entity.getEntityData().setTag("CNPCStoredData", compound = new NBTTagCompound());
		return compound;
	}

	private void saveStoredCompound(NBTTagCompound compound){
		entity.getEntityData().setTag("CNPCStoredData", compound);
	}

	/**
	 * @return The age of this entity in ticks
	 */
	public long getAge(){
		return entity.ticksExisted;
	}

	/**
	 * Despawns this entity. Removes it permanently
	 */
	public void despawn(){
		entity.isDead = true;
	}

	/**
	 * @return Return whether or not this entity is standing in water
	 */
	public boolean inWater(){
		return entity.isInsideOfMaterial(Material.water);
	}

	/**
	 * @return Return whether or not this entity is standing in lava
	 */
	public boolean inLava(){
		return entity.isInsideOfMaterial(Material.lava);
	}

	/**
	 * @return Return whether or not this entity is standing in fire
	 */
	public boolean inFire(){
		return entity.isInsideOfMaterial(Material.fire);
	}

	/**
	 * @return Return whether or not this entity is on fire
	 */
	public boolean isBurning(){
		return entity.isBurning();
	}

	/**
	 * @param ticks Amount of world ticks this entity will burn. 20 ticks equals 1 second
	 */
	public void setBurning(int ticks){
		entity.setFire(ticks);
	}

	/**
	 * Removes fire from this entity
	 */
	public void extinguish(){
		entity.extinguish();
	}

	/**
	 * @return Name as which it's registered in minecraft
	 */
	public String getTypeName(){
		return EntityList.getEntityString(entity);
	}

	/**
	 * @param item Item to be dropped
	 */
	public void dropItem(ScriptItemStack item){
		entity.entityDropItem(item.item, 0);
	}

	/**
	 * @return Return the rider
	 */
	public ScriptEntity getRider(){
		return ScriptController.Instance.getScriptForEntity(entity.riddenByEntity);
	}

	/**
	 * @param entity The entity to ride this entity
	 */
	public void setRider(ScriptEntity entity){
		if(entity != null){
			entity.entity.mountEntity(this.entity);;
		}
		else if(this.entity.riddenByEntity != null)
			this.entity.riddenByEntity.mountEntity(null);
	}

	/**
	 * @return Return the entity, this entity is riding
	 */
	public ScriptEntity getMount(){
		return ScriptController.Instance.getScriptForEntity(entity.ridingEntity);
	}

	/**
	 * @param entity The entity this entity will mount
	 */
	public void setMount(ScriptEntity entity){
		if(entity == null)
			this.entity.mountEntity(null);
		else
			this.entity.mountEntity(entity.entity);
	}

	/**
	 * @see noppes.npcs.scripted.constants.EntityType
	 * @return Returns the EntityType of this entity
	 */
	public int getType(){
		return EntityType.UNKNOWN;
	}

	/**
	 * @since 1.7.10c
	 * @param type @EntityType to check
	 * @return Returns whether the entity is type of the given @EntityType
	 */
	public boolean typeOf(int type){
		return type == EntityType.UNKNOWN;
	}

	/**
	 * @param rotation The rotation to be set (0-360)
	 */
	public void setRotation(float rotation){
		entity.rotationYaw = rotation;
	}

	/**
	 * @return Current rotation of the entity
	 */
	public float getRotation(){
		return entity.rotationYaw;
	}

	public void setPitch(float pitch){entity.rotationPitch = pitch;}

	public float getPitch(){return entity.rotationPitch;}

	/**
	 * @param power How strong the knockback is
	 * @param direction The direction in which he flies back (0-360). Usually based on getRotation()
	 */
	@Override
	public void knockback(int power, float direction){
		float v = direction * (float)Math.PI / 180.0F;
		entity.addVelocity(-MathHelper.sin(v) * (float)power, power, MathHelper.cos(v) * (float)power);
		entity.velocityChanged = true;
	}

	public void knockback(int xpower, int ypower, int zpower, float direction){
		float v = direction * (float)Math.PI / 180.0F;
		entity.addVelocity(-MathHelper.sin(v) * (float)xpower, ypower, MathHelper.cos(v) * (float)zpower);
		entity.velocityChanged = true;
	}

	public void setImmune(int ticks) {
		entity.hurtResistantTime = ticks;
	};

	public boolean hasCollided() {
		return entity.isCollided;
	}

	/**
	 * @since 1.7.10c
	 * @return Returns whether or not this entity is sneaking
	 */
	public boolean isSneaking(){
		return entity.isSneaking();
	}

	/**
	 * @since 1.7.10c
	 * @return Returns whether or not this entity is sprinting
	 */
	public boolean isSprinting(){
		return entity.isSprinting();
	}

	/**
	 * @since 1.7.10c
	 * Expert users only
	 * @return Returns minecrafts entity
	 */
	public T getMCEntity(){
		return entity;
	}

	public INbt getNbt() {
		return NpcAPI.Instance().getINbt(this.entity.getEntityData());
	}

	public INbt getAllNbt() {
		NBTTagCompound compound = new NBTTagCompound();
		this.entity.writeToNBT(compound);
		return NpcAPI.Instance().getINbt(compound);
	}

	public void setNbt(INbt nbt) {
		this.entity.readFromNBT(nbt.getMCNBT());
	}

	public void storeAsClone(int tab, String name) {
		NBTTagCompound compound = new NBTTagCompound();
		if (!this.entity.writeToNBTOptional(compound)) {
			throw new CustomNPCsException("Cannot store dead entities", new Object[0]);
		} else {
			ServerCloneController.Instance.addClone(compound, name, tab);
		}
	}

	public ScriptWorld getWorld() {
		return (ScriptWorld)NpcAPI.Instance().getIWorld((WorldServer) entity.worldObj);
	}
}