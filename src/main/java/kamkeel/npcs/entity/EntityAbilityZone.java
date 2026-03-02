package kamkeel.npcs.entity;

import kamkeel.npcs.util.ByteBufUtils;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.data.effect.AbilityPotionEffect;
import kamkeel.npcs.controllers.data.ability.util.AbilityTargetHelper;
import kamkeel.npcs.controllers.data.ability.enums.TargetFilter;
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

/**
 * Shared zone entity for Trap and Hazard abilities.
 * ZoneType distinguishes behavior: TRAP (proximity-triggered burst) vs HAZARD (persistent AoE).
 */
public class EntityAbilityZone extends Entity implements IEntityAdditionalSpawnData {

    // ═══════════════════════════════════════════════════════════════════
    // ENUMS
    // ═══════════════════════════════════════════════════════════════════

    public enum ZoneType {TRAP, HAZARD}

    public enum ZoneShape {
        CIRCLE, SQUARE;

        @Override
        public String toString() {
            switch (this) {
                case CIRCLE:
                    return "ability.shape.circle";
                case SQUARE:
                    return "ability.shape.square";
                default:
                    return name();
            }
        }
    }

    public enum AccentStyle {
        STATIC, SWAYING, FLICKERING;

        @Override
        public String toString() {
            switch (this) {
                case STATIC:
                    return "ability.accentStyle.static";
                case SWAYING:
                    return "ability.accentStyle.swaying";
                case FLICKERING:
                    return "ability.accentStyle.flickering";
                default:
                    return name();
            }
        }
    }

    public enum ParticleMotion {
        RISING, DRIFTING, SPARKS;

        @Override
        public String toString() {
            switch (this) {
                case RISING:
                    return "ability.particleMotion.rising";
                case DRIFTING:
                    return "ability.particleMotion.drifting";
                case SPARKS:
                    return "ability.particleMotion.sparks";
                default:
                    return name();
            }
        }
    }

    private static final double GROUND_OFFSET = 0.005;
    private static final byte TRIGGER_FLASH_STATUS = 60;

    // ═══════════════════════════════════════════════════════════════════
    // COMMON PROPERTIES
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

    private float particleDensity = 1.0f;
    private float particleScale = 1.0f;
    private float animSpeed = 1.0f;
    private float lightningDensity = 1.0f;

    // Visual layer fields
    private boolean groundFill = true;
    private float groundAlpha = 0.25f;
    private boolean rings = true;
    private int ringCount = 3;
    private boolean border = true;
    private float borderSpeed = 1.0f;
    private boolean accents = true;
    private int accentStyle = 0;
    private boolean lightning = false;
    private boolean particles = true;
    private int particleMotion = 0;
    private String particleDir = "";
    private int particleSize = 32;
    private boolean particleGlow = true;

    private long deathWorldTime = -1;
    private List<AbilityPotionEffect> effects = new ArrayList<>();

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
    private boolean visible = true;

    // ═══════════════════════════════════════════════════════════════════
    // HAZARD-SPECIFIC PROPERTIES
    // ═══════════════════════════════════════════════════════════════════

    private float damagePerSecond = 1.0f;
    private int damageInterval = 20;
    private boolean ignoreIFrames = false;
    private boolean affectsCaster = false;

    // ═══════════════════════════════════════════════════════════════════
    // RUNTIME STATE
    // ═══════════════════════════════════════════════════════════════════

    private transient boolean armed = false;
    private transient int triggerCount = 0;
    private transient int ticksSinceLastTrigger = 0;
    private transient Set<Integer> triggeredEntities = new HashSet<>();
    private transient int triggerFlashTick = -1;

    private transient int ticksSinceDamage = 0;
    private transient Set<Integer> damagedThisTick = new HashSet<>();

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

    private EntityAbilityZone(World world, ZoneType type, EntityLivingBase owner,
                              double x, double y, double z) {
        this(world);
        this.zoneType = type;
        this.ownerEntityId = owner.getEntityId();
        // Snap to ground with tiny offset to prevent z-clipping
        double groundY = Ability.findGroundLevel(world, x, y, z) + GROUND_OFFSET;
        this.setPosition(x, groundY, z);
    }

    public static EntityAbilityZone createTrap(World world, EntityLivingBase owner,
                                               double x, double y, double z,
                                               ZoneShape shape,
                                               float triggerRadius, int armTime, int maxTriggers,
                                               int triggerCooldown, float damage, float damageRadius,
                                               float knockback, int durationTicks,
                                               boolean ignoreIFrames,
                                               int innerColor, int outerColor, boolean outerColorEnabled,
                                               float zoneHeight,
                                               float particleDensity, float particleScale,
                                               float animSpeed, float lightningDensity,
                                               boolean visible,
                                               List<AbilityPotionEffect> effects) {
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
        zone.ignoreIFrames = ignoreIFrames;
        zone.innerColor = innerColor;
        zone.outerColor = outerColor;
        zone.outerColorEnabled = outerColorEnabled;
        zone.zoneHeight = zoneHeight;
        zone.particleDensity = particleDensity;
        zone.particleScale = particleScale;
        zone.animSpeed = animSpeed;
        zone.lightningDensity = lightningDensity;
        zone.visible = visible;
        if (effects != null) {
            for (AbilityPotionEffect e : effects) {
                zone.effects.add(e.copy());
            }
        }
        zone.ticksSinceLastTrigger = triggerCooldown;
        return zone;
    }

