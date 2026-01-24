package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.advanced.ability.SubGuiAbilityCutter;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Cutter ability: Sweeping fan attack in an arc.
 * Deals damage to all entities in a cone/arc in front of the caster.
 * Can sweep outward over time or hit instantly.
 */
public class AbilityCutter extends Ability {

    public enum SweepMode {
        SWIPE,
        SPIN
    }

    private float arcAngle = 90.0f;
    private float range = 6.0f;
    private float damage = 7.0f;
    private float knockback = 1.5f;

    private SweepMode sweepMode = SweepMode.SWIPE;
    private float sweepSpeed = 6.0f;

    private int stunDuration = 0;
    private int poisonDurationSeconds = 0;
    private int poisonLevel = 0;
    private boolean piercing = true;
    private float innerRadius = 0.0f;

    // Runtime state
    private transient Set<Integer> hitEntities = new HashSet<>();
    private transient float currentRotation = 0.0f;

    public AbilityCutter() {
        this.typeId = "ability.cnpc.cutter";
        this.name = "Cutter";
        this.targetingMode = TargetingMode.AOE_SELF;
        this.maxRange = 8.0f;
        this.lockMovement = true;
        this.cooldownTicks = 80;
        this.windUpTicks = 20;
        this.activeTicks = 15;
        this.recoveryTicks = 15;
        this.telegraphType = TelegraphType.CONE;
        this.windUpSound = "random.bow";
        this.activeSound = "random.break";
    }

    @Override
    public boolean hasTypeSettings() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiAbilityConfig createConfigGui(IAbilityConfigCallback callback) {
        return new SubGuiAbilityCutter(this, callback);
    }

