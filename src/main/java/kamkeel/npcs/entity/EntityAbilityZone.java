package kamkeel.npcs.entity;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.NpcDamageSource;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Shared zone entity for Trap and Hazard abilities.
 * Handles rendering, positioning, and damage logic.
 */
public class EntityAbilityZone extends Entity implements IEntityAdditionalSpawnData {

    // ═══════════════════════════════════════════════════════════════════
    // ENUMS
    // ═══════════════════════════════════════════════════════════════════

    public enum ZoneType { TRAP, HAZARD }
    public enum ZoneShape {
        CIRCLE, SQUARE;

        @Override
        public String toString() {
            switch (this) {
                case CIRCLE: return "ability.shape.circle";
                case SQUARE: return "ability.shape.square";
                default: return name();
            }
        }
    }

    public enum AccentStyle {
        STATIC, SWAYING, FLICKERING;

        @Override
        public String toString() {
            switch (this) {
                case STATIC: return "ability.accentStyle.static";
                case SWAYING: return "ability.accentStyle.swaying";
                case FLICKERING: return "ability.accentStyle.flickering";
                default: return name();
            }
        }
    }

    public enum ParticleMotion {
        RISING, DRIFTING, SPARKS;

        @Override
        public String toString() {
            switch (this) {
                case RISING: return "ability.particleMotion.rising";
                case DRIFTING: return "ability.particleMotion.drifting";
                case SPARKS: return "ability.particleMotion.sparks";
                default: return name();
            }
        }
    }

    /** Tiny Y offset above ground to prevent z-clipping */
    private static final double GROUND_OFFSET = 0.005;

    // ═══════════════════════════════════════════════════════════════════
    // COMMON PROPERTIES (synced via ByteBuf)
    // ═══════════════════════════════════════════════════════════════════

    private ZoneType zoneType = ZoneType.TRAP;
    private ZoneShape shape = ZoneShape.CIRCLE;

    private int ownerEntityId = -1;

    private float radius = 4.0f;

    private int durationTicks = 200;
    private int maxTicks = 200;

    private int innerColor = 0xFF6600;
    private int outerColor = 0xFF0000;
    private boolean outerColorEnabled = true;

    private float zoneHeight = 2.0f;

    // Visual parameters (synced for renderer)
    private float particleDensity = 1.0f;
    private float particleScale = 1.0f;
    private float animSpeed = 1.0f;
    private float lightningDensity = 1.0f;

    // Visual layer parameters (always active)
    private boolean groundFill = true;
    private float groundAlpha = 0.25f;
    private boolean rings = true;
    private int ringCount = 3;
    private boolean border = true;
    private float borderSpeed = 1.0f;
    private boolean accents = true;
    private int accentStyle = 0; // AccentStyle ordinal
    private boolean lightning = false;
    private boolean particles = true;
    private int particleMotion = 0; // ParticleMotion ordinal
    private String particleDir = "";
    private int particleSize = 32;
    private boolean particleGlow = true;

    // Lifetime
    private long deathWorldTime = -1;

    // Effects
    private List<AbilityEffect> effects = new ArrayList<>();

    // ═══════════════════════════════════════════════════════════════════
    // TRAP-SPECIFIC PROPERTIES
    // ═══════════════════════════════════════════════════════════════════

    private int armTime = 20;
    private float triggerRadius = 2.0f;
    private int maxTriggers = 1;
    private int triggerCooldown = 20;
    private float damage = 6.0f;
    private float damageRadius = 0.0f;
    private float knockback = 0.5f;

    // ═══════════════════════════════════════════════════════════════════
    // HAZARD-SPECIFIC PROPERTIES
    // ═══════════════════════════════════════════════════════════════════

    private float damagePerSecond = 1.0f;
    private int damageInterval = 20;
    private boolean ignoreInvulnFrames = false;
    private boolean affectsCaster = false;

    // ═══════════════════════════════════════════════════════════════════
    // RUNTIME STATE (transient)
    // ═══════════════════════════════════════════════════════════════════

    // Trap runtime
    private transient boolean armed = false;
    private transient int triggerCount = 0;
    private transient int ticksSinceLastTrigger = 0;
    private transient Set<UUID> triggeredEntities = new HashSet<>();
    private transient int triggerFlashTick = -1; // For visual flash on trigger

