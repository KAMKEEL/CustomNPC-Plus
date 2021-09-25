package noppes.npcs.roles;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.entity.EntityNPCInterface;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.*;
import java.util.Map.Entry;

public class JobSpawner extends JobInterface{
	public NBTTagCompound compound6;
	public NBTTagCompound compound5;
	public NBTTagCompound compound4;
	public NBTTagCompound compound3;
	public NBTTagCompound compound2;
	public NBTTagCompound compound1;

	public String title1;
	public String title2;
	public String title3;
	public String title4;
	public String title5;
	public String title6;
	
	private int number = 0;
	
	public List<EntityLivingBase> spawned  = new ArrayList<EntityLivingBase>();
	
	private Map<String,Long> cooldown = new HashMap<String,Long>();
	
	private String id = RandomStringUtils.random(8, true, true);
	public boolean doesntDie = false;
	
	public int spawnType = 0;
	
	public int xOffset = 0;
	public int yOffset = 0;
	public int zOffset = 0;
	
	private EntityLivingBase target;
	
	public boolean despawnOnTargetLost = true;

	public JobSpawner(EntityNPCInterface npc) {
		super(npc);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		saveCompound(compound1, "SpawnerNBT1", compound);
		saveCompound(compound2, "SpawnerNBT2", compound);
		saveCompound(compound3, "SpawnerNBT3", compound);
		saveCompound(compound4, "SpawnerNBT4", compound);
		saveCompound(compound5, "SpawnerNBT5", compound);
		saveCompound(compound6, "SpawnerNBT6", compound);
		
		compound.setString("SpawnerId", id);
		compound.setBoolean("SpawnerDoesntDie", doesntDie);
		compound.setInteger("SpawnerType", spawnType);
		compound.setInteger("SpawnerXOffset", xOffset);
		compound.setInteger("SpawnerYOffset", yOffset);
		compound.setInteger("SpawnerZOffset", zOffset);
		
		compound.setBoolean("DespawnOnTargetLost", despawnOnTargetLost);
		return compound;
	}

    public NBTTagCompound getTitles() {
    	NBTTagCompound compound = new NBTTagCompound();
    	compound.setString("Title1", getTitle(compound1));
    	compound.setString("Title2", getTitle(compound2));
    	compound.setString("Title3", getTitle(compound3));
    	compound.setString("Title4", getTitle(compound4));
    	compound.setString("Title5", getTitle(compound5));
    	compound.setString("Title6", getTitle(compound6));
		return compound;
	}

    private String getTitle(NBTTagCompound compound) {
		if(compound != null && compound.hasKey("ClonedName"))
			return compound.getString("ClonedName");
		
		return "gui.selectnpc";
	}

	private void saveCompound(NBTTagCompound save, String name, NBTTagCompound compound) {
		if(save != null)
			compound.setTag(name, save);
	}
	
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		compound1 = compound.getCompoundTag("SpawnerNBT1");
		compound2 = compound.getCompoundTag("SpawnerNBT2");
		compound3 = compound.getCompoundTag("SpawnerNBT3");
		compound4 = compound.getCompoundTag("SpawnerNBT4");
		compound5 = compound.getCompoundTag("SpawnerNBT5");
		compound6 = compound.getCompoundTag("SpawnerNBT6");
		
		id = compound.getString("SpawnerId");
		doesntDie = compound.getBoolean("SpawnerDoesntDie");
		spawnType = compound.getInteger("SpawnerType");
		xOffset = compound.getInteger("SpawnerXOffset");
		yOffset = compound.getInteger("SpawnerYOffset");
		zOffset = compound.getInteger("SpawnerZOffset");
		
