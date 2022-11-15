package noppes.npcs.scripted.roles;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobSpawner;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.constants.JobType;
import noppes.npcs.api.INbt;
import noppes.npcs.api.IPos;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.jobs.IJobSpawner;

public class ScriptJobSpawner extends ScriptJobInterface implements IJobSpawner {
	private final JobSpawner job;
	public ScriptJobSpawner(EntityNPCInterface npc){
		super(npc);
		this.job = (JobSpawner) npc.jobInterface;
	}
		
	@Override
	public int getType(){
		return JobType.SPAWNER;
	}
	
	/**
	 * Npc needs to be attacking something or be set to Despawn Spawns On Target Lost: No, otherwise it will despawn right away
	 * @param number The entity going to be spawned (1-6)
	 * @return Returns spawned entity
	 */
	public IEntityLivingBase<?> spawnEntity(int number){
		EntityLivingBase base = job.spawnEntity(number);
		if(base == null)
			return null;
		
		return (IEntityLivingBase<?>) NpcAPI.Instance().getIEntity(base);
	}

	public IEntityLivingBase<?> getEntity(int number, int x, int y, int z, IWorld world) {
		NBTTagCompound compound = this.job.getCompound(number);
		if(compound == null || !compound.hasKey("id"))
			return null;
		Entity entity = NoppesUtilServer.getEntityFromNBT(compound,x,y,z,world.getMCWorld());
		if (!(entity instanceof EntityLivingBase)) {
			return null;
		}
		return (IEntityLivingBase<?>) NpcAPI.Instance().getIEntity(entity);
	}

	public IEntityLivingBase<?> getEntity(int number, IPos pos, IWorld world) {
		return this.getEntity(number,pos.getX(),pos.getY(),pos.getZ(),world);
	}

	public void setEntity(int number, IEntityLivingBase<?> entityLivingBase){
		INbt nbt = entityLivingBase.getNbtOptional();
		if (nbt == null)
			return;
		this.job.setJobCompound(number,nbt.getMCNBT());
	}

		/**
         * Removes all spawned entities
         */
	public void removeAllSpawned(){
		for(EntityLivingBase entity : job.spawned){
			entity.isDead = true;
		}
		job.spawned = new ArrayList<>();
	}

	public IEntityLivingBase<?>[] getNearbySpawned() {
		List<EntityLivingBase> nearbySpawned = this.job.getNearbySpawned();
		List<IEntityLivingBase<?>> list = new ArrayList<>();
		for (EntityLivingBase entity : nearbySpawned) {
			list.add((IEntityLivingBase<?>) NpcAPI.Instance().getIEntity(entity));
		}
		return list.toArray(new IEntityLivingBase[0]);
	}

	public boolean hasPixelmon() {
		return this.job.hasPixelmon();
	}

	public boolean isEmpty() {
		return this.job.isEmpty();
	}

	public boolean isOnCooldown(IPlayer<EntityPlayerMP> player) {
		return this.job.isOnCooldown(player.getName());
	}
}
