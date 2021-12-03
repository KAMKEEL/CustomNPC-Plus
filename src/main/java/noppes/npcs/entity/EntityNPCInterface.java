package noppes.npcs.entity;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.*;

import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ServerChatEvent;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.DataAI;
import noppes.npcs.DataAdvanced;
import noppes.npcs.DataDisplay;
import noppes.npcs.DataInventory;
import noppes.npcs.DataScript;
import noppes.npcs.DataStats;
import noppes.npcs.IChatMessages;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.NpcDamageSource;
import noppes.npcs.Server;
import noppes.npcs.VersionCompatibility;
import noppes.npcs.ai.*;
import noppes.npcs.ai.EntityAIMoveIndoors;
import noppes.npcs.ai.EntityAIPanic;
import noppes.npcs.ai.EntityAIWander;
import noppes.npcs.ai.EntityAIWatchClosest;
import noppes.npcs.ai.selector.NPCAttackSelector;
import noppes.npcs.ai.target.EntityAIClearTarget;
import noppes.npcs.ai.target.EntityAIClosestTarget;
import noppes.npcs.ai.target.EntityAIOwnerHurtByTarget;
import noppes.npcs.ai.target.EntityAIOwnerHurtTarget;
import noppes.npcs.client.EntityUtil;
import noppes.npcs.constants.EnumAnimation;
import noppes.npcs.constants.EnumJobType;
import noppes.npcs.constants.EnumMovingType;
import noppes.npcs.constants.EnumNavType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumPotionType;
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.constants.EnumStandingType;
import noppes.npcs.controllers.Dialog;
import noppes.npcs.controllers.DialogOption;
import noppes.npcs.controllers.Faction;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.Line;
import noppes.npcs.controllers.LinkedNpcController;
import noppes.npcs.controllers.LinkedNpcController.LinkedData;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.PlayerQuestData;
import noppes.npcs.controllers.QuestData;
import noppes.npcs.controllers.TransformData;
import noppes.npcs.entity.data.DataTimers;
import noppes.npcs.roles.JobBard;
import noppes.npcs.roles.JobFollower;
import noppes.npcs.roles.JobInterface;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.roles.RoleFollower;
import noppes.npcs.roles.RoleInterface;
import noppes.npcs.scripted.entity.ScriptNpc;
import noppes.npcs.scripted.entity.ScriptPlayer;
import noppes.npcs.scripted.event.ScriptEventAttack;
import noppes.npcs.scripted.event.ScriptEventDamaged;
import noppes.npcs.scripted.event.ScriptEventKilled;
import noppes.npcs.scripted.event.ScriptEventTarget;
import noppes.npcs.scripted.interfaces.ICustomNpc;
import noppes.npcs.util.GameProfileAlt;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;

public abstract class EntityNPCInterface extends EntityCreature implements IEntityAdditionalSpawnData, ICommandSender, IRangedAttackMob, IBossDisplayData{
	public ICustomNpc wrappedNPC;

	public static final GameProfileAlt chateventProfile = new GameProfileAlt();
	public static FakePlayer chateventPlayer;
	public static FakePlayer CommandPlayer;
	public DataDisplay display;
	public DataStats stats;
	public DataAI ai;
	public DataAdvanced advanced;
	public DataInventory inventory;
	public DataScript script;
	public TransformData transform;
	public DataTimers timers;
	
	public String linkedName = "";
	public long linkedLast = 0;
	public LinkedData linkedData;
	
	public float baseHeight = 1.8f;
	public float scaleX, scaleY, scaleZ;
	private boolean wasKilled = false;
	public RoleInterface roleInterface;
	public JobInterface jobInterface;
	public HashMap<Integer, DialogOption> dialogs;
	public boolean hasDied = false;
	public long killedtime = 0;
	public long totalTicksAlive = 0;
	private int taskCount = 1;
	public int lastInteract = 0;

	public Faction faction; //should only be used server side
	
	private EntityAIRangedAttack aiRange;
	private EntityAIBase aiResponse, aiLeap, aiSprint, aiAttackTarget;
		
	public List<EntityLivingBase> interactingEntities = new ArrayList<EntityLivingBase>();

	public ResourceLocation textureLocation = null;
	public ResourceLocation textureGlowLocation = null;
	public ResourceLocation textureCloakLocation = null;
	
	public EnumAnimation currentAnimation = EnumAnimation.NONE;
	
	public int npcVersion = VersionCompatibility.ModRev;
	public IChatMessages messages;

	public boolean updateClient = false;
	public boolean updateAI = false;

//	 Fly Change
//	public EntityMoveHelper moveHelper;
//	public PathNavigate navigator;

	public EntityNPCInterface(World world) {
		super(world);
		try{
			dialogs = new HashMap<Integer, DialogOption>();
			if(!CustomNpcs.DefaultInteractLine.isEmpty())
				advanced.interactLines.lines.put(0, new Line(CustomNpcs.DefaultInteractLine));
			
			experienceValue = 0;
			scaleX = scaleY = scaleZ = 0.9375f;
	        
			faction = getFaction();
			setFaction(faction.id);
			setSize(1, 1);
			this.updateTasks();

			if (!this.isRemote()) {
				this.wrappedNPC = new ScriptNpc(this);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}	
	
	@Override
    protected void applyEntityAttributes(){
        super.applyEntityAttributes();
        
		display = new DataDisplay(this);
		stats = new DataStats(this);
		ai = new DataAI(this);		
		advanced = new DataAdvanced(this);
		inventory = new DataInventory(this);
		transform = new TransformData(this);
		script = new DataScript(this);
		timers = new DataTimers(this);
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage);

		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(stats.maxHealth);
        this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(CustomNpcs.NpcNavRange);
		this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(this.getSpeed());
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(stats.getAttackStrength());
    }

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataWatcher.addObject(13, String.valueOf(""));//faction
		this.dataWatcher.addObject(14, Integer.valueOf(0)); // Animation
		this.dataWatcher.addObject(15, Integer.valueOf(0)); // isWalking
		this.dataWatcher.addObject(16, String.valueOf("")); // Role
	}
    protected boolean isAIEnabled(){
        return true;
    }
    
    @Override
    public boolean getLeashed(){
        return false; //Prevents npcs from being leashed
    }

    @Override
    public boolean isEntityAlive(){
    	return super.isEntityAlive() && !isKilled();
    }
    
	@Override
	public void onUpdate(){
		super.onUpdate();
		if(this.ticksExisted % 10 == 0)
			script.callScript(EnumScriptType.TICK);
		this.timers.update();
	}
	
	public void setWorld(World world){
		super.setWorld(world);
		script.setWorld(world);
	}