    // Hazard runtime
    private transient int ticksSinceDamage = 0;
    private transient Set<Integer> damagedThisTick = new HashSet<>();

    // Preview mode
    private boolean previewMode = false;
    private EntityLivingBase previewOwner = null;

    // ═══════════════════════════════════════════════════════════════════
    // CONSTRUCTORS
    // ═══════════════════════════════════════════════════════════════════

    public EntityAbilityZone(World world) {
        super(world);
        this.setSize(0.1f, 0.1f);
        this.noClip = true;
        this.isImmuneToFire = true;
        this.ignoreFrustumCheck = true;
    }

    /**
     * Full constructor for creating a zone entity.
     * Use the static factory methods for cleaner creation.
     * Position is snapped to ground level with a tiny offset to avoid z-clipping.
     */
    private EntityAbilityZone(World world, ZoneType type, EntityLivingBase owner,
                               double x, double y, double z) {
        this(world);
        this.zoneType = type;
        this.ownerEntityId = owner.getEntityId();
        // Snap to ground with tiny offset to prevent z-clipping
        double groundY = Ability.findGroundLevel(world, x, y, z) + GROUND_OFFSET;
        this.setPosition(x, groundY, z);
    }

    /**
     * Create a Trap zone entity.
     */
    public static EntityAbilityZone createTrap(World world, EntityLivingBase owner,
                                                double x, double y, double z,
                                                ZoneShape shape,
                                                float triggerRadius, int armTime, int maxTriggers,
                                                int triggerCooldown, float damage, float damageRadius,
                                                float knockback, int durationTicks,
                                                int innerColor, int outerColor, boolean outerColorEnabled,
                                                float zoneHeight,
                                                float particleDensity, float particleScale,
                                                float animSpeed, float lightningDensity,
                                                List<AbilityEffect> effects) {
        EntityAbilityZone zone = new EntityAbilityZone(world, ZoneType.TRAP, owner, x, y, z);
        zone.shape = shape;
        zone.radius = triggerRadius;
        zone.triggerRadius = triggerRadius;
        zone.armTime = armTime;
        zone.maxTriggers = maxTriggers;
        zone.triggerCooldown = triggerCooldown;
        zone.damage = damage;
        zone.damageRadius = damageRadius;
        zone.knockback = knockback;
        zone.durationTicks = durationTicks;
        zone.maxTicks = durationTicks;
        zone.innerColor = innerColor;
        zone.outerColor = outerColor;
        zone.outerColorEnabled = outerColorEnabled;
        zone.zoneHeight = zoneHeight;
        zone.particleDensity = particleDensity;
        zone.particleScale = particleScale;
        zone.animSpeed = animSpeed;
        zone.lightningDensity = lightningDensity;
        if (effects != null) {
            for (AbilityEffect e : effects) {
                zone.effects.add(e.copy());
            }
        }
        // Ready to trigger immediately after arm time
        zone.ticksSinceLastTrigger = triggerCooldown;
        return zone;
    }

    /**
     * Create a Hazard zone entity.
     */
    public static EntityAbilityZone createHazard(World world, EntityLivingBase owner,
                                                  double x, double y, double z,
                                                  ZoneShape shape,
                                                  float radius,
                                                  float damagePerSecond, int damageInterval,
                                                  boolean ignoreInvulnFrames, boolean affectsCaster,
                                                  int durationTicks,
                                                  int innerColor, int outerColor, boolean outerColorEnabled,
                                                  float zoneHeight,
                                                  float particleDensity, float particleScale,
                                                  float animSpeed, float lightningDensity,
                                                  List<AbilityEffect> effects) {
        EntityAbilityZone zone = new EntityAbilityZone(world, ZoneType.HAZARD, owner, x, y, z);
        zone.shape = shape;
        zone.radius = radius;
        zone.damagePerSecond = damagePerSecond;
        zone.damageInterval = damageInterval;
        zone.ignoreInvulnFrames = ignoreInvulnFrames;
        zone.affectsCaster = affectsCaster;
        zone.durationTicks = durationTicks;
        zone.maxTicks = durationTicks;
        zone.innerColor = innerColor;
        zone.outerColor = outerColor;
        zone.outerColorEnabled = outerColorEnabled;
        zone.zoneHeight = zoneHeight;
        zone.particleDensity = particleDensity;
        zone.particleScale = particleScale;
        zone.animSpeed = animSpeed;
        zone.lightningDensity = lightningDensity;
        if (effects != null) {
            for (AbilityEffect e : effects) {
                zone.effects.add(e.copy());
            }
        }
        // Ready to deal damage immediately
        zone.ticksSinceDamage = damageInterval;
        return zone;
    }