    @Override
    public boolean isTargetingModeLocked() {
        return true;
    }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[]{TargetingMode.AOE_SELF};
    }

    @Override
    public float getTelegraphRadius() {
        return range;
    }

    @Override
    public float getTelegraphLength() {
        return range;
    }

    @Override
    public float getTelegraphAngle() {
        return arcAngle;
    }

    @Override
    public void onExecute(EntityNPCInterface npc, EntityLivingBase target, World world) {
        hitEntities.clear();
        currentRotation = -arcAngle / 2.0f;
    }

    @Override
    public void onActiveTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
        if (world.isRemote) return;

        switch (sweepMode) {
            case SWIPE:
                if (currentRotation > arcAngle / 2.0f) {
                    return;
                }
                performSweepDamage(npc, world, innerRadius, range, currentRotation);
                currentRotation += sweepSpeed;
                break;

            case SPIN:
                currentRotation = (currentRotation + sweepSpeed) % 360.0f;
                hitEntities.clear();
                performSweepDamage(npc, world, innerRadius, range, currentRotation);
                break;
        }
    }

    private void performSweepDamage(EntityNPCInterface npc, World world, float minDist, float maxDist, float angleOffset) {
        float casterYaw = npc.rotationYaw + angleOffset;

        AxisAlignedBB searchBox = AxisAlignedBB.getBoundingBox(
            npc.posX - maxDist, npc.posY - 1, npc.posZ - maxDist,
            npc.posX + maxDist, npc.posY + 3, npc.posZ + maxDist
        );

        @SuppressWarnings("unchecked")
        List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, searchBox);

        for (EntityLivingBase entity : entities) {
            if (entity == npc) continue;
            if (hitEntities.contains(entity.getEntityId())) continue;
            // If not piercing, stop after hitting one entity this sweep
            if (!piercing && !hitEntities.isEmpty()) break;

            double dx = entity.posX - npc.posX;
            double dz = entity.posZ - npc.posZ;
            double dist = Math.sqrt(dx * dx + dz * dz);

            if (dist < minDist || dist > maxDist) continue;
            if (!isInArc(dx, dz, casterYaw, arcAngle)) continue;

            hitEntities.add(entity.getEntityId());

            float distFactor = 1.0f - ((float) dist / maxDist) * 0.3f;
            float actualDamage = damage * distFactor;

            // Apply damage with scripted event support
            boolean wasHit = applyAbilityDamage(npc, entity, actualDamage, knockback);

            // Only apply effects if hit wasn't cancelled
            if (wasHit) {
                if (stunDuration > 0) {
                    entity.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, stunDuration, 10));
                    entity.addPotionEffect(new PotionEffect(Potion.weakness.id, stunDuration, 2));
                }
                if (poisonDurationSeconds > 0) {
                    entity.addPotionEffect(new PotionEffect(Potion.poison.id, poisonDurationSeconds * 20, poisonLevel));
                }
            }
        }
    }

    private boolean isInArc(double dx, double dz, float casterYaw, float arcWidth) {
        double angleToEntity = Math.toDegrees(Math.atan2(-dx, dz));
        double angleDiff = normalizeAngle(angleToEntity - casterYaw);
        return Math.abs(angleDiff) <= arcWidth / 2.0;
    }

    private double normalizeAngle(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }

    @Override
    public void onComplete(EntityNPCInterface npc, EntityLivingBase target) {
        hitEntities.clear();
        currentRotation = 0.0f;
    }

    @Override
    public void onInterrupt(EntityNPCInterface npc, DamageSource source, float damage) {
        hitEntities.clear();
        currentRotation = 0.0f;
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("arcAngle", arcAngle);
        nbt.setFloat("range", range);
        nbt.setFloat("damage", damage);
        nbt.setFloat("knockback", knockback);
        nbt.setString("sweepMode", sweepMode.name());
        nbt.setFloat("sweepSpeed", sweepSpeed);
        nbt.setInteger("stunDuration", stunDuration);
        nbt.setInteger("poisonDurationSeconds", poisonDurationSeconds);
        nbt.setInteger("poisonLevel", poisonLevel);
        nbt.setBoolean("piercing", piercing);
        nbt.setFloat("innerRadius", innerRadius);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.arcAngle = nbt.hasKey("arcAngle") ? nbt.getFloat("arcAngle") : 90.0f;
        this.range = nbt.hasKey("range") ? nbt.getFloat("range") : 6.0f;
        this.damage = nbt.hasKey("damage") ? nbt.getFloat("damage") : 7.0f;
        this.knockback = nbt.hasKey("knockback") ? nbt.getFloat("knockback") : 1.5f;
        try {
            this.sweepMode = SweepMode.valueOf(nbt.getString("sweepMode"));
        } catch (Exception e) {
            this.sweepMode = SweepMode.SWIPE;
        }
        this.sweepSpeed = nbt.hasKey("sweepSpeed") ? nbt.getFloat("sweepSpeed") : 6.0f;
        this.stunDuration = nbt.hasKey("stunDuration") ? nbt.getInteger("stunDuration") : 0;
        this.poisonDurationSeconds = nbt.hasKey("poisonDurationSeconds") ? nbt.getInteger("poisonDurationSeconds") : 0;
        this.poisonLevel = nbt.hasKey("poisonLevel") ? nbt.getInteger("poisonLevel") : 0;
        this.piercing = !nbt.hasKey("piercing") || nbt.getBoolean("piercing");
        this.innerRadius = nbt.hasKey("innerRadius") ? nbt.getFloat("innerRadius") : 0.0f;
    }

    // Getters & Setters
    public float getArcAngle() {
        return arcAngle;
    }

    public void setArcAngle(float arcAngle) {
        this.arcAngle = arcAngle;
    }

    public float getRange() {
        return range;
    }

    public void setRange(float range) {
        this.range = range;
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public float getKnockback() {
        return knockback;
    }

    public void setKnockback(float knockback) {
        this.knockback = knockback;
    }

    public SweepMode getSweepMode() {
        return sweepMode;
    }

    public void setSweepMode(SweepMode sweepMode) {
        this.sweepMode = sweepMode;
    }

    public float getSweepSpeed() {
        return sweepSpeed;
    }

    public void setSweepSpeed(float sweepSpeed) {
        this.sweepSpeed = sweepSpeed;
    }

    public int getStunDuration() {
        return stunDuration;
    }

    public void setStunDuration(int stunDuration) {
        this.stunDuration = stunDuration;
    }

    public int getPoisonDurationSeconds() {
        return poisonDurationSeconds;
    }

    public void setPoisonDurationSeconds(int poisonDurationSeconds) {
        this.poisonDurationSeconds = poisonDurationSeconds;
    }

    public int getPoisonLevel() {
        return poisonLevel;
    }

    public void setPoisonLevel(int poisonLevel) {
        this.poisonLevel = poisonLevel;
    }

    public boolean isPiercing() {
        return piercing;
    }

    public void setPiercing(boolean piercing) {
        this.piercing = piercing;
    }

    public float getInnerRadius() {
        return innerRadius;
    }

    public void setInnerRadius(float innerRadius) {
        this.innerRadius = innerRadius;
    }

    public float getCurrentRotation() {
        return currentRotation;
    }
}
