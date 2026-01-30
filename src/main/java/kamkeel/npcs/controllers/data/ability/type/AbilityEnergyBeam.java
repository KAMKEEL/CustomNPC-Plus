package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AnchorPoint;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.data.*;
import kamkeel.npcs.controllers.data.telegraph.Telegraph;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import kamkeel.npcs.entity.EntityAbilityBeam;
import kamkeel.npcs.util.AnchorPointHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.advanced.ability.SubGuiAbilityEnergyBeam;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.api.ability.type.IAbilityEnergyBeam;
import noppes.npcs.entity.EntityNPCInterface;

/**
 * Energy Beam ability: A homing head with a trailing path.
 * The beam stays attached to the origin and curves as it homes in on target.
 * Cannot cross over itself (self-intersection prevention).
 */
public class AbilityEnergyBeam extends Ability implements IAbilityEnergyBeam {

    // Shape properties (standalone)
    private float beamWidth = 0.4f;
    private float headSize = 0.6f;

    // Data classes
    private EnergyColorData colorData = new EnergyColorData(0xFFFFFF, 0x00AAFF, true, 0.4f, 0.5f, 6.0f);
    private EnergyCombatData combatData = new EnergyCombatData(10.0f, 1.5f, 0.2f, false, 4.0f, 0.5f);
    private EnergyHomingData homingData = new EnergyHomingData(0.4f, true, 0.1f, 15.0f);
    private EnergyLightningData lightningData = new EnergyLightningData();
    private EnergyLifespanData lifespanData = new EnergyLifespanData(25.0f, 200);
    private EnergyAnchorData anchorData = new EnergyAnchorData(AnchorPoint.RIGHT_HAND);

    // Transient state for beam entity (used during windup charging)
    private transient EntityAbilityBeam beamEntity = null;

    public AbilityEnergyBeam() {
        this.typeId = "ability.cnpc.beam";
        this.name = "Beam";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 20.0f;
        this.minRange = 5.0f;
        this.cooldownTicks = 0;
        this.windUpTicks = 40;
        this.lockMovement = LockMovementType.WINDUP_AND_ACTIVE;
        this.telegraphType = TelegraphType.CIRCLE;
        this.showTelegraph = true;
        // Default built-in animations
        this.windUpAnimationName = "Ability_Beam_Windup";
        this.activeAnimationName = "Ability_Beam_Active";
    }

    @Override
    public boolean hasTypeSettings() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiAbilityConfig createConfigGui(IAbilityConfigCallback callback) {
        return new SubGuiAbilityEnergyBeam(this, callback);
    }