    // ═══════════════════════════════════════════════════════════════════
    // ENTITY LIFECYCLE
    // ═══════════════════════════════════════════════════════════════════

    @Override
    protected void entityInit() {
        // No DataWatcher needed
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        super.onUpdate();

        // Set death time on first tick
        if (deathWorldTime < 0 && worldObj != null) {
            deathWorldTime = worldObj.getTotalWorldTime() + maxTicks;
        }

        // Check lifetime
        if (deathWorldTime > 0 && worldObj.getTotalWorldTime() >= deathWorldTime) {
            this.setDead();
            return;
        }

        // Server handles damage
        if (!worldObj.isRemote && !previewMode) {
            switch (zoneType) {
                case TRAP:
                    tickTrap();
                    break;
                case HAZARD:
                    tickHazard();
                    break;
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // TRAP LOGIC (server-only)
    // ═══════════════════════════════════════════════════════════════════

    private void tickTrap() {
        // Arm time countdown
        if (!armed) {
            if (ticksExisted >= armTime) {
                armed = true;
            }
            return;
        }

        ticksSinceLastTrigger++;

        // All triggers used
        if (maxTriggers > 0 && triggerCount >= maxTriggers) {
            this.setDead();
            return;
        }

        // Cooldown between triggers
        if (ticksSinceLastTrigger < triggerCooldown) {
            return;
        }

        // Proximity detection
        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(
            posX - triggerRadius, posY - 0.5, posZ - triggerRadius,
            posX + triggerRadius, posY + zoneHeight, posZ + triggerRadius
        );

        Entity owner = getOwner();
        @SuppressWarnings("unchecked")
        List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, box);

        for (EntityLivingBase entity : entities) {
            if (owner != null && entity == owner) continue;
            if (entity.isDead) continue;
            if (maxTriggers == 1 && triggeredEntities.contains(entity.getUniqueID())) continue;

            double dx = entity.posX - posX;
            double dz = entity.posZ - posZ;

            boolean inRange;
            if (shape == ZoneShape.SQUARE) {
                inRange = Math.abs(dx) <= triggerRadius && Math.abs(dz) <= triggerRadius;
            } else {
                double dist = Math.sqrt(dx * dx + dz * dz);
                inRange = dist <= triggerRadius;
            }

            if (inRange) {
                triggerTrap(entity);
                return;
            }
        }
    }

    private void triggerTrap(EntityLivingBase triggerer) {
        Entity owner = getOwner();
        Set<EntityLivingBase> affected = new HashSet<>();

        if (damageRadius > 0) {
            AxisAlignedBB box = AxisAlignedBB.getBoundingBox(
                posX - damageRadius, posY - 0.5, posZ - damageRadius,
                posX + damageRadius, posY + zoneHeight, posZ + damageRadius
            );

            @SuppressWarnings("unchecked")
            List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, box);

            for (EntityLivingBase entity : entities) {
                if (owner != null && entity == owner) continue;
                double dx = entity.posX - posX;
                double dz = entity.posZ - posZ;
                if (shape == ZoneShape.SQUARE) {
                    if (Math.abs(dx) <= damageRadius && Math.abs(dz) <= damageRadius) {
                        affected.add(entity);
                    }
                } else {
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    if (dist <= damageRadius) {
                        affected.add(entity);
                    }
                }
            }
        } else {
            affected.add(triggerer);
        }

        boolean anyDamaged = false;
        for (EntityLivingBase entity : affected) {
            boolean hit = applyDamage(entity, owner, damage);
            if (hit) {
                anyDamaged = true;
            }

            // Apply knockback only if damage landed
            if (hit && knockback > 0) {
                double dx = entity.posX - posX;
                double dz = entity.posZ - posZ;
                double len = Math.sqrt(dx * dx + dz * dz);
                if (len > 0) {
                    entity.addVelocity((dx / len) * knockback * 0.5, 0.1, (dz / len) * knockback * 0.5);
                    entity.velocityChanged = true;
                }
            }

            // Apply effects only if damage landed
            if (hit) {
                applyEffects(entity);
            }
        }

        // Only count the trigger if at least one entity was actually damaged
        if (anyDamaged) {
            triggerCount++;
            ticksSinceLastTrigger = 0;
            triggeredEntities.add(triggerer.getUniqueID());
            triggerFlashTick = ticksExisted;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // HAZARD LOGIC (server-only)
    // ═══════════════════════════════════════════════════════════════════

    private void tickHazard() {
        damagedThisTick.clear();
        ticksSinceDamage++;

        if (ticksSinceDamage < damageInterval) {
            return;
        }
        ticksSinceDamage = 0;

        AxisAlignedBB searchBox = AxisAlignedBB.getBoundingBox(
            posX - radius, posY - 0.5, posZ - radius,
            posX + radius, posY + zoneHeight, posZ + radius
        );

        Entity owner = getOwner();
        @SuppressWarnings("unchecked")
        List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, searchBox);

        for (EntityLivingBase entity : entities) {
            if (owner != null && entity == owner && !affectsCaster) continue;
            if (damagedThisTick.contains(entity.getEntityId())) continue;
            if (!isInZone(entity)) continue;

            if (damagePerSecond > 0) {
                if (ignoreInvulnFrames) {
                    entity.hurtResistantTime = 0;
                }
                applyDamage(entity, owner, damagePerSecond);
            }

            applyEffects(entity);
            damagedThisTick.add(entity.getEntityId());
        }
    }

    private boolean isInZone(EntityLivingBase entity) {
        double dx = entity.posX - posX;
        double dz = entity.posZ - posZ;

        switch (shape) {
            case CIRCLE:
                return Math.sqrt(dx * dx + dz * dz) <= radius;
            case SQUARE:
                return Math.abs(dx) <= radius && Math.abs(dz) <= radius;
            default:
                return Math.sqrt(dx * dx + dz * dz) <= radius;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // DAMAGE
    // ═══════════════════════════════════════════════════════════════════

    private boolean applyDamage(EntityLivingBase target, Entity owner, float dmg) {
        if (dmg <= 0) return false;

        if (owner instanceof EntityNPCInterface) {
            return target.attackEntityFrom(new NpcDamageSource("npc_ability", (EntityNPCInterface) owner), dmg);
        } else if (owner instanceof EntityPlayer) {
            return target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) owner), dmg);
        } else if (owner instanceof EntityLivingBase) {
            return target.attackEntityFrom(DamageSource.causeMobDamage((EntityLivingBase) owner), dmg);
        } else {
            return target.attackEntityFrom(new NpcDamageSource("npc_ability", null), dmg);
        }
    }

    private void applyEffects(EntityLivingBase entity) {
        if (entity == null || effects.isEmpty()) return;
        for (AbilityEffect effect : effects) {
            effect.apply(entity);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // ENTITY HELPERS
    // ═══════════════════════════════════════════════════════════════════

    private Entity getOwner() {
        if (previewMode && previewOwner != null) return previewOwner;
        if (ownerEntityId == -1) return null;
        return worldObj.getEntityByID(ownerEntityId);
    }

    public void setupPreview(EntityLivingBase owner) {
        this.previewMode = true;
        this.previewOwner = owner;
    }

    // ═══════════════════════════════════════════════════════════════════
    // RENDERING SUPPORT
    // ═══════════════════════════════════════════════════════════════════

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        return distance < 16384.0D; // 128 blocks
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1; // Translucent pass
    }

    @Override
    public float getBrightness(float partialTicks) {
        return 1.0f;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getBrightnessForRender(float partialTicks) {
        return 0xF000F0;
    }

    // ═══════════════════════════════════════════════════════════════════
    // GETTERS FOR RENDERER
    // ═══════════════════════════════════════════════════════════════════

    public ZoneType getZoneType() { return zoneType; }
    public ZoneShape getShape() { return shape; }
    public float getRadius() { return radius; }
    public int getInnerColor() { return innerColor; }
    public int getOuterColor() { return outerColor; }
    public boolean isOuterColorEnabled() { return outerColorEnabled; }
    public float getZoneHeight() { return zoneHeight; }
    public boolean isArmed() { return armed || ticksExisted >= armTime; }
    public int getTriggerFlashTick() { return triggerFlashTick; }
    public int getDurationTicks() { return durationTicks; }
    public int getMaxTicks() { return maxTicks; }
    public float getParticleDensity() { return particleDensity; }
    public float getParticleScale() { return particleScale; }
    public float getAnimSpeed() { return animSpeed; }
    public float getLightningDensity() { return lightningDensity; }

    // Visual layer getters
    public boolean isGroundFill() { return groundFill; }
    public float getGroundAlpha() { return groundAlpha; }
    public boolean isRings() { return rings; }
    public int getRingCount() { return ringCount; }
    public boolean isBorder() { return border; }
    public float getBorderSpeed() { return borderSpeed; }
    public boolean isAccents() { return accents; }
    public int getAccentStyle() { return accentStyle; }
    public boolean isLightning() { return lightning; }
    public boolean isParticles() { return particles; }
    public int getParticleMotion() { return particleMotion; }
    public String getParticleDir() { return particleDir; }
    public int getParticleSize() { return particleSize; }
    public boolean isParticleGlow() { return particleGlow; }

    /**
     * Apply visual settings from ability data.
     */
    public void applyVisual(boolean groundFill, float groundAlpha,
                             boolean rings, int ringCount,
                             boolean border, float borderSpeed,
                             boolean accents, int accentStyle,
                             boolean lightning,
                             boolean particles, int particleMotion,
                             String particleDir, int particleSize, boolean particleGlow) {
        this.groundFill = groundFill;
        this.groundAlpha = groundAlpha;
        this.rings = rings;
        this.ringCount = ringCount;
        this.border = border;
        this.borderSpeed = borderSpeed;
        this.accents = accents;
        this.accentStyle = accentStyle;
        this.lightning = lightning;
        this.particles = particles;
        this.particleMotion = particleMotion;
        this.particleDir = particleDir != null ? particleDir : "";
        this.particleSize = particleSize;
        this.particleGlow = particleGlow;
    }

    /**
     * Apply preset defaults by old style name (for backward compatibility).
     */
    private void applyPresetDefaults(String styleName) {
        switch (styleName) {
            case "TOXIC":
                groundFill = true; groundAlpha = 0.30f; rings = true; ringCount = 3;
                border = false; borderSpeed = 1.0f; accents = true; accentStyle = 1;
                lightning = false; particles = true; particleMotion = 0; particleGlow = true;
                break;
            case "INFERNO":
                groundFill = true; groundAlpha = 0.35f; rings = true; ringCount = 3;
                border = true; borderSpeed = 2.0f; accents = true; accentStyle = 2;
                lightning = false; particles = true; particleMotion = 0; particleGlow = true;
                break;
            case "ARCANE":
                groundFill = true; groundAlpha = 0.20f; rings = true; ringCount = 1;
                border = true; borderSpeed = 0.8f; accents = true; accentStyle = 0;
                lightning = false; particles = true; particleMotion = 1; particleGlow = true;
                break;
            case "ELECTRIC":
                groundFill = true; groundAlpha = 0.15f; rings = false; ringCount = 1;
                border = false; borderSpeed = 1.0f; accents = false; accentStyle = 0;
                lightning = true; particles = true; particleMotion = 2; particleGlow = true;
                break;
            case "FROST":
                groundFill = true; groundAlpha = 0.25f; rings = true; ringCount = 3;
                border = true; borderSpeed = 0.3f; accents = true; accentStyle = 0;
                lightning = false; particles = true; particleMotion = 1; particleGlow = true;
                break;
            default: // DEFAULT
                groundFill = true; groundAlpha = 0.25f; rings = true; ringCount = 3;
                border = true; borderSpeed = 1.0f; accents = true; accentStyle = 0;
                lightning = false; particles = false; particleMotion = 0; particleGlow = true;
                break;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // NBT SERIALIZATION
    // ═══════════════════════════════════════════════════════════════════

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        this.zoneType = ZoneType.values()[nbt.getInteger("ZoneType")];

        // Shape: read new string key first, fall back to old ordinal (always CIRCLE for old RING/CONE)
        if (nbt.hasKey("ShapeName")) {
            try {
                this.shape = ZoneShape.valueOf(nbt.getString("ShapeName"));
            } catch (Exception e) {
                this.shape = ZoneShape.CIRCLE;
            }
        } else {
            // Legacy: old ordinal data — RING(1) and CONE(2) map to CIRCLE
            this.shape = ZoneShape.CIRCLE;
        }

        this.ownerEntityId = nbt.getInteger("OwnerId");
        this.radius = nbt.getFloat("Radius");
        this.durationTicks = nbt.getInteger("Duration");
        this.maxTicks = nbt.getInteger("MaxTicks");
        this.innerColor = nbt.getInteger("InnerColor");
        this.outerColor = nbt.getInteger("OuterColor");
        this.outerColorEnabled = nbt.getBoolean("OuterColorEnabled");
        this.zoneHeight = nbt.hasKey("ZoneHeight") ? nbt.getFloat("ZoneHeight") : 2.0f;
        this.particleDensity = nbt.hasKey("ParticleDensity") ? nbt.getFloat("ParticleDensity") : 1.0f;
        this.particleScale = nbt.hasKey("ParticleScale") ? nbt.getFloat("ParticleScale") : 1.0f;
        this.animSpeed = nbt.hasKey("AnimSpeed") ? nbt.getFloat("AnimSpeed") : 1.0f;
        this.lightningDensity = nbt.hasKey("LightningDensity") ? nbt.getFloat("LightningDensity") : 1.0f;
        this.deathWorldTime = nbt.hasKey("DeathWorldTime") ? nbt.getLong("DeathWorldTime") : -1;

        // Visual fields — backward compat with old ZoneStyle format
        if (nbt.hasKey("ZoneStyle")) {
            // OLD FORMAT: apply preset defaults, then overlay CustomVisual
            applyPresetDefaults(nbt.getString("ZoneStyle"));
            if (nbt.hasKey("CustomVisual")) {
                NBTTagCompound cv = nbt.getCompoundTag("CustomVisual");
                this.groundFill = !cv.hasKey("GroundFill") || cv.getBoolean("GroundFill");
                this.groundAlpha = cv.hasKey("GroundAlpha") ? cv.getFloat("GroundAlpha") : 0.25f;
                this.rings = !cv.hasKey("Rings") || cv.getBoolean("Rings");
                this.ringCount = cv.hasKey("RingCount") ? cv.getInteger("RingCount") : 3;
                this.border = !cv.hasKey("Border") || cv.getBoolean("Border");
                this.borderSpeed = cv.hasKey("BorderSpeed") ? cv.getFloat("BorderSpeed") : 1.0f;
                this.accents = !cv.hasKey("Accents") || cv.getBoolean("Accents");
                this.accentStyle = cv.hasKey("AccentStyle") ? cv.getInteger("AccentStyle") : 0;
                this.lightning = cv.hasKey("Lightning") && cv.getBoolean("Lightning");
                this.particles = !cv.hasKey("Particles") || cv.getBoolean("Particles");
                this.particleMotion = cv.hasKey("ParticleMotion") ? cv.getInteger("ParticleMotion") : 0;
                this.particleDir = cv.hasKey("ParticleDir") ? cv.getString("ParticleDir") : "";
                this.particleSize = cv.hasKey("ParticleSize") ? cv.getInteger("ParticleSize") : 32;
                this.particleGlow = !cv.hasKey("ParticleGlow") || cv.getBoolean("ParticleGlow");
            }
        } else {
            // NEW FORMAT: read individual top-level keys
            this.groundFill = !nbt.hasKey("GroundFill") || nbt.getBoolean("GroundFill");
            this.groundAlpha = nbt.hasKey("GroundAlpha") ? nbt.getFloat("GroundAlpha") : 0.25f;
            this.rings = !nbt.hasKey("Rings") || nbt.getBoolean("Rings");
            this.ringCount = nbt.hasKey("RingCount") ? nbt.getInteger("RingCount") : 3;
            this.border = !nbt.hasKey("Border") || nbt.getBoolean("Border");
            this.borderSpeed = nbt.hasKey("BorderSpeed") ? nbt.getFloat("BorderSpeed") : 1.0f;
            this.accents = !nbt.hasKey("Accents") || nbt.getBoolean("Accents");
            this.accentStyle = nbt.hasKey("AccentStyle") ? nbt.getInteger("AccentStyle") : 0;
            this.lightning = nbt.hasKey("Lightning") && nbt.getBoolean("Lightning");
            this.particles = !nbt.hasKey("Particles") || nbt.getBoolean("Particles");
            this.particleMotion = nbt.hasKey("ParticleMotion") ? nbt.getInteger("ParticleMotion") : 0;
            this.particleDir = nbt.hasKey("ParticleDir") ? nbt.getString("ParticleDir") : "";
            this.particleSize = nbt.hasKey("ParticleSize") ? nbt.getInteger("ParticleSize") : 32;
            this.particleGlow = !nbt.hasKey("ParticleGlow") || nbt.getBoolean("ParticleGlow");
        }

        // Trap
        this.armTime = nbt.getInteger("ArmTime");
        this.triggerRadius = nbt.getFloat("TriggerRadius");
        this.maxTriggers = nbt.getInteger("MaxTriggers");
        this.triggerCooldown = nbt.getInteger("TriggerCooldown");
        this.damage = nbt.getFloat("Damage");
        this.damageRadius = nbt.getFloat("DamageRadius");
        this.knockback = nbt.getFloat("Knockback");

        // Hazard
        this.damagePerSecond = nbt.getFloat("DamagePerSecond");
        this.damageInterval = nbt.getInteger("DamageInterval");
        this.ignoreInvulnFrames = nbt.getBoolean("IgnoreInvuln");
        this.affectsCaster = nbt.getBoolean("AffectsCaster");

        // Effects
        effects.clear();
        if (nbt.hasKey("Effects")) {
            NBTTagList effectList = nbt.getTagList("Effects", 10);
            for (int i = 0; i < effectList.tagCount(); i++) {
                AbilityEffect effect = AbilityEffect.fromNBT(effectList.getCompoundTagAt(i));
                if (effect != null && effect.isValid()) {
                    effects.add(effect);
                }
            }
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setInteger("ZoneType", zoneType.ordinal());
        nbt.setString("ShapeName", shape.name());
        nbt.setInteger("OwnerId", ownerEntityId);
        nbt.setFloat("Radius", radius);
        nbt.setInteger("Duration", durationTicks);
        nbt.setInteger("MaxTicks", maxTicks);
        nbt.setInteger("InnerColor", innerColor);
        nbt.setInteger("OuterColor", outerColor);
        nbt.setBoolean("OuterColorEnabled", outerColorEnabled);
        nbt.setFloat("ZoneHeight", zoneHeight);
        nbt.setFloat("ParticleDensity", particleDensity);
        nbt.setFloat("ParticleScale", particleScale);
        nbt.setFloat("AnimSpeed", animSpeed);
        nbt.setFloat("LightningDensity", lightningDensity);
        nbt.setLong("DeathWorldTime", deathWorldTime);

        // Visual fields — always written as top-level keys
        nbt.setBoolean("GroundFill", groundFill);
        nbt.setFloat("GroundAlpha", groundAlpha);
        nbt.setBoolean("Rings", rings);
        nbt.setInteger("RingCount", ringCount);
        nbt.setBoolean("Border", border);
        nbt.setFloat("BorderSpeed", borderSpeed);
        nbt.setBoolean("Accents", accents);
        nbt.setInteger("AccentStyle", accentStyle);
        nbt.setBoolean("Lightning", lightning);
        nbt.setBoolean("Particles", particles);
        nbt.setInteger("ParticleMotion", particleMotion);
        nbt.setString("ParticleDir", particleDir);
        nbt.setInteger("ParticleSize", particleSize);
        nbt.setBoolean("ParticleGlow", particleGlow);

        // Trap
        nbt.setInteger("ArmTime", armTime);
        nbt.setFloat("TriggerRadius", triggerRadius);
        nbt.setInteger("MaxTriggers", maxTriggers);
        nbt.setInteger("TriggerCooldown", triggerCooldown);
        nbt.setFloat("Damage", damage);
        nbt.setFloat("DamageRadius", damageRadius);
        nbt.setFloat("Knockback", knockback);

        // Hazard
        nbt.setFloat("DamagePerSecond", damagePerSecond);
        nbt.setInteger("DamageInterval", damageInterval);
        nbt.setBoolean("IgnoreInvuln", ignoreInvulnFrames);
        nbt.setBoolean("AffectsCaster", affectsCaster);

        // Effects
        NBTTagList effectList = new NBTTagList();
        for (AbilityEffect effect : effects) {
            effectList.appendTag(effect.writeNBT());
        }
        nbt.setTag("Effects", effectList);
    }

    // ═══════════════════════════════════════════════════════════════════
    // SPAWN DATA (ByteBuf for client sync)
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeInt(zoneType.ordinal());
        ByteBufUtils.writeUTF8String(buffer, shape.name());
        buffer.writeInt(ownerEntityId);
        buffer.writeFloat(radius);
        buffer.writeInt(durationTicks);
        buffer.writeInt(maxTicks);
        buffer.writeInt(innerColor);
        buffer.writeInt(outerColor);
        buffer.writeBoolean(outerColorEnabled);
        buffer.writeFloat(zoneHeight);
        buffer.writeFloat(particleDensity);
        buffer.writeFloat(particleScale);
        buffer.writeFloat(animSpeed);
        buffer.writeFloat(lightningDensity);

        // Visual fields — always written individually
        buffer.writeBoolean(groundFill);
        buffer.writeFloat(groundAlpha);
        buffer.writeBoolean(rings);
        buffer.writeInt(ringCount);
        buffer.writeBoolean(border);
        buffer.writeFloat(borderSpeed);
        buffer.writeBoolean(accents);
        buffer.writeInt(accentStyle);
        buffer.writeBoolean(lightning);
        buffer.writeBoolean(particles);
        buffer.writeInt(particleMotion);
        ByteBufUtils.writeUTF8String(buffer, particleDir);
        buffer.writeInt(particleSize);
        buffer.writeBoolean(particleGlow);

        // Trap-specific (client needs armTime for visual state)
        buffer.writeInt(armTime);

        // Effects via NBT in buffer
        NBTTagCompound effectsNbt = new NBTTagCompound();
        NBTTagList effectList = new NBTTagList();
        for (AbilityEffect effect : effects) {
            effectList.appendTag(effect.writeNBT());
        }
        effectsNbt.setTag("Effects", effectList);
        ByteBufUtils.writeTag(buffer, effectsNbt);
    }

    @Override
    public void readSpawnData(ByteBuf buffer) {
        this.zoneType = ZoneType.values()[buffer.readInt()];
        String shapeName = ByteBufUtils.readUTF8String(buffer);
        try {
            this.shape = ZoneShape.valueOf(shapeName);
        } catch (Exception e) {
            this.shape = ZoneShape.CIRCLE;
        }
        this.ownerEntityId = buffer.readInt();
        this.radius = buffer.readFloat();
        this.durationTicks = buffer.readInt();
        this.maxTicks = buffer.readInt();
        this.innerColor = buffer.readInt();
        this.outerColor = buffer.readInt();
        this.outerColorEnabled = buffer.readBoolean();
        this.zoneHeight = buffer.readFloat();
        this.particleDensity = buffer.readFloat();
        this.particleScale = buffer.readFloat();
        this.animSpeed = buffer.readFloat();
        this.lightningDensity = buffer.readFloat();

        // Visual fields — always read individually
        this.groundFill = buffer.readBoolean();
        this.groundAlpha = buffer.readFloat();
        this.rings = buffer.readBoolean();
        this.ringCount = buffer.readInt();
        this.border = buffer.readBoolean();
        this.borderSpeed = buffer.readFloat();
        this.accents = buffer.readBoolean();
        this.accentStyle = buffer.readInt();
        this.lightning = buffer.readBoolean();
        this.particles = buffer.readBoolean();
        this.particleMotion = buffer.readInt();
        this.particleDir = ByteBufUtils.readUTF8String(buffer);
        this.particleSize = buffer.readInt();
        this.particleGlow = buffer.readBoolean();

        this.armTime = buffer.readInt();

        // Effects via NBT
        NBTTagCompound effectsNbt = ByteBufUtils.readTag(buffer);
        effects.clear();
        if (effectsNbt != null && effectsNbt.hasKey("Effects")) {
            NBTTagList effectList = effectsNbt.getTagList("Effects", 10);
            for (int i = 0; i < effectList.tagCount(); i++) {
                AbilityEffect effect = AbilityEffect.fromNBT(effectList.getCompoundTagAt(i));
                if (effect != null && effect.isValid()) {
                    effects.add(effect);
                }
            }
        }
    }
}