    public static EntityAbilityZone createHazard(World world, EntityLivingBase owner,
                                                 double x, double y, double z,
                                                 ZoneShape shape,
                                                 float radius,
                                                 float damagePerSecond, int damageInterval,
                                                 boolean ignoreIFrames, boolean affectsCaster,
                                                 int durationTicks,
                                                 int innerColor, int outerColor, boolean outerColorEnabled,
                                                 float zoneHeight,
                                                 float particleDensity, float particleScale,
                                                 float animSpeed, float lightningDensity,
                                                 List<AbilityPotionEffect> effects) {
        EntityAbilityZone zone = new EntityAbilityZone(world, ZoneType.HAZARD, owner, x, y, z);
        zone.shape = shape;
        zone.radius = radius;
        zone.damagePerSecond = damagePerSecond;
        zone.damageInterval = damageInterval;
        zone.ignoreIFrames = ignoreIFrames;
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
            for (AbilityPotionEffect e : effects) {
                zone.effects.add(e.copy());
            }
        }
        zone.ticksSinceDamage = damageInterval;
        return zone;
    }

    // ═══════════════════════════════════════════════════════════════════
    // ENTITY LIFECYCLE
    // ═══════════════════════════════════════════════════════════════════

    @Override
    protected void entityInit() {
    }

    @Override
    public void handleHealthUpdate(byte id) {
        if (id == TRIGGER_FLASH_STATUS) {
            this.triggerFlashTick = this.ticksExisted;
        } else {
            super.handleHealthUpdate(id);
        }
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        super.onUpdate();

        if (deathWorldTime < 0 && worldObj != null) {
            deathWorldTime = worldObj.getTotalWorldTime() + maxTicks;
        }

        if (deathWorldTime > 0 && worldObj.getTotalWorldTime() >= deathWorldTime) {
            this.setDead();
            return;
        }

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
    // TRAP LOGIC
    // ═══════════════════════════════════════════════════════════════════

    private void tickTrap() {
        if (!armed) {
            if (ticksExisted >= armTime) {
                armed = true;
            }
            return;
        }

        ticksSinceLastTrigger++;

        if (maxTriggers > 0 && triggerCount >= maxTriggers) {
            this.setDead();
            return;
        }

        if (ticksSinceLastTrigger < triggerCooldown) {
            return;
        }

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
            if (maxTriggers == 1 && triggeredEntities.contains(entity.getEntityId())) continue;
            if (owner instanceof EntityLivingBase && !AbilityTargetHelper.shouldAffect((EntityLivingBase) owner, entity, TargetFilter.ENEMIES, false)) continue;

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
                if (owner instanceof EntityLivingBase && !AbilityTargetHelper.shouldAffect((EntityLivingBase) owner, entity, TargetFilter.ENEMIES, false)) continue;
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

            if (hit && knockback > 0) {
                double dx = entity.posX - posX;
                double dz = entity.posZ - posZ;
                double len = Math.sqrt(dx * dx + dz * dz);
                if (len > 0) {
                    entity.addVelocity((dx / len) * knockback * 0.5, 0.1, (dz / len) * knockback * 0.5);
                    entity.velocityChanged = true;
                }
            }

            if (hit) {
                applyEffects(entity);
            }
        }

        if (anyDamaged) {
            triggerCount++;
            ticksSinceLastTrigger = 0;
            triggeredEntities.add(triggerer.getEntityId());
            triggerFlashTick = ticksExisted;
            worldObj.setEntityState(this, TRIGGER_FLASH_STATUS);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // HAZARD LOGIC
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
            if (entity != owner && owner instanceof EntityLivingBase && !AbilityTargetHelper.shouldAffect((EntityLivingBase) owner, entity, TargetFilter.ENEMIES, false)) continue;
            if (!isInZone(entity)) continue;

            if (damagePerSecond > 0) {
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

        int previousHurtResistantTime = Ability.clearHurtResistanceIfNeeded(target, ignoreIFrames);
        try {
            if (owner instanceof EntityNPCInterface) {
                return target.attackEntityFrom(new NpcDamageSource("npc_ability", (EntityNPCInterface) owner), dmg);
            } else if (owner instanceof EntityPlayer) {
                return target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) owner), dmg);
            } else if (owner instanceof EntityLivingBase) {
                return target.attackEntityFrom(DamageSource.causeMobDamage((EntityLivingBase) owner), dmg);
            } else {
                return target.attackEntityFrom(new NpcDamageSource("npc_ability", null), dmg);
            }
        } finally {
            Ability.restoreHurtResistanceIfNeeded(target, ignoreIFrames, previousHurtResistantTime);
        }
    }

    private void applyEffects(EntityLivingBase entity) {
        if (entity == null || effects.isEmpty()) return;
        for (AbilityPotionEffect effect : effects) {
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
        return distance < 16384.0D;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
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

    public ZoneType getZoneType() {
        return zoneType;
    }

    public ZoneShape getShape() {
        return shape;
    }

    public float getRadius() {
        return radius;
    }

    public int getInnerColor() {
        return innerColor;
    }

    public int getOuterColor() {
        return outerColor;
    }

    public boolean isOuterColorEnabled() {
        return outerColorEnabled;
    }

    public float getZoneHeight() {
        return zoneHeight;
    }

    public boolean isArmed() {
        return armed || ticksExisted >= armTime;
    }

    public boolean isVisible() {
        return visible;
    }

    public int getTriggerFlashTick() {
        return triggerFlashTick;
    }

    public int getDurationTicks() {
        return durationTicks;
    }

    public int getMaxTicks() {
        return maxTicks;
    }

    public float getParticleDensity() {
        return particleDensity;
    }

    public float getParticleScale() {
        return particleScale;
    }

    public float getAnimSpeed() {
        return animSpeed;
    }

    public float getLightningDensity() {
        return lightningDensity;
    }

    public boolean isGroundFill() {
        return groundFill;
    }

    public float getGroundAlpha() {
        return groundAlpha;
    }

    public boolean isRings() {
        return rings;
    }

    public int getRingCount() {
        return ringCount;
    }

    public boolean isBorder() {
        return border;
    }

    public float getBorderSpeed() {
        return borderSpeed;
    }

    public boolean isAccents() {
        return accents;
    }

    public int getAccentStyle() {
        return accentStyle;
    }

    public boolean isLightning() {
        return lightning;
    }

    public boolean isParticles() {
        return particles;
    }

    public int getParticleMotion() {
        return particleMotion;
    }

    public String getParticleDir() {
        return particleDir;
    }

    public int getParticleSize() {
        return particleSize;
    }

    public boolean isParticleGlow() {
        return particleGlow;
    }

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

    // ═══════════════════════════════════════════════════════════════════
    // NBT SERIALIZATION
    // ═══════════════════════════════════════════════════════════════════

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        // Intentionally empty — ability entities are transient (not saved to world)
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        // Intentionally empty — ability entities are transient (not saved to world)
    }

    // ═══════════════════════════════════════════════════════════════════
    // SPAWN DATA (ByteBuf for client sync)
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public void writeSpawnData(ByteBuf buffer) {
        buffer.writeInt(zoneType.ordinal());
        ByteBufUtils.writeString(buffer, shape.name());
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

        // Visual layer fields
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
        ByteBufUtils.writeString(buffer, particleDir);
        buffer.writeInt(particleSize);
        buffer.writeBoolean(particleGlow);

        // Trap-specific
        buffer.writeInt(armTime);
        buffer.writeBoolean(visible);

        // Effects
        NBTTagCompound effectsNbt = new NBTTagCompound();
        NBTTagList effectList = new NBTTagList();
        for (AbilityPotionEffect effect : effects) {
            effectList.appendTag(effect.writeNBT());
        }
        effectsNbt.setTag("Effects", effectList);
        try {
            ByteBufUtils.writeNBT(buffer, effectsNbt);
        } catch (java.io.IOException e) {
            noppes.npcs.LogWriter.error("Error writing zone effects spawn data", e);
        }
    }

    @Override
    public void readSpawnData(ByteBuf buffer) {
        int zoneTypeOrd = buffer.readInt();
        this.zoneType = (zoneTypeOrd >= 0 && zoneTypeOrd < ZoneType.values().length) ? ZoneType.values()[zoneTypeOrd] : ZoneType.TRAP;
        String shapeName = ByteBufUtils.readString(buffer);
        try {
            this.shape = ZoneShape.valueOf(shapeName);
        } catch (Exception e) {
            this.shape = ZoneShape.CIRCLE;
        }
        this.ownerEntityId = buffer.readInt();
        this.radius = Math.max(0.1f, Math.min(100.0f, buffer.readFloat()));
        if (Float.isNaN(radius) || Float.isInfinite(radius)) radius = 5.0f;
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

        // Visual layer fields
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
        this.particleDir = ByteBufUtils.readString(buffer);
        this.particleSize = buffer.readInt();
        this.particleGlow = buffer.readBoolean();

        this.armTime = buffer.readInt();
        this.visible = buffer.readBoolean();

        // Effects
        NBTTagCompound effectsNbt = null;
        try {
            effectsNbt = ByteBufUtils.readNBT(buffer);
        } catch (java.io.IOException e) {
            noppes.npcs.LogWriter.error("Error reading zone effects spawn data", e);
        }
        effects.clear();
        if (effectsNbt != null && effectsNbt.hasKey("Effects")) {
            NBTTagList effectList = effectsNbt.getTagList("Effects", 10);
            for (int i = 0; i < effectList.tagCount(); i++) {
                AbilityPotionEffect effect = AbilityPotionEffect.fromNBT(effectList.getCompoundTagAt(i));
                if (effect != null && effect.isValid()) {
                    effects.add(effect);
                }
            }
        }
    }
}