		despawnOnTargetLost = compound.getBoolean("DespawnOnTargetLost");
	}


	public void cleanCompound(NBTTagCompound compound) {
		compound.removeTag("SpawnerNBT1");
		compound.removeTag("SpawnerNBT2");
		compound.removeTag("SpawnerNBT3");
		compound.removeTag("SpawnerNBT4");
		compound.removeTag("SpawnerNBT5");
		compound.removeTag("SpawnerNBT6");
	}
    
    public void setJobCompound(int i, NBTTagCompound compound){
    	if(i == 1)
    		compound1 = compound;
    	if(i == 2)
    		compound2 = compound;
    	if(i == 3)
    		compound3 = compound;
    	if(i == 4)
    		compound4 = compound;
    	if(i == 5)
    		compound5 = compound;
    	if(i == 6)
    		compound6 = compound;
    }
    
	@Override
	public void aiUpdateTask() {
		if(spawned.isEmpty()){
			if(spawnType == 0){
				if(spawnEntity(number + 1) == null && !doesntDie)
					npc.setDead();
			}
			if(spawnType == 1){
				if(number >= 6 && !doesntDie)
					npc.setDead();
				else{
					spawnEntity(compound1);
					spawnEntity(compound2);
					spawnEntity(compound3);
					spawnEntity(compound4);
					spawnEntity(compound5);
					spawnEntity(compound6);
					number = 6;
				}
			}
			if(spawnType == 2){
				ArrayList<NBTTagCompound> list = new ArrayList<NBTTagCompound>();
				if(compound1 != null && compound1.hasKey("id"))
					list.add(compound1);
				if(compound2 != null && compound2.hasKey("id"))
					list.add(compound2);
				if(compound3 != null && compound3.hasKey("id"))
					list.add(compound3);
				if(compound4 != null && compound4.hasKey("id"))
					list.add(compound4);
				if(compound5 != null && compound5.hasKey("id"))
					list.add(compound5);
				if(compound6 != null && compound6.hasKey("id"))
					list.add(compound6);
				
				if(!list.isEmpty()){
					NBTTagCompound compound = list.get(npc.getRNG().nextInt(list.size()));
					spawnEntity(compound);
				}
				else if(!doesntDie)
					npc.setDead();
			}
		}
		else{
			checkSpawns();
		}
		
	}
	
	public void checkSpawns(){
		Iterator<EntityLivingBase> iterator = spawned.iterator();
		while(iterator.hasNext()){
			EntityLivingBase spawn = iterator.next();
			if(shouldDelete(spawn)){
				spawn.isDead = true;
				iterator.remove();
			}
			else{
				checkTarget(spawn);
			}
		}
	}
	
	public void checkTarget(EntityLivingBase entity){
		if(entity instanceof EntityLiving){
			EntityLiving liv = (EntityLiving) entity;
			if(liv.getAttackTarget() == null || npc.getRNG().nextInt(100) == 1)
				liv.setAttackTarget(target);
		}
		else if(entity.getAITarget() == null || npc.getRNG().nextInt(100) == 1){
			entity.setRevengeTarget(target);
		}
	}
	
	public boolean shouldDelete(EntityLivingBase entity){
		return npc.getDistanceToEntity(entity) > 60 || entity.isDead || entity.getHealth() <= 0 || 
				PixelmonHelper.Enabled  && hasPixelmon() && !PixelmonHelper.isBattling(entity) || despawnOnTargetLost && target == null;
	}
	
	private EntityLivingBase getTarget() {
		EntityLivingBase target = getTarget(npc);
		if(target != null)
			return target;

		for(EntityLivingBase entity : spawned){
			target = getTarget(entity);
			if(target != null)
				return target;
		}
		return null;
	}
	
	private EntityLivingBase getTarget(EntityLivingBase entity){
		if(entity instanceof EntityLiving){
			target = ((EntityLiving)entity).getAttackTarget();
			if(target != null && !target.isDead && target.getHealth() > 0)
				return target;
		}
		target = entity.getAITarget();
		if(target != null && !target.isDead && target.getHealth() > 0)
			return target;
		return null;
	}

	private boolean isEmpty(){
		if(compound1 != null && compound1.hasKey("id"))
			return false;
		if(compound2 != null && compound2.hasKey("id"))
			return false;
		if(compound3 != null && compound3.hasKey("id"))
			return false;
		if(compound4 != null && compound4.hasKey("id"))
			return false;
		if(compound5 != null && compound5.hasKey("id"))
			return false;
		if(compound6 != null && compound6.hasKey("id"))
			return false;
		
		return true;
	}
	
	private void setTarget(EntityLivingBase base, EntityLivingBase target) {
		if (PixelmonHelper.isTrainer(base) && target instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) target;
			if(!PixelmonHelper.canBattle(player, npc))
				return;
			cooldown.put(player.getCommandSenderName(), System.currentTimeMillis());
			
			Iterator<Entry<String,Long>> ita = cooldown.entrySet().iterator();
			
			while(ita.hasNext()){
				Entry<String, Long> entry = ita.next();
				if(!isOnCooldown(entry.getKey()))
					ita.remove();
			}
		} 
		else if (base instanceof EntityLiving)
			((EntityLiving) base).setAttackTarget(target);
		else
			base.setRevengeTarget(target);
	}

	@Override
	public boolean aiShouldExecute() {
		if(isEmpty() || npc.isKilled())
			return false;
		
		target = getTarget();
		if(npc.getRNG().nextInt(30) == 1){
			if(spawned.isEmpty())
				spawned = getNearbySpawned();
		}
		if(!spawned.isEmpty())
			checkSpawns();
		return target != null;
	}

	public boolean aiContinueExecute() {
		return aiShouldExecute();
	}
	public void resetTask() {
		reset();
	}

	public void aiStartExecuting() {
		number = 0;
		for(EntityLivingBase entity : spawned){
			int i = entity.getEntityData().getInteger("NpcSpawnerNr");
			if(i > number)
				number = i;
			setTarget(entity, npc.getAttackTarget());
		}
	}

	@Override
	public void reset() {
		number = 0;
		if(spawned.isEmpty())
			spawned = getNearbySpawned();

		target = null;
		checkSpawns();
	}
	@Override
	public void killed() {
		reset();
	}
	
	public EntityLivingBase spawnEntity(int i){
		NBTTagCompound compound = getCompound(i);
		if(compound == null){
			return null;
		}
		return spawnEntity(compound);
	}
	
	private EntityLivingBase spawnEntity(NBTTagCompound compound){
		if(compound == null || !compound.hasKey("id"))
			return null;
		double x = npc.posX + xOffset - 0.5 + npc.getRNG().nextFloat();
		double y = npc.posY + yOffset;
		double z = npc.posZ + zOffset - 0.5 + npc.getRNG().nextFloat();
		Entity entity = NoppesUtilServer.spawnClone(compound, MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z), npc.worldObj);
		if(entity == null || !(entity instanceof EntityLivingBase))
			return null;
		EntityLivingBase living = (EntityLivingBase) entity;
		living.getEntityData().setString("NpcSpawnerId", id);
		living.getEntityData().setInteger("NpcSpawnerNr", number);
		setTarget(living, npc.getAttackTarget());
		living.setPosition(x, y, z);
		if(living instanceof EntityNPCInterface){
			EntityNPCInterface snpc = (EntityNPCInterface) living;
			snpc.stats.spawnCycle = 3;
			snpc.ai.returnToStart = false;
		}
		spawned.add(living);
		return living;
	}

	private NBTTagCompound getCompound(int i) {
		if(i <= 1 && compound1 != null && compound1.hasKey("id")){
			number = 1;
			return compound1;
		}
		if(i <= 2 && compound2 != null && compound2.hasKey("id")){
			number = 2;
			return compound2;
		}
		if(i <= 3 && compound3 != null && compound3.hasKey("id")){
			number = 3;
			return compound3;
		}
		if(i <= 4 && compound4 != null && compound4.hasKey("id")){
			number = 4;
			return compound4;
		}
		if(i <= 5 && compound5 != null && compound5.hasKey("id")){
			number = 5;
			return compound5;
		}
		if(i <= 6 && compound6 != null && compound6.hasKey("id")){
			number = 6;
			return compound6;
		}
		return null;
	}

	
	private List<EntityLivingBase> getNearbySpawned(){
		List<EntityLivingBase> spawnList = new ArrayList<EntityLivingBase>();
		List<EntityLivingBase> list = npc.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, npc.boundingBox.expand(40, 40, 40));
		for(EntityLivingBase entity : list){
			if(entity.getEntityData().getString("NpcSpawnerId").equals(id) && !entity.isDead)
				spawnList.add(entity);
		}
		return spawnList;
	}

	public boolean isOnCooldown(String name) {
		if(!cooldown.containsKey(name))
			return false;
		
		long time = cooldown.get(name);
		return System.currentTimeMillis() < time + 1200000; //20 minutes cooldown
	}


	public boolean hasPixelmon() {
		return compound1 != null && compound1.getString("id").equals("pixelmontainer");
	}
}
