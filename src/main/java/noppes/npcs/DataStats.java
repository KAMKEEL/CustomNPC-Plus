package noppes.npcs;

import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.constants.EnumParticleType;
import noppes.npcs.constants.EnumPotionType;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ValueUtil;

public class DataStats {
	
	private int attackStrength = 5;
	public int attackSpeed = 20, attackRange = 2, knockback = 0;
	public int minDelay = 20, maxDelay = 40, rangedRange = 15, fireRate = 5, burstCount = 1, shotCount = 1, accuracy = 60;
	public int aggroRange = 16;
	
	public EnumPotionType potionType = EnumPotionType.None;
	public int potionDuration = 5; //20 = 1 second
	public int potionAmp = 0;

	public int maxHealth = 20;
	public int respawnTime = 20;
	public int spawnCycle = 0;
	public boolean hideKilledBody = false;
	public boolean canDespawn = false;
	
	public Resistances resistances = new Resistances();

	public boolean immuneToFire = false;
	public boolean potionImmune = false;
	public boolean canDrown = true;
	public boolean burnInSun = false;
	public boolean noFallDamage = false;
	public int healthRegen = 1;
	public int combatRegen = 0;
	
	public int pDamage = 4, pImpact = 0, pSize = 5, pSpeed = 10, pArea = 0, pDur = 5;
    public boolean pPhysics = true, pXlr8 = false, pGlows = false, pExplode = false;
    public boolean pRender3D = false, pSpin = false, pStick = false;
    public EnumPotionType pEffect = EnumPotionType.None;
    public EnumParticleType pTrail = EnumParticleType.None;
    public int pEffAmp = 0;
    public String fireSound = "random.bow";
	public boolean aimWhileShooting = false;
	
	public EnumCreatureAttribute creatureType = EnumCreatureAttribute.UNDEFINED;
	
	private EntityNPCInterface npc;
	public boolean attackInvisible = false;
	
	public DataStats(EntityNPCInterface npc){
		this.npc = npc;
	}
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{	
		compound.setTag("Resistances", resistances.writeToNBT());
		compound.setInteger("MaxHealth", maxHealth);
		compound.setInteger("AggroRange", aggroRange);
		compound.setBoolean("HideBodyWhenKilled", hideKilledBody);
		compound.setInteger("RespawnTime", respawnTime);
		compound.setInteger("SpawnCycle", spawnCycle);
		compound.setInteger("CreatureType", creatureType.ordinal());
		compound.setInteger("HealthRegen", healthRegen);
		compound.setInteger("CombatRegen", combatRegen);
		
		compound.setInteger("AttackStrenght", attackStrength);
		compound.setInteger("AttackRange", attackRange);
		compound.setInteger("AttackSpeed", attackSpeed);
		compound.setInteger("KnockBack", knockback);
		compound.setInteger("PotionEffect", potionType.ordinal());
		compound.setInteger("PotionDuration", potionDuration);
		compound.setInteger("PotionAmp", potionAmp);
		
		compound.setInteger("MaxFiringRange", rangedRange);
		compound.setInteger("FireRate", fireRate);
		compound.setInteger("minDelay", minDelay);
		compound.setInteger("maxDelay", maxDelay);
		compound.setInteger("BurstCount", burstCount);
		compound.setInteger("ShotCount", shotCount);
		compound.setInteger("Accuracy", accuracy);
		
		compound.setInteger("pDamage", pDamage);
		compound.setInteger("pImpact", pImpact);
		compound.setInteger("pSize", pSize);
		compound.setInteger("pSpeed", pSpeed);
		compound.setInteger("pArea", pArea);
		compound.setInteger("pDur", pDur);
		compound.setBoolean("pPhysics", pPhysics);
		compound.setBoolean("pXlr8", pXlr8);
		compound.setBoolean("pGlows", pGlows);
		compound.setBoolean("pExplode", pExplode);
		compound.setBoolean("pRender3D", pRender3D);
		compound.setBoolean("pSpin", pSpin);
		compound.setBoolean("pStick", pStick);
		compound.setInteger("pEffect", pEffect.ordinal());
		compound.setInteger("pTrail", pTrail.ordinal());
		compound.setInteger("pEffAmp", pEffAmp);
		compound.setString("FiringSound", fireSound);
		compound.setBoolean("AimWhileShooting", aimWhileShooting);

		compound.setBoolean("ImmuneToFire", immuneToFire);
		compound.setBoolean("PotionImmune", potionImmune);
		compound.setBoolean("CanDrown", canDrown);
		compound.setBoolean("BurnInSun", burnInSun);
		compound.setBoolean("NoFallDamage", noFallDamage);
		compound.setBoolean("CanDespawn", canDespawn);
		compound.setBoolean("AttackInvisible", attackInvisible);
		
		return compound;
	}

