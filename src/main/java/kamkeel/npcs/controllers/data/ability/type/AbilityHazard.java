package kamkeel.npcs.controllers.data.ability.type;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.ability.telegraph.TelegraphType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Hazard ability: Creates persistent ground effect zones.
 * Deals damage and/or applies debuffs to entities in the zone over time.
 */
public class AbilityHazard extends Ability {

    public enum HazardShape {
        CIRCLE,
        RING,
        CONE
    }

    public enum PlacementMode {
        AT_CASTER,
        AT_TARGET,
        FOLLOW_CASTER,
        FOLLOW_TARGET
    }

    private float radius = 4.0f;
    private float innerRadius = 0.0f;
    private float coneAngle = 45.0f;
    private HazardShape shape = HazardShape.CIRCLE;
    private PlacementMode placement = PlacementMode.AT_TARGET;

    private float damagePerTick = 1.0f;
    private int damageInterval = 20;
    private boolean ignoreInvulnFrames = false;

    private int slownessLevel = -1;
    private int weaknessLevel = -1;
    private int poisonLevel = -1;
    private int witherLevel = -1;
    private int blindnessLevel = -1;
    private int debuffDuration = 40;

    private boolean affectsCaster = false;
    private float heightAbove = 2.0f;
    private float heightBelow = 1.0f;

    // Offset parameters - hazard spawns near target, not exactly on them
    private float minOffset = 0.0f;  // Minimum offset from target
    private float maxOffset = 2.0f;  // Maximum offset from target
    private boolean randomOffset = true; // Random or fixed offset

    // Runtime state
    private static final Random RANDOM = new Random();
    private transient double zoneX, zoneY, zoneZ;
    private transient boolean positionLocked = false;
    private transient int ticksSinceDamage = 0;
    private transient Set<Integer> damagedThisTick = new HashSet<>();

    public AbilityHazard() {
        this.typeId = "cnpc:hazard";
        this.name = "Hazard";
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 15.0f;
        this.lockMovement = true;
        this.cooldownTicks = 200;
        this.windUpTicks = 30;
        this.activeTicks = 100;
        this.recoveryTicks = 10;
        this.telegraphType = TelegraphType.CIRCLE;
    }

    @Override
    public boolean hasTypeSettings() { return true; }