    @Override
    public boolean isTargetingModeLocked() {
        return true;
    }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[]{TargetingMode.AGGRO_TARGET};
    }

    @Override
    public void onExecute(EntityLivingBase caster, EntityLivingBase target, World world) {
        if (world.isRemote) {
            signalCompletion();
            return;
        }

        // Start firing the beam that was spawned during windup
        if (beamEntity != null && !beamEntity.isDead) {
            beamEntity.startFiring(target);
        }

        // Ability stays active until entity dies (prevents firing another while projectile is alive)
        // Movement locking is handled separately by the base class
    }

    @Override
    public void onWindUpTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        if (world.isRemote) return;

        // Spawn beam in charging mode on first tick of windup
        if (tick == 1) {
            float offsetDist = 1.0f;

            // Create beam in charging mode - follows caster based on anchor point during windup
            Vec3 spawnPos = AnchorPointHelper.calculateAnchorPosition(caster, anchorData, offsetDist);
            beamEntity = new EntityAbilityBeam(
                world, caster, target,
                spawnPos.xCoord, spawnPos.yCoord, spawnPos.zCoord,
                beamWidth, headSize,
                colorData, combatData, homingData, lightningData, lifespanData,
                lockMovement.locksActive());
            beamEntity.setupCharging(anchorData, windUpTicks, offsetDist);

            beamEntity.setEffects(this.effects);
            world.spawnEntityInWorld(beamEntity);
        }
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        // Signal completion when entity dies
        if (beamEntity == null || beamEntity.isDead) {
            beamEntity = null;
            signalCompletion();
        }
    }

    @Override
    public void onComplete(EntityLivingBase caster, EntityLivingBase target) {
    }

    @Override
    public void cleanup() {
        // Despawn beam entity if still alive
        if (beamEntity != null && !beamEntity.isDead) {
            beamEntity.setDead();
        }
        beamEntity = null;
    }

    @Override
    public TelegraphInstance createTelegraph(EntityLivingBase caster, EntityLivingBase target) {
        if (!showTelegraph || telegraphType == TelegraphType.NONE || target == null) {
            return null;
        }

        // Create circle telegraph at target position
        Telegraph telegraph = Telegraph.circle(headSize * 2.0f);
        telegraph.setDurationTicks(windUpTicks);
        telegraph.setColor(windUpColor);
        telegraph.setWarningColor(activeColor);
        telegraph.setWarningStartTick(Math.max(5, windUpTicks / 4));
        telegraph.setHeightOffset(telegraphHeightOffset);

        // Position at target and follow target during windup
        TelegraphInstance instance = new TelegraphInstance(telegraph, target.posX, target.posY, target.posZ, caster.rotationYaw);
        instance.setCasterEntityId(caster.getEntityId());
        instance.setEntityIdToFollow(target.getEntityId());

        return instance;
    }

    @Override
    public float getTelegraphRadius() {
        return headSize * 2.0f;
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("beamWidth", beamWidth);
        nbt.setFloat("headSize", headSize);
        anchorData.writeNBT(nbt);
        colorData.writeNBT(nbt);
        combatData.writeNBT(nbt);
        homingData.writeNBT(nbt);
        lightningData.writeNBT(nbt);
        lifespanData.writeNBT(nbt);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.beamWidth = nbt.hasKey("beamWidth") ? nbt.getFloat("beamWidth") : 0.4f;
        this.headSize = nbt.hasKey("headSize") ? nbt.getFloat("headSize") : 0.6f;
        anchorData.readNBT(nbt);
        colorData.readNBT(nbt);
        combatData.readNBT(nbt);
        homingData.readNBT(nbt);
        lightningData.readNBT(nbt);
        lifespanData.readNBT(nbt);
    }

    // Getters & Setters - Standalone fields
    public float getBeamWidth() { return beamWidth; }
    public void setBeamWidth(float beamWidth) { this.beamWidth = beamWidth; }
    public float getHeadSize() { return headSize; }
    public void setHeadSize(float headSize) { this.headSize = headSize; }

    // Getters & Setters - Color data
    public int getInnerColor() { return colorData.innerColor; }
    public void setInnerColor(int innerColor) { this.colorData.innerColor = innerColor; }
    public int getOuterColor() { return colorData.outerColor; }
    public void setOuterColor(int outerColor) { this.colorData.outerColor = outerColor; }
    public boolean isOuterColorEnabled() { return colorData.outerColorEnabled; }
    public void setOuterColorEnabled(boolean outerColorEnabled) { this.colorData.outerColorEnabled = outerColorEnabled; }
    public float getOuterColorWidth() { return colorData.outerColorWidth; }
    public void setOuterColorWidth(float outerColorWidth) { this.colorData.outerColorWidth = outerColorWidth; }
    public float getOuterColorAlpha() { return colorData.outerColorAlpha; }
    public void setOuterColorAlpha(float outerColorAlpha) { this.colorData.outerColorAlpha = outerColorAlpha; }
    public float getRotationSpeed() { return colorData.rotationSpeed; }
    public void setRotationSpeed(float rotationSpeed) { this.colorData.rotationSpeed = rotationSpeed; }

    // Getters & Setters - Combat data
    public float getDamage() { return combatData.damage; }
    public void setDamage(float damage) { this.combatData.damage = damage; }
    public float getKnockback() { return combatData.knockback; }
    public void setKnockback(float knockback) { this.combatData.knockback = knockback; }
    public float getKnockbackUp() { return combatData.knockbackUp; }
    public void setKnockbackUp(float knockbackUp) { this.combatData.knockbackUp = knockbackUp; }
    public boolean isExplosive() { return combatData.explosive; }
    public void setExplosive(boolean explosive) { this.combatData.explosive = explosive; }
    public float getExplosionRadius() { return combatData.explosionRadius; }
    public void setExplosionRadius(float explosionRadius) { this.combatData.explosionRadius = explosionRadius; }
    public float getExplosionDamageFalloff() { return combatData.explosionDamageFalloff; }
    public void setExplosionDamageFalloff(float explosionDamageFalloff) { this.combatData.explosionDamageFalloff = explosionDamageFalloff; }

    // Getters & Setters - Homing data
    public float getSpeed() { return homingData.speed; }
    public void setSpeed(float speed) { this.homingData.speed = speed; }
    public boolean isHoming() { return homingData.homing; }
    public void setHoming(boolean homing) { this.homingData.homing = homing; }
    public float getHomingStrength() { return homingData.homingStrength; }
    public void setHomingStrength(float homingStrength) { this.homingData.homingStrength = homingStrength; }
    public float getHomingRange() { return homingData.homingRange; }
    public void setHomingRange(float homingRange) { this.homingData.homingRange = homingRange; }

    // Getters & Setters - Lightning data
    public boolean hasLightningEffect() { return lightningData.lightningEffect; }
    public void setLightningEffect(boolean lightningEffect) { this.lightningData.lightningEffect = lightningEffect; }
    public float getLightningDensity() { return lightningData.lightningDensity; }
    public void setLightningDensity(float lightningDensity) { this.lightningData.lightningDensity = lightningDensity; }
    public float getLightningRadius() { return lightningData.lightningRadius; }
    public void setLightningRadius(float lightningRadius) { this.lightningData.lightningRadius = lightningRadius; }

    // Getters & Setters - Lifespan data
    public float getMaxDistance() { return lifespanData.maxDistance; }
    public void setMaxDistance(float maxDistance) { this.lifespanData.maxDistance = maxDistance; }
    public int getMaxLifetime() { return lifespanData.maxLifetime; }
    public void setMaxLifetime(int maxLifetime) { this.lifespanData.maxLifetime = maxLifetime; }

    // Getters & Setters - Anchor point
    public AnchorPoint getAnchorPointEnum() { return anchorData.anchorPoint; }
    public float getAnchorOffsetX() { return anchorData.anchorOffsetX; }
    public float getAnchorOffsetY() { return anchorData.anchorOffsetY; }
    public float getAnchorOffsetZ() { return anchorData.anchorOffsetZ; }
    public void setAnchorPointEnum(AnchorPoint anchorPoint) { this.anchorData.anchorPoint = anchorPoint; }
    public void setAnchorOffsetX(float x) { this.anchorData.anchorOffsetX = x; }
    public void setAnchorOffsetY(float y) { this.anchorData.anchorOffsetY = y; }
    public void setAnchorOffsetZ(float z) { this.anchorData.anchorOffsetZ = z; }

    @Override
    public int getAnchorPoint() { return anchorData.anchorPoint.ordinal(); }

    @Override
    public void setAnchorPoint(int point) { this.anchorData.anchorPoint = AnchorPoint.fromOrdinal(point); }

    @Override
    @SideOnly(Side.CLIENT)
    public Entity createPreviewEntity(EntityNPCInterface npc) {
        if (npc == null || npc.worldObj == null) return null;

        EntityAbilityBeam beam = new EntityAbilityBeam(npc.worldObj);
        beam.setupPreview(npc, beamWidth, headSize, colorData, lightningData, anchorData, windUpTicks, 1.0f);
        return beam;
    }

    @Override
    public int getPreviewActiveDuration() {
        return lifespanData.maxLifetime > 0 ? Math.min(lifespanData.maxLifetime, 100) : 100;
    }
}