	public void readToNBT(NBTTagCompound compound)
	{
		resistances.readToNBT(compound.getCompoundTag("Resistances"));
		setMaxHealth(compound.getInteger("MaxHealth"));
		hideKilledBody = compound.getBoolean("HideBodyWhenKilled");
		aggroRange = compound.getInteger("AggroRange");
		respawnTime = compound.getInteger("RespawnTime");
		spawnCycle = compound.getInteger("SpawnCycle");
		creatureType = EnumCreatureAttribute.values()[compound.getInteger("CreatureType") % EnumPotionType.values().length];
		healthRegen = compound.getInteger("HealthRegen");
		combatRegen = compound.getInteger("CombatRegen");
		
		setAttackStrength(compound.getInteger("AttackStrenght"));
		attackSpeed = compound.getInteger("AttackSpeed");
		attackRange = compound.getInteger("AttackRange");
		knockback = compound.getInteger("KnockBack");
		potionType = EnumPotionType.values()[compound.getInteger("PotionEffect") % EnumPotionType.values().length];
		potionDuration = compound.getInteger("PotionDuration");
		potionAmp = compound.getInteger("PotionAmp");
		
		rangedRange = compound.getInteger("MaxFiringRange");
		fireRate = compound.getInteger("FireRate");
		minDelay = ValueUtil.CorrectInt(compound.getInteger("minDelay"), 1, 9999);
		maxDelay = ValueUtil.CorrectInt(compound.getInteger("maxDelay"), 1, 9999);
		burstCount = compound.getInteger("BurstCount");
		shotCount = ValueUtil.CorrectInt(compound.getInteger("ShotCount"), 1, 10);
		accuracy = compound.getInteger("Accuracy");	
		
		pDamage = compound.getInteger("pDamage");
		pImpact = compound.getInteger("pImpact");
		pSize = compound.getInteger("pSize");
		pSpeed = compound.getInteger("pSpeed");
		pArea = compound.getInteger("pArea");
		pDur = compound.getInteger("pDur");
		pPhysics = compound.getBoolean("pPhysics");
		pXlr8 = compound.getBoolean("pXlr8");
		pGlows = compound.getBoolean("pGlows");
		pExplode = compound.getBoolean("pExplode");
		pRender3D = compound.getBoolean("pRender3D");
		pSpin = compound.getBoolean("pSpin");
		pStick = compound.getBoolean("pStick");
		pEffect = EnumPotionType.values()[compound.getInteger("pEffect") % EnumPotionType.values().length];
		pTrail = EnumParticleType.values()[compound.getInteger("pTrail") % EnumParticleType.values().length];
		pEffAmp = compound.getInteger("pEffAmp");
		fireSound = compound.getString("FiringSound");
		aimWhileShooting = compound.getBoolean("AimWhileShooting");

		immuneToFire = compound.getBoolean("ImmuneToFire");	
		potionImmune = compound.getBoolean("PotionImmune");		
		canDrown = compound.getBoolean("CanDrown");
		burnInSun = compound.getBoolean("BurnInSun");
		noFallDamage = compound.getBoolean("NoFallDamage");
		canDespawn = compound.getBoolean("CanDespawn");
		attackInvisible = compound.getBoolean("AttackInvisible");
		
		npc.setImmuneToFire(immuneToFire);
	}

	public int getAttackStrength(){
		return attackStrength;
	}
	public void setAttackStrength(int strength){
		attackStrength = strength;
		npc.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(attackStrength);
	}
	
	public void setMaxHealth(int maxHealth) {
		this.maxHealth = maxHealth;
		npc.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(maxHealth);
	}
}