    @Override
    public boolean isTargetingModeLocked() { return true; }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[] { TargetingMode.AGGRO_TARGET };
    }

    @Override
    public float getTelegraphRadius() { return radius; }

    @Override
    public TelegraphInstance createTelegraph(EntityNPCInterface npc, EntityLivingBase target) {
        TelegraphInstance instance = super.createTelegraph(npc, target);
        if (instance == null) return null;

        // Control telegraph following based on placement mode
        switch (placement) {
            case AT_CASTER:
            case FOLLOW_CASTER:
                // Telegraph stays at caster position, no following
                instance.setEntityIdToFollow(-1);
                instance.setX(npc.posX);
                instance.setY(npc.posY);
                instance.setZ(npc.posZ);
                break;
            case AT_TARGET:
                // Telegraph follows target during windup, locks on execute
                if (target != null) {
                    instance.setEntityIdToFollow(target.getEntityId());
                }
                break;
            case FOLLOW_TARGET:
                // Telegraph follows target during windup AND active phase
                if (target != null) {
                    instance.setEntityIdToFollow(target.getEntityId());
                }
                break;
        }
        return instance;
    }

    /**
     * Calculates offset position near the given coordinates.
     * Used to place hazard near target rather than exactly on them.
     */
    private double[] calculateOffsetPosition(double baseX, double baseY, double baseZ) {
        if (maxOffset <= 0) {
            return new double[]{baseX, baseY, baseZ};
        }

        double offsetDist;
        double offsetAngle;

        if (randomOffset) {
            // Random offset within min-max range
            offsetDist = minOffset + RANDOM.nextDouble() * (maxOffset - minOffset);
            offsetAngle = RANDOM.nextDouble() * Math.PI * 2;
        } else {
            // Fixed offset (use max, random angle)
            offsetDist = maxOffset;
            offsetAngle = RANDOM.nextDouble() * Math.PI * 2;
        }

        double offsetX = Math.cos(offsetAngle) * offsetDist;
        double offsetZ = Math.sin(offsetAngle) * offsetDist;

        return new double[]{baseX + offsetX, baseY, baseZ + offsetZ};
    }

    @Override
    public void onWindUpTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
        // AT_CASTER locks immediately, AT_TARGET follows during windup and locks on execute
        if (tick == 0) {
            switch (placement) {
                case AT_CASTER:
                    zoneX = npc.posX;
                    zoneY = npc.posY;
                    zoneZ = npc.posZ;
                    positionLocked = true;
                    break;
                case AT_TARGET:
                    // Position will be set from telegraph on execute
                    // Telegraph follows target during windup
                    positionLocked = false;
                    break;
                default:
                    positionLocked = false;
                    break;
            }
        }
    }

    @Override
    public void onExecute(EntityNPCInterface npc, EntityLivingBase target, World world) {
        // Lock position from telegraph (which was following target) or calculate it now
        TelegraphInstance telegraph = getTelegraphInstance();

        switch (placement) {
            case AT_CASTER:
                // Already locked during windup
                if (!positionLocked) {
                    zoneX = npc.posX;
                    zoneY = npc.posY;
                    zoneZ = npc.posZ;
                }
                break;
            case AT_TARGET:
                // Use telegraph position if available, apply offset
                if (telegraph != null) {
                    double[] pos = calculateOffsetPosition(telegraph.getX(), telegraph.getY(), telegraph.getZ());
                    zoneX = pos[0];
                    zoneY = pos[1];
                    zoneZ = pos[2];
                } else if (target != null) {
                    double[] pos = calculateOffsetPosition(target.posX, target.posY, target.posZ);
                    zoneX = pos[0];
                    zoneY = pos[1];
                    zoneZ = pos[2];
                } else {
                    zoneX = npc.posX;
                    zoneY = npc.posY;
                    zoneZ = npc.posZ;
                }
                break;
            case FOLLOW_CASTER:
                zoneX = npc.posX;
                zoneY = npc.posY;
                zoneZ = npc.posZ;
                break;
            case FOLLOW_TARGET:
                if (target != null) {
                    zoneX = target.posX;
                    zoneY = target.posY;
                    zoneZ = target.posZ;
                } else {
                    zoneX = npc.posX;
                    zoneY = npc.posY;
                    zoneZ = npc.posZ;
                }
                break;
        }
        positionLocked = true;
        ticksSinceDamage = damageInterval;
    }

    @Override
    public void onActiveTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
        if (world.isRemote) return;

        damagedThisTick.clear();
        ticksSinceDamage++;

        switch (placement) {
            case FOLLOW_CASTER:
                zoneX = npc.posX;
                zoneY = npc.posY;
                zoneZ = npc.posZ;
                break;
            case FOLLOW_TARGET:
                if (target != null) {
                    zoneX = target.posX;
                    zoneY = target.posY;
                    zoneZ = target.posZ;
                }
                break;
            default:
                break;
        }

        if (ticksSinceDamage >= damageInterval) {
            ticksSinceDamage = 0;

            AxisAlignedBB searchBox = AxisAlignedBB.getBoundingBox(
                zoneX - radius, zoneY - heightBelow, zoneZ - radius,
                zoneX + radius, zoneY + heightAbove, zoneZ + radius
            );

            @SuppressWarnings("unchecked")
            List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, searchBox);

            for (EntityLivingBase entity : entities) {
                if (entity == npc && !affectsCaster) continue;
                if (damagedThisTick.contains(entity.getEntityId())) continue;
                if (!isInZone(entity, npc)) continue;

                if (damagePerTick > 0) {
                    if (ignoreInvulnFrames) {
                        entity.hurtResistantTime = 0;
                    }
                    // Apply damage with scripted event support (no knockback for hazard ticks)
                    boolean wasHit = applyAbilityDamage(npc, entity, damagePerTick, 0, 0);
                    if (!wasHit) continue; // Skip debuffs if hit was cancelled
                }

                applyDebuffs(entity);
                damagedThisTick.add(entity.getEntityId());
            }
        }
    }

    private boolean isInZone(EntityLivingBase entity, EntityNPCInterface npc) {
        double dx = entity.posX - zoneX;
        double dz = entity.posZ - zoneZ;
        double dist = Math.sqrt(dx * dx + dz * dz);

        switch (shape) {
            case CIRCLE:
                return dist <= radius;
            case RING:
                return dist >= innerRadius && dist <= radius;
            case CONE:
                if (dist > radius) return false;
                float casterYaw = npc.rotationYaw;
                double angleToEntity = Math.toDegrees(Math.atan2(-dx, dz));
                double angleDiff = Math.abs(normalizeAngle(angleToEntity - casterYaw));
                return angleDiff <= coneAngle / 2;
            default:
                return dist <= radius;
        }
    }

    private double normalizeAngle(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }

    private void applyDebuffs(EntityLivingBase target) {
        if (slownessLevel >= 0) {
            target.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, debuffDuration, slownessLevel));
        }
        if (weaknessLevel >= 0) {
            target.addPotionEffect(new PotionEffect(Potion.weakness.id, debuffDuration, weaknessLevel));
        }
        if (poisonLevel >= 0) {
            target.addPotionEffect(new PotionEffect(Potion.poison.id, debuffDuration, poisonLevel));
        }
        if (witherLevel >= 0) {
            target.addPotionEffect(new PotionEffect(Potion.wither.id, debuffDuration, witherLevel));
        }
        if (blindnessLevel >= 0) {
            target.addPotionEffect(new PotionEffect(Potion.blindness.id, debuffDuration, blindnessLevel));
        }
    }

    @Override
    public void onComplete(EntityNPCInterface npc, EntityLivingBase target) {
        damagedThisTick.clear();
        positionLocked = false;
    }

    @Override
    public void onInterrupt(EntityNPCInterface npc, DamageSource source, float damage) {
        damagedThisTick.clear();
        positionLocked = false;
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("radius", radius);
        nbt.setFloat("innerRadius", innerRadius);
        nbt.setFloat("coneAngle", coneAngle);
        nbt.setString("shape", shape.name());
        nbt.setString("placement", placement.name());
        nbt.setFloat("damagePerTick", damagePerTick);
        nbt.setInteger("damageInterval", damageInterval);
        nbt.setBoolean("ignoreInvulnFrames", ignoreInvulnFrames);
        nbt.setInteger("slownessLevel", slownessLevel);
        nbt.setInteger("weaknessLevel", weaknessLevel);
        nbt.setInteger("poisonLevel", poisonLevel);
        nbt.setInteger("witherLevel", witherLevel);
        nbt.setInteger("blindnessLevel", blindnessLevel);
        nbt.setInteger("debuffDuration", debuffDuration);
        nbt.setBoolean("affectsCaster", affectsCaster);
        nbt.setFloat("heightAbove", heightAbove);
        nbt.setFloat("heightBelow", heightBelow);
        nbt.setFloat("minOffset", minOffset);
        nbt.setFloat("maxOffset", maxOffset);
        nbt.setBoolean("randomOffset", randomOffset);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.radius = nbt.hasKey("radius") ? nbt.getFloat("radius") : 4.0f;
        this.innerRadius = nbt.hasKey("innerRadius") ? nbt.getFloat("innerRadius") : 0.0f;
        this.coneAngle = nbt.hasKey("coneAngle") ? nbt.getFloat("coneAngle") : 45.0f;
        try {
            this.shape = HazardShape.valueOf(nbt.getString("shape"));
        } catch (Exception e) {
            this.shape = HazardShape.CIRCLE;
        }
        try {
            this.placement = PlacementMode.valueOf(nbt.getString("placement"));
        } catch (Exception e) {
            this.placement = PlacementMode.AT_TARGET;
        }
        this.damagePerTick = nbt.hasKey("damagePerTick") ? nbt.getFloat("damagePerTick") : 1.0f;
        this.damageInterval = nbt.hasKey("damageInterval") ? nbt.getInteger("damageInterval") : 20;
        this.ignoreInvulnFrames = nbt.hasKey("ignoreInvulnFrames") && nbt.getBoolean("ignoreInvulnFrames");
        this.slownessLevel = nbt.hasKey("slownessLevel") ? nbt.getInteger("slownessLevel") : -1;
        this.weaknessLevel = nbt.hasKey("weaknessLevel") ? nbt.getInteger("weaknessLevel") : -1;
        this.poisonLevel = nbt.hasKey("poisonLevel") ? nbt.getInteger("poisonLevel") : -1;
        this.witherLevel = nbt.hasKey("witherLevel") ? nbt.getInteger("witherLevel") : -1;
        this.blindnessLevel = nbt.hasKey("blindnessLevel") ? nbt.getInteger("blindnessLevel") : -1;
        this.debuffDuration = nbt.hasKey("debuffDuration") ? nbt.getInteger("debuffDuration") : 40;
        this.affectsCaster = nbt.hasKey("affectsCaster") && nbt.getBoolean("affectsCaster");
        this.heightAbove = nbt.hasKey("heightAbove") ? nbt.getFloat("heightAbove") : 2.0f;
        this.heightBelow = nbt.hasKey("heightBelow") ? nbt.getFloat("heightBelow") : 1.0f;
        this.minOffset = nbt.hasKey("minOffset") ? nbt.getFloat("minOffset") : 0.0f;
        this.maxOffset = nbt.hasKey("maxOffset") ? nbt.getFloat("maxOffset") : 2.0f;
        this.randomOffset = !nbt.hasKey("randomOffset") || nbt.getBoolean("randomOffset");
    }

    // Getters & Setters
    public float getRadius() { return radius; }
    public void setRadius(float radius) { this.radius = radius; }

    public float getInnerRadius() { return innerRadius; }
    public void setInnerRadius(float innerRadius) { this.innerRadius = innerRadius; }

    public float getConeAngle() { return coneAngle; }
    public void setConeAngle(float coneAngle) { this.coneAngle = coneAngle; }

    public HazardShape getShape() { return shape; }
    public void setShape(HazardShape shape) { this.shape = shape; }

    public PlacementMode getPlacement() { return placement; }
    public void setPlacement(PlacementMode placement) { this.placement = placement; }

    public float getDamagePerTick() { return damagePerTick; }
    public void setDamagePerTick(float damagePerTick) { this.damagePerTick = damagePerTick; }

    public int getDamageInterval() { return damageInterval; }
    public void setDamageInterval(int damageInterval) { this.damageInterval = damageInterval; }

    public boolean isIgnoreInvulnFrames() { return ignoreInvulnFrames; }
    public void setIgnoreInvulnFrames(boolean ignoreInvulnFrames) { this.ignoreInvulnFrames = ignoreInvulnFrames; }

    public int getSlownessLevel() { return slownessLevel; }
    public void setSlownessLevel(int slownessLevel) { this.slownessLevel = slownessLevel; }

    public int getWeaknessLevel() { return weaknessLevel; }
    public void setWeaknessLevel(int weaknessLevel) { this.weaknessLevel = weaknessLevel; }

    public int getPoisonLevel() { return poisonLevel; }
    public void setPoisonLevel(int poisonLevel) { this.poisonLevel = poisonLevel; }

    public int getWitherLevel() { return witherLevel; }
    public void setWitherLevel(int witherLevel) { this.witherLevel = witherLevel; }

    public int getBlindnessLevel() { return blindnessLevel; }
    public void setBlindnessLevel(int blindnessLevel) { this.blindnessLevel = blindnessLevel; }

    public int getDebuffDuration() { return debuffDuration; }
    public void setDebuffDuration(int debuffDuration) { this.debuffDuration = debuffDuration; }

    public boolean isAffectsCaster() { return affectsCaster; }
    public void setAffectsCaster(boolean affectsCaster) { this.affectsCaster = affectsCaster; }

    public float getHeightAbove() { return heightAbove; }
    public void setHeightAbove(float heightAbove) { this.heightAbove = heightAbove; }

    public float getHeightBelow() { return heightBelow; }
    public void setHeightBelow(float heightBelow) { this.heightBelow = heightBelow; }

    public float getMinOffset() { return minOffset; }
    public void setMinOffset(float minOffset) { this.minOffset = minOffset; }

    public float getMaxOffset() { return maxOffset; }
    public void setMaxOffset(float maxOffset) { this.maxOffset = maxOffset; }

    public boolean isRandomOffset() { return randomOffset; }
    public void setRandomOffset(boolean randomOffset) { this.randomOffset = randomOffset; }

    // Runtime getters
    public double getZoneX() { return zoneX; }
    public double getZoneY() { return zoneY; }
    public double getZoneZ() { return zoneZ; }
}