    @Override
    public boolean attackEntityAsMob(Entity par1Entity){
        float f = stats.getAttackStrength();
        
    	if (stats.attackSpeed < 10){
        	par1Entity.hurtResistantTime = 0;
        }
    	if(par1Entity instanceof EntityLivingBase){
	        ScriptEventAttack event = new ScriptEventAttack(f, (EntityLivingBase)par1Entity, false);
			if(script.callScript(EnumScriptType.ATTACK, "event", event, "target", par1Entity))
				return false;
			f = event.getDamage();
    	}
    	
        boolean var4 = par1Entity.attackEntityFrom(new NpcDamageSource("mob", this), f);

        if (var4){
        	if(getOwner() instanceof EntityPlayer)
        		NPCEntityHelper.setRecentlyHit((EntityLivingBase)par1Entity);
            if (stats.knockback > 0){
                par1Entity.addVelocity((double)(-MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F) * (float)stats.knockback * 0.5F), 0.1D, (double)(MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F) * (float)stats.knockback * 0.5F));
                this.motionX *= 0.6D;
                this.motionZ *= 0.6D;
            }
            if(advanced.role == EnumRoleType.Companion){
            	((RoleCompanion)roleInterface).attackedEntity(par1Entity);
            }
        }

        if (stats.potionType != EnumPotionType.None){
        	if (stats.potionType != EnumPotionType.Fire)
        		((EntityLivingBase)par1Entity).addPotionEffect(new PotionEffect(this.getPotionEffect(stats.potionType), stats.potionDuration * 20, stats.potionAmp));
        	else
        		par1Entity.setFire(stats.potionDuration);
        }
        return var4;
    }

    @Override
    public void onLivingUpdate(){
    	if(CustomNpcs.FreezeNPCs)
    		return;
    	totalTicksAlive++;
        this.updateArmSwingProgress();
        if(this.ticksExisted % 20 == 0)
			faction = getFaction();
		if(!worldObj.isRemote){
	    	if(!isKilled() && this.ticksExisted % 20 == 0){
	    		if(this.getHealth() < this.getMaxHealth()){
	    			if(stats.healthRegen > 0 && !isAttacking())
	    				heal(stats.healthRegen);
	    			if(stats.combatRegen > 0 && isAttacking())
	    				heal(stats.combatRegen);
	    		}
	    		if(faction.getsAttacked && !isAttacking()){
	    			List<EntityMob> list = this.worldObj.getEntitiesWithinAABB(EntityMob.class, this.boundingBox.expand(16, 16, 16));
	    			for(EntityMob mob : list){
	    				if(mob.getAttackTarget() == null && this.canSee(mob)){
		    	    		if(mob instanceof EntityZombie && !mob.getEntityData().hasKey("AttackNpcs")){
		    	    	        mob.tasks.addTask(2, new EntityAIAttackOnCollide(mob, EntityLivingBase.class, 1.0D, false));
		    	    	        mob.getEntityData().setBoolean("AttackNpcs", true);
		    	    		}
	    					mob.setAttackTarget(this);
	    				}
	    			}
	    		}
	    		if(linkedData != null && linkedData.time > linkedLast){
	    			LinkedNpcController.Instance.loadNpcData(this);
	    		}
				if(updateClient){
	    			NBTTagCompound compound = writeSpawnData();
	    			compound.setInteger("EntityId", getEntityId());
	    			Server.sendAssociatedData(this, EnumPacketClient.UPDATE_NPC, compound);
	    			updateClient = false;
	    		}
	    		if(updateAI){
	    			updateTasks();
	    			updateAI = false;
	    		}
	    	}
			if(getHealth() <= 0){
				clearActivePotions();
				setBoolFlag(true, 8);
			}
			setBoolFlag(this.getAttackTarget() != null, 4);
			setBoolFlag(!getNavigator().noPath(), 1);
			setBoolFlag(isInteracting(), 2);
			
			onCollide();
		}
		
		if(wasKilled != isKilled() && wasKilled){
			reset();
		}
		
		wasKilled = isKilled();
		
		if (this.worldObj.isDaytime() && !this.worldObj.isRemote && this.stats.burnInSun){
            float f = this.getBrightness(1.0F);

            if (f > 0.5F && this.rand.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && this.worldObj.canBlockSeeTheSky(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ))){
                this.setFire(8);
            }
        }
		
        super.onLivingUpdate();
        
        if (worldObj.isRemote){
        	 if(roleInterface != null){
     			roleInterface.clientUpdate();
     		}
        	
        	if(!display.cloakTexture.isEmpty())
        		cloakUpdate();
			if(currentAnimation.ordinal() != dataWatcher.getWatchableObjectInt(14)){
				currentAnimation = EnumAnimation.values()[dataWatcher.getWatchableObjectInt(14)];
				updateHitbox();
			}
			if(advanced.job == EnumJobType.Bard)
				((JobBard)jobInterface).onLivingUpdate();
        }
    }
    
	@Override
	public boolean interact(EntityPlayer player) {
		if(worldObj.isRemote)
			return false;
		ItemStack currentItem = player.inventory.getCurrentItem();
		if (currentItem != null) {
			Item item = currentItem.getItem();
			if (item == CustomItems.cloner || item == CustomItems.wand || item == CustomItems.mount || item == CustomItems.scripter) {
				setAttackTarget(null);
				setRevengeTarget(null);
				return true;
			}
			if (item == CustomItems.moving) {
				setAttackTarget(null);
				if(currentItem.stackTagCompound == null)
					currentItem.stackTagCompound = new NBTTagCompound();
				
				currentItem.stackTagCompound.setInteger("NPCID",this.getEntityId());
				player.addChatMessage(new ChatComponentTranslation("Registered " + this.getCommandSenderName() + " to your NPC Pather"));
				return true;
			}
		}
		
		if(script.callScript(EnumScriptType.INTERACT, "player", player) || isAttacking() || isKilled() || faction.isAggressiveToPlayer(player))
			return false;

		addInteract(player);

		Dialog dialog = getDialog(player);
		PlayerQuestData playerdata = PlayerDataController.instance.getPlayerData(player).questData;
		QuestData data = playerdata.getQuestCompletion(player, this);
		if (data != null){
			Server.sendData((EntityPlayerMP)player, EnumPacketClient.QUEST_COMPLETION, data.quest.writeToNBT(new NBTTagCompound()));
		}
		else if (dialog != null){
			NoppesUtilServer.openDialog(player, this, dialog);
		}
		else if(roleInterface != null)
			roleInterface.interact(player);
		else
			say(player, advanced.getInteractLine());
		
		return true;
	}
	
	public void addInteract(EntityLivingBase entity){
		if( !ai.stopAndInteract || isAttacking() || !entity.isEntityAlive())
			return;
		if((ticksExisted - lastInteract)  < 180)
			interactingEntities.clear();
		getNavigator().clearPathEntity();
		lastInteract = ticksExisted;
		if(!interactingEntities.contains(entity))
			interactingEntities.add(entity);
	}
	
	public boolean isInteracting(){
		if((ticksExisted - lastInteract) < 40 || isRemote() && getBoolFlag(2))
			return true;
		return ai.stopAndInteract && !interactingEntities.isEmpty() && (ticksExisted - lastInteract)  < 180;
	}

	private Dialog getDialog(EntityPlayer player) {
		for (DialogOption option : dialogs.values()) {
			if (option == null)
				continue;
			if (!option.hasDialog())
				continue;
			Dialog dialog = option.getDialog();
			if (dialog.availability.isAvailable(player)){
				return dialog;
			}
		}
		return null;
	}

	@Override
	public boolean attackEntityFrom(DamageSource damagesource, float i) {
        if (this.worldObj.isRemote || CustomNpcs.FreezeNPCs || damagesource.damageType.equals("inWall")){
            return false;
        }
        if(damagesource.damageType.equals("outOfWorld") && isKilled()){
        	reset();
        }
        i = stats.resistances.applyResistance(damagesource, i);
        if((float)this.hurtResistantTime > (float)this.maxHurtResistantTime / 2.0F && i <= this.lastDamage)
        	return false;
        
		Entity entity = damagesource.getEntity();

		EntityLivingBase attackingEntity = null;
		
		if (entity instanceof EntityLivingBase) 
			attackingEntity = (EntityLivingBase) entity;

		if ((entity instanceof EntityArrow) && ((EntityArrow) entity).shootingEntity instanceof EntityLivingBase)
			attackingEntity = (EntityLivingBase) ((EntityArrow) entity).shootingEntity;
		else if ((entity instanceof EntityThrowable))
			attackingEntity = ((EntityThrowable) entity).getThrower();
		
		if(attackingEntity != null && attackingEntity == getOwner())
			return false;
		else if (attackingEntity instanceof EntityNPCInterface){
			EntityNPCInterface npc = (EntityNPCInterface) attackingEntity;
			if(npc.faction.id == faction.id)
				return false;
			if(npc.getOwner() instanceof EntityPlayer)
				this.recentlyHit = 100;
		}
		else if (attackingEntity instanceof EntityPlayer && faction.isFriendlyToPlayer((EntityPlayer) attackingEntity))
			return false;
		ScriptEventDamaged result = new ScriptEventDamaged(i, attackingEntity, damagesource);
		if(script.callScript(EnumScriptType.DAMAGED, "event", result) || isKilled())
			return false;
		i = result.getDamage();
		
		if(isKilled())
			return false;
		
		if(attackingEntity == null)
			return super.attackEntityFrom(damagesource, i);
		
		try{
			if (isAttacking()){
				if(getAttackTarget() != null && attackingEntity != null && this.getDistanceSqToEntity(getAttackTarget()) > this.getDistanceSqToEntity(attackingEntity)){
					setAttackTarget(attackingEntity);
				}
				return super.attackEntityFrom(damagesource, i);
			}
			
			if (i > 0) {
				List<EntityNPCInterface> inRange = worldObj.getEntitiesWithinAABB(EntityNPCInterface.class, this.boundingBox.expand(32D, 16D, 32D));
				for (EntityNPCInterface npc : inRange) {
					if (npc.isKilled() || !npc.advanced.defendFaction || npc.faction.id != faction.id)
						continue;
					
					if (npc.canSee(this) || npc.ai.directLOS || npc.canSee(attackingEntity))
						npc.onAttack(attackingEntity);
				}
				setAttackTarget(attackingEntity);
			}
			return super.attackEntityFrom(damagesource, i);
		}
		finally{
			if(result.getClearTarget()){
				setAttackTarget(null);
				setRevengeTarget(null);
			}
		}
	}
	public void onAttack(EntityLivingBase entity) {
		if (entity == null || entity == this || isAttacking() || ai.onAttack == 3 || entity == getOwner())
			return;
		super.setAttackTarget(entity);
	}
	
	@Override
    public void setAttackTarget(EntityLivingBase entity){
    	if(entity instanceof EntityPlayer && ((EntityPlayer)entity).capabilities.disableDamage || entity != null && entity == getOwner())
    		return;
    	if(getAttackTarget() != entity && entity != null){
	    	ScriptEventTarget event = new ScriptEventTarget(entity);
			if(script.callScript(EnumScriptType.TARGET, "event", event))
				return;
			
			if(event.getTarget() == null)
				entity = null;
			else
				entity = event.getTarget().getMCEntity();
    	}
		if (entity != null && entity != this && ai.onAttack != 3 && !isAttacking() && !isRemote()){
			Line line = advanced.getAttackLine();
			if(line != null)
				saySurrounding(line.formatTarget(entity));
		}
		
		super.setAttackTarget(entity);
    }

	@Override
	public void attackEntityWithRangedAttack(EntityLivingBase entity, float f) {
        ItemStack proj = inventory.getProjectile();
        if(proj == null){
    		updateTasks();
        	return;
        }
        ScriptEventAttack event = new ScriptEventAttack(stats.pDamage, entity, true);
		if(script.callScript(EnumScriptType.ATTACK, "event", event, "target", entity))
			return;
		for(int i = 0; i < this.stats.shotCount; i++)
		{
			EntityProjectile projectile = shoot(entity, stats.accuracy, proj, f == 1);
			projectile.damage = event.getDamage();
		}
        this.playSound(this.stats.fireSound, 2.0F, 1.0f);

    }
	
	public EntityProjectile shoot(EntityLivingBase entity, int accuracy, ItemStack proj, boolean indirect){
		return shoot(entity.posX, entity.boundingBox.minY + (double)(entity.height / 2.0F), entity.posZ, accuracy, proj, indirect);
	}
	
	public EntityProjectile shoot(double x, double y, double z, int accuracy, ItemStack proj, boolean indirect){
        EntityProjectile projectile = new EntityProjectile(this.worldObj, this, proj.copy(), true);
        //TODO calculate height of the thrower
        double varX = x - this.posX;
		double varY = y - (this.posY + this.getEyeHeight());
		double varZ = z - this.posZ;
		float varF = projectile.hasGravity() ? MathHelper.sqrt_double(varX * varX + varZ * varZ) : 0.0F;
		float angle = projectile.getAngleForXYZ(varX, varY, varZ, varF, indirect);
		float acc = 20.0F - MathHelper.floor_float(accuracy / 5.0F);
        projectile.setThrowableHeading(varX, varY, varZ, angle, acc);
        worldObj.spawnEntityInWorld(projectile);
        return projectile;
	}
	
	private void clearTasks(EntityAITasks tasks){
        Iterator iterator = tasks.taskEntries.iterator();
        List<EntityAITaskEntry> list = new ArrayList(tasks.taskEntries);
        for (EntityAITaskEntry entityaitaskentry : list)
        {
            tasks.removeTask(entityaitaskentry.action);
        }
        tasks.taskEntries = new ArrayList<EntityAITaskEntry>();
	}
	private void updateTasks() {
		if (worldObj == null || worldObj.isRemote)
			return;
		aiLeap = aiAttackTarget = aiResponse = aiSprint = aiRange = null;

		clearTasks(tasks);
		clearTasks(targetTasks);
		IEntitySelector attackEntitySelector = new NPCAttackSelector(this);
		this.targetTasks.addTask(0, new EntityAIClearTarget(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAIClosestTarget(this, EntityLivingBase.class, 4, this.ai.directLOS, false, attackEntitySelector));
        this.targetTasks.addTask(3, new EntityAIOwnerHurtByTarget(this));
        this.targetTasks.addTask(4, new EntityAIOwnerHurtTarget(this));

        this.tasks.addTask(0, new EntityAIWaterNav(this));

		if(canFly()){
			//this.moveHelper = new FlyingMoveHelper(this);
			//this.navigator = new PathNavigateFlying(this, worldObj);
			this.getNavigator().setCanSwim(true);
			this.tasks.addTask(0, new EntityAISwimming(this));
		}
		else{
			//this.moveHelper = new EntityMoveHelper(this);
			//this.navigator = new PathNavigateGround(this, worldObj);
			this.tasks.addTask(0, new EntityAIWaterNav(this));
		}

		this.taskCount = 1;
		this.doorInteractType();
		this.seekShelter();
		this.setResponse();
		this.setMoveType();
		this.addRegularEntries();
	}
	
	private void removeTask(EntityAIBase task){
		if(task != null)
			tasks.removeTask(task);
	}
	
	/*
	 * Branch task function for setting how an NPC responds to a threat
	 */
	public void setResponse(){	
		removeTask(aiLeap);
		removeTask(aiResponse);
		removeTask(aiSprint);
		removeTask(aiAttackTarget);
		removeTask(aiRange);
		aiLeap = aiAttackTarget = aiResponse = aiSprint = aiRange = null;
        
        if (this.ai.onAttack == 1) 
        	this.tasks.addTask(this.taskCount++, aiResponse = new EntityAIPanic(this, 1.2F));
        
        else if (this.ai.onAttack == 2)  {
        	this.tasks.addTask(this.taskCount++, aiResponse = new EntityAIAvoidTarget(this));
        	this.setCanSprint();
        }
        
        else if (this.ai.onAttack == 0) {
        	this.setCanLeap();
        	this.setCanSprint();
        	if (this.inventory.getProjectile() == null || this.ai.useRangeMelee == 2)
        	{
	        	switch(this.ai.tacticalVariant)
	        	{
	        		case Dodge : this.tasks.addTask(this.taskCount++, aiResponse = new EntityAIZigZagTarget(this, 1.0D, this.ai.tacticalRadius)); break;
	        		case Surround : this.tasks.addTask(this.taskCount++, aiResponse = new EntityAIOrbitTarget(this, 1.0D, this.ai.tacticalRadius, true)); break;
	        		case HitNRun : this.tasks.addTask(this.taskCount++, aiResponse = new EntityAIAvoidTarget(this)); break;
	        		case Ambush : this.tasks.addTask(this.taskCount++, aiResponse = new EntityAIAmbushTarget(this, 1.2D, this.ai.tacticalRadius, false)); break;
	        		case Stalk : this.tasks.addTask(this.taskCount++, aiResponse = new EntityAIStalkTarget(this, this.ai.tacticalRadius)); break;
	        		default :
	        	}
        	}
        	else
        	{
        		switch(this.ai.tacticalVariant)
        		{
        			case Dodge : this.tasks.addTask(this.taskCount++, aiResponse = new EntityAIDodgeShoot(this)); break;
        			case Surround : this.tasks.addTask(this.taskCount++, aiResponse = new EntityAIOrbitTarget(this, 1.0D, stats.rangedRange, false)); break;
        			case HitNRun : this.tasks.addTask(this.taskCount++, aiResponse = new EntityAIAvoidTarget(this)); break;
        			case Ambush : this.tasks.addTask(this.taskCount++, aiResponse = new EntityAIAmbushTarget(this, 1.2D, this.ai.tacticalRadius, false)); break;
        			case Stalk : this.tasks.addTask(this.taskCount++, aiResponse = new EntityAIStalkTarget(this, this.ai.tacticalRadius)); break;
        			default :
        		}
        	}
        	this.tasks.addTask(this.taskCount, aiAttackTarget = new EntityAIAttackTarget(this));
        	((EntityAIAttackTarget)aiAttackTarget).navOverride(ai.tacticalVariant == EnumNavType.None);        	
        	
        	if(this.inventory.getProjectile() != null){
        		this.tasks.addTask(this.taskCount++, aiRange = new EntityAIRangedAttack(this));
        		aiRange.navOverride(ai.tacticalVariant == EnumNavType.None);
        	}
        }
        else if (this.ai.onAttack == 3) {
        	//do nothing
        }
    }

	/*
	 * Branch task function for setting if an NPC wanders or not
	 */
	public void setMoveType(){	
		if (ai.movingType == EnumMovingType.Wandering){
			this.tasks.addTask(this.taskCount++, new EntityAIWander(this));
		}
		if (ai.movingType == EnumMovingType.MovingPath){
			this.tasks.addTask(this.taskCount++, new EntityAIMovingPath(this));
		}
	}
	/*
	 * Branch task function for adjusting NPC door interactivity
	 */
	public void doorInteractType(){
		if(canFly()) //currently flying does not support opening doors
			return;
		EntityAIBase aiDoor = null;
		if (this.ai.doorInteract == 1)
		{
			this.tasks.addTask(this.taskCount++, aiDoor = new EntityAIOpenDoor(this, true));
		}
		else if (this.ai.doorInteract == 0)
		{
			this.tasks.addTask(this.taskCount++, aiDoor = new EntityAIBustDoor(this));
		}
		this.getNavigator().setBreakDoors(aiDoor != null);
	}
	
	/*
	 * Branch task function for finding shelter under the appropriate conditions
	 */
	public void seekShelter() {
		if (this.ai.findShelter == 0)
		{
			this.tasks.addTask(this.taskCount++, new EntityAIMoveIndoors(this));
		}
		else if (this.ai.findShelter == 1)
		{
			if(!canFly()) // doesnt work when flying
				this.tasks.addTask(this.taskCount++, new EntityAIRestrictSun(this));
			this.tasks.addTask(this.taskCount++, new EntityAIFindShade(this));
		}
	}
		
	/*
	 * Branch task function for leaping
	 */
	public void setCanLeap() {
		if (this.ai.canLeap)
			this.tasks.addTask(this.taskCount++, aiLeap = new EntityAILeapAtTarget(this, 0.4F));
	}
	
	/*
	 * Branch task function for sprinting
	 */
	public void setCanSprint() {
		if (this.ai.canSprint)
			this.tasks.addTask(this.taskCount++, aiSprint = new EntityAISprintToTarget(this));
	}
	
	/*
	 * Add immutable task entries.
	 */
	public void addRegularEntries() {
		this.tasks.addTask(this.taskCount++, new EntityAIReturn(this));
		this.tasks.addTask(this.taskCount++, new EntityAIFollow(this));
		if (this.ai.standingType != EnumStandingType.NoRotation && this.ai.standingType != EnumStandingType.HeadRotation)
			this.tasks.addTask(this.taskCount++, new EntityAIWatchClosest(this, EntityLivingBase.class, 5.0F));
		this.tasks.addTask(this.taskCount++, new EntityAILook(this));
		this.tasks.addTask(this.taskCount++, new EntityAIWorldLines(this));
		this.tasks.addTask(this.taskCount++, new EntityAIJob(this));
		this.tasks.addTask(this.taskCount++, new EntityAIRole(this));
		this.tasks.addTask(this.taskCount++, new EntityAIAnimation(this));
		if(transform.isValid())
			this.tasks.addTask(this.taskCount++, new EntityAITransform(this));
	}
	
	/*
	 * Function for getting proper move speeds. This way we don't have to modify them every time we use them.
	 */
	public float getSpeed() {
		return (float)ai.getWalkingSpeed() / 20.0F;
	}

    @Override
	public float getBlockPathWeight(int par1, int par2, int par3){
		float weight = this.worldObj.getLightBrightness(par1, par2, par3) - 0.5F;
    	Block block = worldObj.getBlock(par1, par2, par3);
    	if(block.isOpaqueCube())
    		weight += 10;
    	return weight;
    }
    
	/*
	 * Used for getting the applied potion effect from dataStats.
	 */
	private int getPotionEffect(EnumPotionType p) {
		switch(p)
		{
		case Poison : return Potion.poison.id;
		case Hunger : return Potion.hunger.id;
		case Weakness : return Potion.weakness.id;
		case Slowness : return Potion.moveSlowdown.id;
		case Nausea : return Potion.confusion.id;
		case Blindness : return Potion.blindness.id;
		case Wither : return Potion.wither.id;
		default : return 0;
		}
	}

    @Override
	protected int decreaseAirSupply(int par1)
    {
		if (!this.stats.canDrown)
			return par1;
        return super.decreaseAirSupply(par1);
    }

    @Override
	public EnumCreatureAttribute getCreatureAttribute()
    {
        return this.stats.creatureType;
    }
    
    /**
     * Returns the sound this mob makes while it's alive.
     */
	@Override
	protected String getLivingSound() {
		if (!this.isEntityAlive())
			return null;
		if (this.getAttackTarget() != null)
			return advanced.angrySound.isEmpty() ? null : advanced.angrySound;

		return advanced.idleSound.isEmpty() ? null : advanced.idleSound;
	}

	@Override
    public int getTalkInterval(){
        return 160;
    }
    /**
     * Returns the sound this mob makes when it is hurt.
     */
    @Override
    protected String getHurtSound(){
    	if(this.advanced.hurtSound.isEmpty())
    		return null;
        return this.advanced.hurtSound;
    }

    /**
     * Returns the sound this mob makes on death.
     */
    @Override
    protected String getDeathSound(){
    	if(this.advanced.deathSound.isEmpty())
    		return null;
        return this.advanced.deathSound;
    }
	
	@Override
    protected float getSoundPitch(){
		if(this.advanced.disablePitch)
			return 1;
    	return super.getSoundPitch();
    }
    
    /**
     * Plays step sound at given x, y, z for the entity
     */
    @Override
    protected void func_145780_a(int p_145780_1_, int p_145780_2_, int p_145780_3_, Block p_145780_4_)
    {
    	if (!this.advanced.stepSound.equals(""))
    	{
    		this.playSound(this.advanced.stepSound, 0.15F, 1.0F);
    	}
    	else
    	{
    		super.func_145780_a(p_145780_1_, p_145780_2_, p_145780_3_, p_145780_4_);
    	}
    }

    
    public EntityPlayerMP getFakePlayer(){
    	if(worldObj.isRemote)
    		return null;
		if(chateventPlayer == null)
			chateventPlayer = new FakePlayer((WorldServer)worldObj, chateventProfile);
		EntityUtil.Copy(this, chateventPlayer);
		chateventProfile.npc = this;
		chateventPlayer.refreshDisplayName();
		return chateventPlayer;
    }

	public void saySurrounding(Line line) {
		if (line == null || line.text == null)
			return;
		ServerChatEvent event = new ServerChatEvent(getFakePlayer(), line.text, new ChatComponentTranslation(line.text.replace("%", "%%")));
        if (MinecraftForge.EVENT_BUS.post(event) || event.component == null){
            return;
        }
		line.text = event.component.getUnformattedText().replace("%%", "%");
		List<EntityPlayer> inRange = worldObj.getEntitiesWithinAABB(
				EntityPlayer.class, this.boundingBox.expand(20D, 20D, 20D));
		for (EntityPlayer player : inRange)
			say(player, line);
	}

	public void say(EntityPlayer player, Line line) {
		if (line == null || !this.canSee(player) || line.text == null)
			return;		
		
		if(!line.sound.isEmpty()){
			Server.sendData((EntityPlayerMP)player, EnumPacketClient.PLAY_SOUND, line.sound, (float)posX, (float)posY, (float)posZ);
		}
		Server.sendData((EntityPlayerMP)player, EnumPacketClient.CHATBUBBLE, this.getEntityId(), line.text, !line.hideText);
	}
    public boolean getAlwaysRenderNameTagForRender(){
    	return true;
    }

	@Override
	public void addVelocity(double d, double d1, double d2) {
		if (isWalking() && !isKilled())
			super.addVelocity(d, d1, d2);
	}


	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		npcVersion = compound.getInteger("ModRev");
		VersionCompatibility.CheckNpcCompatibility(this, compound);
		
		display.readToNBT(compound);
		stats.readToNBT(compound);
		ai.readToNBT(compound);
		script.readFromNBT(compound);
		timers.readFromNBT(compound);
		advanced.readToNBT(compound);
        if (advanced.role != EnumRoleType.None && roleInterface != null) 
            roleInterface.readFromNBT(compound);
        if (advanced.job != EnumJobType.None && jobInterface != null)
            jobInterface.readFromNBT(compound);        
        
		inventory.readEntityFromNBT(compound);
		transform.readToNBT(compound);
		
		killedtime = compound.getLong("KilledTime");	
		totalTicksAlive = compound.getLong("TotalTicksAlive");
		
		linkedName = compound.getString("LinkedNpcName");
		if(!isRemote())
			LinkedNpcController.Instance.loadNpcData(this);
		
        this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(CustomNpcs.NpcNavRange);

		this.updateTasks();
	}



	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		display.writeToNBT(compound);
		stats.writeToNBT(compound);
		ai.writeToNBT(compound);
		script.writeToNBT(compound);
		timers.writeToNBT(compound);
		advanced.writeToNBT(compound);
        if (advanced.role != EnumRoleType.None && roleInterface != null)
            roleInterface.writeToNBT(compound);
        if (advanced.job != EnumJobType.None && jobInterface != null)
            jobInterface.writeToNBT(compound);
        
		inventory.writeEntityToNBT(compound);
		transform.writeToNBT(compound);

		compound.setLong("KilledTime", killedtime);
		compound.setLong("TotalTicksAlive", totalTicksAlive);		
		compound.setInteger("ModRev", npcVersion);
		compound.setString("LinkedNpcName", linkedName);
	}

	public void updateHitbox() {
		
		if(currentAnimation == EnumAnimation.LYING || currentAnimation == EnumAnimation.CRAWLING){
			width = 0.8f;
			height = 0.4f;
		}
		else if (isRiding()){
			width = 0.6f;
			height = baseHeight * 0.77f;
		}
		else{
			width = 0.6f;
			height = baseHeight;
		}
		width = (width / 5f) * display.modelSize;
		height = (height / 5f) * display.modelSize;
		
		this.setPosition(posX, posY, posZ);
	}

	@Override
	public void onDeathUpdate(){
		if(stats.spawnCycle == 3){
			super.onDeathUpdate();
			return;
		}
		
		++this.deathTime;
		if(worldObj.isRemote)
			return;
		if(!hasDied){
			setDead();
		}
		if (killedtime < System.currentTimeMillis()) {
			if (stats.spawnCycle == 0 || (this.worldObj.isDaytime() && stats.spawnCycle == 1) || (!this.worldObj.isDaytime() && stats.spawnCycle == 2)) {
				reset();
			}
		}
	}
	
	public void reset() {
		hasDied = false;
		isDead = false;
		wasKilled = false;
		setSprinting(false);
		setHealth(getMaxHealth());
		dataWatcher.updateObject(14, 0); // animation Normal
		dataWatcher.updateObject(15, 0); 
		this.setAttackTarget(null);
		this.setRevengeTarget(null);
		this.deathTime = 0;
		//fleeingTick = 0;
		if(ai.returnToStart && !hasOwner())
			setLocationAndAngles(getStartXPos(), getStartYPos(), getStartZPos(), rotationYaw, rotationPitch);
		killedtime = 0;
		extinguish();
		this.clearActivePotions();
		moveEntityWithHeading(0,0);
		distanceWalkedModified = 0;
		getNavigator().clearPathEntity();
		currentAnimation = EnumAnimation.NONE;
		updateHitbox();
		updateAI = true;
		ai.movingPos = 0;
		if(getOwner() != null){
			getOwner().setLastAttacker(null);
		}
		
		if(jobInterface != null)
			jobInterface.reset();
		
		script.callScript(EnumScriptType.INIT);
	}

    public void onCollide() {	
    	if(!isEntityAlive() || ticksExisted % 4 != 0)
    		return;
    	
        AxisAlignedBB axisalignedbb = null;

        if (this.ridingEntity != null && this.ridingEntity.isEntityAlive()){
            axisalignedbb = this.boundingBox.func_111270_a(this.ridingEntity.boundingBox).expand(1.0D, 0.0D, 1.0D);
        }
        else{
            axisalignedbb = this.boundingBox.expand(1.0D, 0.5D, 1.0D);
        }

        List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, axisalignedbb);
        if(list == null)
        	return;

        for (int i = 0; i < list.size(); ++i){
            Entity entity = (Entity)list.get(i);
            if (entity.isEntityAlive())
    			script.callScript(EnumScriptType.COLLIDE, "entity", entity);
        }
        
    }
 

    
	@Override
	public void setInPortal(){
		//prevent npcs from walking into portals
	}
	
	public double field_20066_r;
	public double field_20065_s;
	public double field_20064_t;
	public double field_20063_u;
	public double field_20062_v;
	public double field_20061_w;

	public void cloakUpdate() {
		field_20066_r = field_20063_u;
		field_20065_s = field_20062_v;
		field_20064_t = field_20061_w;
		double d = posX - field_20063_u;
		double d1 = posY - field_20062_v;
		double d2 = posZ - field_20061_w;
		double d3 = 10D;
		if (d > d3) {
			field_20066_r = field_20063_u = posX;
		}
		if (d2 > d3) {
			field_20064_t = field_20061_w = posZ;
		}
		if (d1 > d3) {
			field_20065_s = field_20062_v = posY;
		}
		if (d < -d3) {
			field_20066_r = field_20063_u = posX;
		}
		if (d2 < -d3) {
			field_20064_t = field_20061_w = posZ;
		}
		if (d1 < -d3) {
			field_20065_s = field_20062_v = posY;
		}
		field_20063_u += d * 0.25D;
		field_20061_w += d2 * 0.25D;
		field_20062_v += d1 * 0.25D;
	}

	@Override
	protected boolean canDespawn() {
		return stats.canDespawn;
	}

	@Override
	public ItemStack getHeldItem() {
		if (isAttacking())
			return inventory.getWeapon();
		else if(advanced.role == EnumRoleType.Companion)
			return ((RoleCompanion)roleInterface).getHeldItem();
		else if (jobInterface != null && jobInterface.overrideMainHand)
			return jobInterface.mainhand;
		else
			return inventory.getWeapon();
	}

    @Override
    public ItemStack getEquipmentInSlot(int slot){
    	if(slot == 0)
    		return inventory.weapons.get(0);
        return inventory.armorItemInSlot(4 - slot);
    }

    @Override
    public ItemStack func_130225_q(int slot){
        return inventory.armorItemInSlot(3 - slot);
    }

    @Override
    public void setCurrentItemOrArmor(int slot, ItemStack item){
    	if(slot == 0)
    		inventory.setWeapon(item);
    	else{
    		inventory.armor.put(4 - slot, item);
    	}
    }
    private static final ItemStack[] lastActive = new ItemStack[5];
    @Override
    public ItemStack[] getLastActiveItems(){
    	return lastActive;
    }
    
    @Override
    protected void dropEquipment(boolean p_82160_1_, int p_82160_2_){
    	
    }

	public ItemStack getOffHand() {
		if (isAttacking())
			return inventory.getOffHand();
		else if (jobInterface != null && jobInterface.overrideOffHand)
			return jobInterface.offhand;
		else
			return inventory.getOffHand();
	}
	
	@Override
	public void onDeath(DamageSource damagesource){
		setSprinting(false);
		getNavigator().clearPathEntity();
		extinguish();
		clearActivePotions();
		
		Entity entity = damagesource.getEntity();

		EntityLivingBase attackingEntity = null;
		
		if (entity instanceof EntityLivingBase) 
			attackingEntity = (EntityLivingBase) entity;

		if ((entity instanceof EntityArrow) && ((EntityArrow) entity).shootingEntity instanceof EntityLivingBase)
			attackingEntity = (EntityLivingBase) ((EntityArrow) entity).shootingEntity;
		else if ((entity instanceof EntityThrowable))
			attackingEntity = ((EntityThrowable) entity).getThrower();
				
		ScriptEventKilled result = new ScriptEventKilled(attackingEntity, damagesource);
		if(script.callScript(EnumScriptType.KILLED, "event", result))
			return;
		if(!isRemote()){
			if(this.recentlyHit > 0)
				inventory.dropStuff(entity, damagesource);
			Line line = advanced.getKilledLine();
			if(line != null)
				saySurrounding(line.formatTarget(attackingEntity));
		}
		super.onDeath(damagesource);
	}
	
	@Override
	public void setDead() {
		hasDied = true;
		if(worldObj.isRemote || stats.spawnCycle == 3){
			this.spawnExplosionParticle();
			delete();
		}
		else {
			if(this.riddenByEntity != null)
				this.riddenByEntity.mountEntity(null);
			if(this.ridingEntity != null)
				this.mountEntity(null);
			setHealth(-1);
			setSprinting(false);
			getNavigator().clearPathEntity();
			if(killedtime <= 0)
				killedtime = stats.respawnTime * 1000 + System.currentTimeMillis();
			
			if (advanced.role != EnumRoleType.None && roleInterface != null)
				roleInterface.killed();
			if (advanced.job != EnumJobType.None && jobInterface != null)
				jobInterface.killed();
		}
	}

	public void delete() {
		if (advanced.role != EnumRoleType.None && roleInterface != null)
			roleInterface.delete();
		if (advanced.job != EnumJobType.None && jobInterface != null)
			jobInterface.delete();
		super.setDead();
	}
	
	public float getStartXPos(){
		
		return getStartPos()[0] + ai.bodyOffsetX / 10;
	}
	
	public float getStartZPos(){
		return getStartPos()[2] + ai.bodyOffsetZ / 10;
	}
	
	public int[] getStartPos(){
		if(ai.startPos == null || ai.startPos.length != 3)
			ai.startPos = new int[] { 
				MathHelper.floor_double(posX),
				MathHelper.floor_double(posY),
				MathHelper.floor_double(posZ) };
		return ai.startPos;
	}

	public boolean isVeryNearAssignedPlace() {
		double xx = posX - getStartXPos();
		double zz = posZ - getStartZPos();
		if (xx < -0.2 || xx > 0.2)
			return false;
		if (zz < -0.2 || zz > 0.2)
			return false;
		return true;
	}

	@Override
	public IIcon getItemIcon(ItemStack par1ItemStack, int par2){
    	// Change Here
        if (par1ItemStack.getItem() instanceof ItemBow){
            return par1ItemStack.getItem().getIcon(par1ItemStack, par2);
        }
		EntityPlayer player = CustomNpcs.proxy.getPlayer();
		if(player == null)
			return super.getItemIcon(par1ItemStack, par2);
		return player.getItemIcon(par1ItemStack, par2);
    }

	public double getStartYPos() {
		int i = getStartPos()[0];
		int j = getStartPos()[1];
		int k = getStartPos()[2];
		double yy = 0;
		for (int ii = j; ii >= 0; ii--) {
			Block block = worldObj.getBlock(i, ii, k);
			if (block == null)
				continue;
			AxisAlignedBB bb = block.getCollisionBoundingBoxFromPool(worldObj, i, ii, k);
			if (bb == null)
				continue;
			yy = bb.maxY;
			break;
		}
		if (yy <= 0)
			setDead();
		yy += 0.5;
		return yy;
	}

	public void givePlayerItem(EntityPlayer player, ItemStack item) {
		if (worldObj.isRemote) {
			return;
		}
		item = item.copy();
		float f = 0.7F;
		double d = (double) (worldObj.rand.nextFloat() * f)
				+ (double) (1.0F - f);
		double d1 = (double) (worldObj.rand.nextFloat() * f)
				+ (double) (1.0F - f);
		double d2 = (double) (worldObj.rand.nextFloat() * f)
				+ (double) (1.0F - f);
		EntityItem entityitem = new EntityItem(worldObj, posX + d, posY + d1,
				posZ + d2, item);
		entityitem.delayBeforeCanPickup = 2;
		worldObj.spawnEntityInWorld(entityitem);

		int i = item.stackSize;

		if (player.inventory.addItemStackToInventory(item)) {
			worldObj.playSoundAtEntity(
					entityitem,
					"random.pop",
					0.2F,
					((rand.nextFloat() - rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
			player.onItemPickup(entityitem, i);

			if (item.stackSize <= 0) {
				entityitem.setDead();
			}
		}
	}
	
	@Override
	public boolean isPlayerSleeping() {
		return currentAnimation == EnumAnimation.LYING && !isAttacking();
	}

	@Override
	public boolean isRiding() {
		return currentAnimation == EnumAnimation.SITTING && !isAttacking() || ridingEntity != null;
	}

	public boolean isWalking() {
		return ai.movingType != EnumMovingType.Standing || isAttacking() || isFollower() || getBoolFlag(1);
	}
	
	public void setBoolFlag(boolean bo, int id){
		int i = dataWatcher.getWatchableObjectInt(15);
		if(bo && (i & id) == 0)
			dataWatcher.updateObject(15, i | id);
		if(!bo && (i & id) != 0)
			dataWatcher.updateObject(15, i - id);
	}
	
	/**
	 * 1: walking, 2:interacting, 4:attacking, 8:killed
	 */
	public boolean getBoolFlag(int id){
		return (dataWatcher.getWatchableObjectInt(15) & id) != 0;
	}

	@Override
	public boolean isSneaking() {
		return currentAnimation == EnumAnimation.SNEAKING;
	}
	@Override
    public void knockBack(Entity par1Entity, float par2, double par3, double par5)
    {
		if(stats.resistances.knockback >= 2)
			return;
        this.isAirBorne = true;
        float f1 = MathHelper.sqrt_double(par3 * par3 + par5 * par5);
        float f2 = 0.5F *  (2 - stats.resistances.knockback);
        this.motionX /= 2.0D;
        this.motionY /= 2.0D;
        this.motionZ /= 2.0D;
        this.motionX -= par3 / (double)f1 * (double)f2;
        this.motionY += 0.2 + f2 / 2;
        this.motionZ -= par5 / (double)f1 * (double)f2;

        if (this.motionY > 0.4000000059604645D)
        {
            this.motionY = 0.4000000059604645D;
        }
    }
    
	public Faction getFaction() {
		String[] split = dataWatcher.getWatchableObjectString(13).split(":");
		int faction = 0;
		if(worldObj == null || split.length <= 1 && worldObj.isRemote)
			return new Faction();
		if(split.length > 1)
			faction = Integer.parseInt(split[0]);
		if(worldObj.isRemote){
			Faction fac = new Faction();
			fac.id = faction;
			fac.color = Integer.parseInt(split[1]);
			fac.name = split[2];
			return fac;
		}
		else{
			Faction fac = FactionController.getInstance().getFaction(faction);
			if (fac == null) {
				faction = FactionController.getInstance().getFirstFactionId();
				fac = FactionController.getInstance().getFaction(faction);
			}
			return fac;
		}
	}
	public boolean isRemote(){
		return worldObj == null || worldObj.isRemote;
	}
	public void setFaction(int integer) {
		if(integer < 0|| isRemote())
			return;
		Faction faction = FactionController.getInstance().getFaction(integer);
		if(faction == null)
			return;
		String str = faction.id + ":" + faction.color + ":" + faction.name;
		if(str.length() > 64)
			str = str.substring(0, 64);
		dataWatcher.updateObject(13, str);
	}
	
	@Override
	public boolean isPotionApplicable(PotionEffect effect){
		if(stats.potionImmune)
			return false;
		if(getCreatureAttribute() == EnumCreatureAttribute.ARTHROPOD && effect.getPotionID() == Potion.poison.id)
			return false;
        return super.isPotionApplicable(effect);
    }

	public boolean isAttacking() {
		return getBoolFlag(4);
	}

	public boolean isKilled() {
		return getBoolFlag(8) || isDead;
	}

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		try {
			Server.writeNBT(buffer, writeSpawnData());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public NBTTagCompound writeSpawnData() {
		NBTTagCompound compound = new NBTTagCompound();
		display.writeToNBT(compound);
		compound.setDouble("MaxHealth", stats.maxHealth);
		compound.setTag("Armor", NBTTags.nbtItemStackList(inventory.getArmor()));
		compound.setTag("Weapons", NBTTags.nbtItemStackList(inventory.getWeapons()));
		compound.setInteger("Speed", ai.getWalkingSpeed());
		compound.setBoolean("DeadBody", stats.hideKilledBody);
		compound.setInteger("StandingState", ai.standingType.ordinal());
		compound.setInteger("MovingState", ai.movingType.ordinal());
		compound.setInteger("Orientation", ai.orientation);
		compound.setInteger("Role", advanced.role.ordinal());
		compound.setInteger("Job", advanced.job.ordinal());
		if(advanced.job == EnumJobType.Bard){
			NBTTagCompound bard = new NBTTagCompound();
			jobInterface.writeToNBT(bard);
			compound.setTag("Bard", bard);
		}
		if(advanced.job == EnumJobType.Puppet){
			NBTTagCompound bard = new NBTTagCompound();
			jobInterface.writeToNBT(bard);
			compound.setTag("Puppet", bard);
		}
		if(advanced.role == EnumRoleType.Companion){
			NBTTagCompound bard = new NBTTagCompound();
			roleInterface.writeToNBT(bard);
			compound.setTag("Companion", bard);
		}
		
		if(this instanceof EntityCustomNpc){
			compound.setTag("ModelData", ((EntityCustomNpc)this).modelData.writeToNBT());
		}
		return compound;
	}
	@Override
	public void readSpawnData(ByteBuf buf) {
		try {
			readSpawnData(Server.readNBT(buf));
		} catch (IOException e) {
		} 
	}
	public void readSpawnData(NBTTagCompound compound) {
		stats.maxHealth = compound.getDouble("MaxHealth");
		ai.setWalkingSpeed(compound.getInteger("Speed"));
		stats.hideKilledBody = compound.getBoolean("DeadBody");
		ai.standingType = EnumStandingType.values()[compound.getInteger("StandingState") % EnumStandingType.values().length];
		ai.movingType = EnumMovingType.values()[compound.getInteger("MovingState") % EnumMovingType.values().length];
		ai.orientation = compound.getInteger("Orientation");
		
		this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(stats.maxHealth);
		inventory.setArmor(NBTTags.getItemStackList(compound.getTagList("Armor", 10)));
		inventory.setWeapons(NBTTags.getItemStackList(compound.getTagList("Weapons", 10)));
		advanced.setRole(compound.getInteger("Role"));
		advanced.setJob(compound.getInteger("Job"));
		if(advanced.job == EnumJobType.Bard){
			NBTTagCompound bard = compound.getCompoundTag("Bard");
			jobInterface.readFromNBT(bard);
		}		
		if(advanced.job == EnumJobType.Puppet){
			NBTTagCompound puppet = compound.getCompoundTag("Puppet");
			jobInterface.readFromNBT(puppet);
		}	
		if(advanced.role == EnumRoleType.Companion){
			NBTTagCompound puppet = compound.getCompoundTag("Companion");
			roleInterface.readFromNBT(puppet);
		}		
		if(this instanceof EntityCustomNpc){
			((EntityCustomNpc)this).modelData.readFromNBT(compound.getCompoundTag("ModelData"));
		}
		display.readToNBT(compound);
	}

	public Entity func_174793_f() {
		if (this.worldObj.isRemote) {
			return this;
		} else {
			EntityUtil.Copy(this, CommandPlayer);
			CommandPlayer.setWorld(this.worldObj);
			CommandPlayer.setPosition(this.posX, this.posY, this.posZ);
			return CommandPlayer;
		}
	}

	@Override
	public String getCommandSenderName() {
		return display.name;
	}

	@Override
	public boolean canCommandSenderUseCommand(int var1, String var2) {
		if(CustomNpcs.NpcUseOpCommands)
	        return true;
        return var1 <= 2;
	}

	@Override
	public ChunkCoordinates getPlayerCoordinates() {
        return new ChunkCoordinates(MathHelper.floor_double(posX), MathHelper.floor_double(posY), MathHelper.floor_double(posZ));
	}
	
	@Override
	public boolean canAttackClass(Class par1Class)
    {
        return EntityBat.class != par1Class;
    }
	public void setImmuneToFire(boolean immuneToFire) {
		this.isImmuneToFire = immuneToFire;
		stats.immuneToFire = immuneToFire;
	}
	
	public void setAvoidWater(boolean avoidWater) {
		this.getNavigator().setAvoidsWater(avoidWater);
		ai.avoidsWater = avoidWater;
	}
	@Override
	protected void fall(float par1) {
		if (!this.stats.noFallDamage)
			super.fall(par1);
	}
	@Override
    public void setInWeb(){
    	if(!ai.ignoreCobweb)
    		super.setInWeb();
    }
	@Override
	public boolean canBeCollidedWith(){
		return !isKilled();
	}

	public boolean canFly(){
		return false;
	}

	public EntityAIRangedAttack getRangedTask(){
		return this.aiRange;
	}

	public String getRoleDataWatcher(){
		return dataWatcher.getWatchableObjectString(16);
	}
	
	public void setRoleDataWatcher(String s){
		dataWatcher.updateObject(16, s);
	}
	
	@Override
	public World getEntityWorld() {
		return worldObj;
	}

	@Override
    public boolean isInvisibleToPlayer(EntityPlayer player){
        return (scriptInvisibleToPlayer(player) || display.visible == 1) && (player.getHeldItem() == null || player.getHeldItem().getItem() != CustomItems.wand);
    }

	public boolean scriptInvisibleToPlayer(EntityPlayer player){
		return display.invisibleToList != null && display.invisibleToList.contains(player.getPersistentID());
	}

	@Override
	public boolean isInvisible(){
		return display.visible != 0;
	}
	
	@Override
	public void addChatMessage(IChatComponent var1) {}
	
	public void setCurrentAnimation(EnumAnimation animation) {
    	currentAnimation = animation;
    	dataWatcher.updateObject(14, animation.ordinal());
	}
	
	public boolean canSee(Entity entity){
		return this.getEntitySenses().canSee(entity);
	}
	
	public boolean isFollower() {
		return advanced.role == EnumRoleType.Follower && ((RoleFollower) roleInterface).isFollowing() || 
				advanced.role == EnumRoleType.Companion && ((RoleCompanion) roleInterface).isFollowing() || 
				advanced.job == EnumJobType.Follower && ((JobFollower)jobInterface).isFollowing();
	}
		
	public EntityLivingBase getOwner(){
		if(roleInterface instanceof RoleFollower)
			return ((RoleFollower)roleInterface).owner;
		
		if(roleInterface instanceof RoleCompanion)
			return ((RoleCompanion)roleInterface).owner;

		if(jobInterface instanceof JobFollower)
			return ((JobFollower)jobInterface).following;
		
		return null;
	}
	
	public boolean hasOwner(){
		return advanced.role == EnumRoleType.Follower && ((RoleFollower) roleInterface).hasOwner() || 
				advanced.role == EnumRoleType.Companion && ((RoleCompanion) roleInterface).hasOwner() || 
				advanced.job == EnumJobType.Follower && ((JobFollower)jobInterface).hasOwner();
	}
	
	public int followRange() {
		if(advanced.role == EnumRoleType.Follower && ((RoleFollower) roleInterface).isFollowing())
			return 36;
		if(advanced.role == EnumRoleType.Companion && ((RoleCompanion) roleInterface).isFollowing())
			return ((RoleCompanion) roleInterface).followRange();
		if(advanced.job == EnumJobType.Follower && ((JobFollower)jobInterface).isFollowing())
			return 16;
		
		return 225;
	}

	@Override
	public void setHomeArea(int x, int y, int z, int range){
		super.setHomeArea(x, y, z, range);
		ai.startPos = new int[]{x, y, z};
	}

	@Override
    protected float applyArmorCalculations(DamageSource source, float damage){
		if(advanced.role == EnumRoleType.Companion)
			damage = ((RoleCompanion)roleInterface).applyArmorCalculations(source, damage);
    	return damage;
    }

	@Override
    public boolean isOnSameTeam(EntityLivingBase entity){
		if(entity instanceof EntityPlayer && !isRemote() && getFaction().isFriendlyToPlayer((EntityPlayer)entity))
			return true;
        return super.isOnSameTeam(entity);
    }
	
	public void setDataWatcher(DataWatcher dataWatcher) {
		this.dataWatcher = dataWatcher;
	}

	@Override
    public void moveEntityWithHeading(float p_70612_1_, float p_70612_2_){
        double d0 = this.posX;
        double d1 = this.posY;
        double d2 = this.posZ;
    	super.moveEntityWithHeading(p_70612_1_, p_70612_2_);
    	if(advanced.role == EnumRoleType.Companion && !isRemote())
    		((RoleCompanion)roleInterface).addMovementStat(this.posX - d0, this.posY - d1, this.posZ - d2);
    }
	
	@Override
    public boolean allowLeashing(){
    	return false;
    }
	
	@Override
    public boolean shouldDismountInWater(Entity rider){
    	return false;
    }

	// Model Types: 0: Steve 64x32, 1: Steve 64x64, 2: Alex 64x64
	public int getModelType()
	{
		return this.display.modelType;
	}

	public void setModelType(int val)
	{
		this.display.modelType = val;
	}
}
