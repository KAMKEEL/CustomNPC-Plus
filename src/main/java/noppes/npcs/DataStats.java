package noppes.npcs;

import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.*;
import noppes.npcs.constants.EnumParticleType;
import noppes.npcs.constants.EnumPotionType;
import noppes.npcs.controllers.data.MagicData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ValueUtil;

import java.util.HashSet;
import java.util.UUID;

public class DataStats {

	private float attackStrength = 5;
	public int attackSpeed = 20, swingWarmUp = 0, attackRange = 2, knockback = 0;
	public int minDelay = 20, maxDelay = 40, rangedRange = 15, fireRate = 5, burstCount = 1, shotCount = 1, accuracy = 60;
	public int aggroRange = 16;

	public EnumPotionType potionType = EnumPotionType.None;
	public int potionDuration = 5; //20 = 1 second
	public int potionAmp = 0;

	public double maxHealth = 20;
	public int respawnTime = 20;
	public int spawnCycle = 0;
	public boolean hideKilledBody = false;
	public boolean canDespawn = false;
	public boolean playerSetCanDespawn = false;

	public Resistances resistances = new Resistances();
    public MagicData magicData = new MagicData();

	public boolean ignoreCobweb = false;
	public boolean immuneToFire = false;
	public boolean potionImmune = false;
	public int drowningType = 1; // 0 - Never, 1 - Water,  2 - Air
	public boolean burnInSun = false;
	public boolean noFallDamage = false;
	public float healthRegen = 1;
	public float combatRegen = 0;

	public float pDamage = 4;
	public int pImpact = 0, pSize = 5, pSpeed = 10, pArea = 0, pDur = 5;
    public boolean pPhysics = true, pXlr8 = false, pGlows = false, pExplode = false;
    public boolean pRender3D = false, pSpin = false, pStick = false;
    public EnumPotionType pEffect = EnumPotionType.None;
    public EnumParticleType pTrail = EnumParticleType.None;
    public int pEffAmp = 0;
    public String fireSound = "random.bow";
	public boolean aimWhileShooting = false;
	public boolean projectilesKeepTerrain = false;

	public EnumCreatureAttribute creatureType = EnumCreatureAttribute.UNDEFINED;

	private EntityNPCInterface npc;
	public boolean attackInvisible = false;

    // 0 - Yes, 1 - No, 2 - Only NPCs, 3 - Only Players, 4 - NPCs and Players
    public int collidesWith = 1;

	public DataStats(EntityNPCInterface npc){
		this.npc = npc;
	}
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		compound.setTag("Resistances", resistances.writeToNBT());
		compound.setDouble("MaxHealth", maxHealth);
		compound.setInteger("AggroRange", aggroRange);
		compound.setBoolean("HideBodyWhenKilled", hideKilledBody);
		compound.setInteger("RespawnTime", respawnTime);
		compound.setInteger("SpawnCycle", spawnCycle);
		compound.setInteger("CreatureType", creatureType.ordinal());
		compound.setBoolean("IgnoreCobweb", ignoreCobweb);
		compound.setFloat("HealthRegen", healthRegen);
		compound.setFloat("CombatRegen", combatRegen);

		compound.setFloat("AttackStrenght", attackStrength);
		compound.setInteger("AttackRange", attackRange);
		compound.setInteger("AttackSpeed", attackSpeed);
		compound.setInteger("SwingWarmup", swingWarmUp);
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

		compound.setFloat("pDamage", pDamage);
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
		compound.setBoolean("ProjectilesKeepTerrain", projectilesKeepTerrain);

		compound.setBoolean("ImmuneToFire", immuneToFire);
		compound.setBoolean("PotionImmune", potionImmune);
		compound.setInteger("DrowningType", drowningType);
		compound.setBoolean("BurnInSun", burnInSun);
		compound.setBoolean("NoFallDamage", noFallDamage);
		compound.setBoolean("AttackInvisible", attackInvisible);
		compound.setBoolean("CanDespawn", canDespawn);
		compound.setBoolean("PlayerSetCanDespawn", playerSetCanDespawn);
        compound.setInteger("CollidesWith",collidesWith);

        magicData.writeToNBT(compound);

		return compound;
	}

	public void readToNBT(NBTTagCompound compound) {
        resistances.readToNBT(compound.getCompoundTag("Resistances"));
        setMaxHealth(compound.getDouble("MaxHealth"));
        hideKilledBody = compound.getBoolean("HideBodyWhenKilled");
        aggroRange = compound.getInteger("AggroRange");
        respawnTime = compound.getInteger("RespawnTime");
        spawnCycle = compound.getInteger("SpawnCycle");
        creatureType = EnumCreatureAttribute.values()[compound.getInteger("CreatureType") % EnumPotionType.values().length];
        ignoreCobweb = compound.getBoolean("IgnoreCobweb");
        healthRegen = compound.getFloat("HealthRegen");
        combatRegen = compound.getFloat("CombatRegen");

        setAttackStrength(compound.getFloat("AttackStrenght"));
        attackSpeed = compound.getInteger("AttackSpeed");
        swingWarmUp = ValueUtil.clamp(compound.getInteger("SwingWarmup"), 0, 1000);
        attackRange = compound.getInteger("AttackRange");
        knockback = compound.getInteger("KnockBack");
        potionType = EnumPotionType.values()[compound.getInteger("PotionEffect") % EnumPotionType.values().length];
        potionDuration = compound.getInteger("PotionDuration");
        potionAmp = compound.getInteger("PotionAmp");

        rangedRange = compound.getInteger("MaxFiringRange");
        fireRate = compound.getInteger("FireRate");
        minDelay = ValueUtil.clamp(compound.getInteger("minDelay"), 1, 9999);
        maxDelay = ValueUtil.clamp(compound.getInteger("maxDelay"), 1, 9999);
        burstCount = compound.getInteger("BurstCount");
        shotCount = ValueUtil.clamp(compound.getInteger("ShotCount"), 1, 10);
        accuracy = compound.getInteger("Accuracy");

        pDamage = compound.getFloat("pDamage");
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
        projectilesKeepTerrain = compound.getBoolean("ProjectilesKeepTerrain");

        immuneToFire = compound.getBoolean("ImmuneToFire");
        potionImmune = compound.getBoolean("PotionImmune");
        drowningType = compound.getInteger("DrowningType");

        burnInSun = compound.getBoolean("BurnInSun");
        noFallDamage = compound.getBoolean("NoFallDamage");
        attackInvisible = compound.getBoolean("AttackInvisible");
        canDespawn = compound.getBoolean("CanDespawn");
        playerSetCanDespawn = compound.getBoolean("PlayerSetCanDespawn");
        magicData.readToNBT(compound);

        npc.setImmuneToFire(immuneToFire);

        if (compound.hasKey("CollidesWith"))
            collidesWith = compound.getInteger("CollidesWith");
        else
            collidesWith = 1;
    }


	public float getAttackStrength(){
		return attackStrength;
	}
	public void setAttackStrength(double strength){
		attackStrength = (float)(Math.floor(strength));
		npc.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(attackStrength);
	}

	public void setMaxHealth(double maxHealth) {
		this.maxHealth = Math.floor(maxHealth);
		npc.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(maxHealth);
	}
}
