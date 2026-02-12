package kamkeel.npcs.controllers.data.ability.type;

import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.UserType;
import kamkeel.npcs.controllers.data.ability.data.EnergyDisplayData;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import kamkeel.npcs.entity.EntityAbilityZone;
import kamkeel.npcs.entity.EntityAbilityZone.AccentStyle;
import kamkeel.npcs.entity.EntityAbilityZone.ParticleMotion;
import kamkeel.npcs.entity.EntityAbilityZone.ZoneShape;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import noppes.npcs.client.gui.builder.FieldDef;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Abstract base class for zone-based abilities (Trap, Hazard).
 * Extracts shared fields: duration, shape, spawn, visual, lifecycle, and FieldDefs.
 */
public abstract class AbilityZone extends Ability {

    // Zone properties
    protected int durationTicks;
    protected ZoneShape zoneShape = ZoneShape.CIRCLE;
    protected float spawnRadius = 5.0f;
    protected float telegraphSize = 5.0f;
    protected int zoneCount = 1;
    protected float zoneHeight = 2.0f;

    // Visual parameters
    protected float particleDensity = 1.0f;
    protected float particleScale = 1.0f;
    protected float animSpeed = 1.0f;
    protected float lightningDensity = 1.0f;
    protected EnergyDisplayData colorData;

    // Visual layer fields (always active)
    protected boolean groundFill = true;
    protected float groundAlpha = 0.25f;
    protected boolean rings = true;
    protected int ringCount = 3;
    protected boolean border = true;
    protected float borderSpeed = 1.0f;
    protected boolean accents = true;
    protected int accentStyle = 0;
    protected boolean lightning = false;
    protected boolean particles = true;
    protected int particleMotion = 0;
    protected String particleDir = "";
    protected int particleSize = 32;
    protected boolean particleGlow = true;

    // Runtime state
    protected static final Random RANDOM = new Random();
    protected transient List<EntityAbilityZone> activeEntities = new ArrayList<>();

    protected AbilityZone(int defaultDuration, EnergyDisplayData defaultColors) {
        this.durationTicks = defaultDuration;
        this.colorData = defaultColors;
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 15.0f;
        this.lockMovement = LockMovementType.WINDUP;
        this.cooldownTicks = 0;
        this.telegraphType = TelegraphType.CIRCLE;
        this.allowedBy = UserType.NPC_ONLY;
    }

    // ═════════════════════════════════════════════════════════════════
    // SHARED OVERRIDES
    // ═════════════════════════════════════════════════════════════════

    @Override
    public boolean allowBurst() {
        return false;
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
    public float getTelegraphRadius() {
        return telegraphSize;
    }

    @Override
    public TelegraphInstance createTelegraph(EntityLivingBase caster, EntityLivingBase target) {
        TelegraphInstance instance = super.createTelegraph(caster, target);
        if (instance == null) return null;

        // Always show telegraph at caster position
        instance.setEntityIdToFollow(-1);
        instance.setX(caster.posX);
        instance.setY(caster.posY);
        instance.setZ(caster.posZ);
        return instance;
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        // Remove dead entities
        Iterator<EntityAbilityZone> it = activeEntities.iterator();
        while (it.hasNext()) {
            EntityAbilityZone entity = it.next();
            if (entity == null || entity.isDead) {
                it.remove();
            }
        }

        if (activeEntities.isEmpty()) {
            signalCompletion();
        }
    }

    @Override
    public void onComplete(EntityLivingBase caster, EntityLivingBase target) {
        cleanup();
    }

    @Override
    public void onInterrupt(EntityLivingBase caster, DamageSource source, float damage) {
        cleanup();
    }

    @Override
    public void cleanup() {
        for (EntityAbilityZone entity : activeEntities) {
            if (entity != null && !entity.isDead) {
                killAbilityEntity(entity);
            }
        }
        activeEntities.clear();
    }

    @Override
    public int getMaxPreviewDuration() {
        return durationTicks + 10;
    }

    // ═════════════════════════════════════════════════════════════════
    // SHARED SPAWN HELPER
    // ═════════════════════════════════════════════════════════════════

    /**
     * Find a non-overlapping spawn position within spawnRadius of the caster.
     */
    protected double[] findSpawnPosition(EntityLivingBase caster, List<double[]> placedPositions, float minSeparation) {
        double spawnX = caster.posX;
        double spawnZ = caster.posZ;

        for (int attempt = 0; attempt < 15; attempt++) {
            double angle = RANDOM.nextDouble() * Math.PI * 2;
            double dist = Math.sqrt(RANDOM.nextDouble()) * spawnRadius;
            double candidateX = caster.posX + Math.cos(angle) * dist;
            double candidateZ = caster.posZ + Math.sin(angle) * dist;

            boolean overlaps = false;
            for (double[] placed : placedPositions) {
                double dx = candidateX - placed[0];
                double dz = candidateZ - placed[1];
                if (Math.sqrt(dx * dx + dz * dz) < minSeparation) {
                    overlaps = true;
                    break;
                }
            }

            spawnX = candidateX;
            spawnZ = candidateZ;
            if (!overlaps) break;
        }

        return new double[]{spawnX, spawnZ};
    }

    /**
     * Apply visual settings to a zone entity.
     */
    protected void applyVisualToEntity(EntityAbilityZone entity) {
        entity.applyVisual(groundFill, groundAlpha,
            rings, ringCount, border, borderSpeed,
            accents, accentStyle, lightning,
            particles, particleMotion, particleDir,
            particleSize, particleGlow);
    }

    // ═════════════════════════════════════════════════════════════════
    // PRESET SUPPORT
    // ═════════════════════════════════════════════════════════════════

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
            case "DEFAULT":
                groundFill = true; groundAlpha = 0.25f; rings = true; ringCount = 3;
                border = true; borderSpeed = 1.0f; accents = true; accentStyle = 0;
                lightning = false; particles = false; particleMotion = 0; particleGlow = true;
                break;
            default:
                break;
        }
    }

    /**
     * Client-only preset enum for the GUI dropdown.
     * Not persisted — presets simply set field values.
     */
    @SideOnly(Side.CLIENT)
    public enum ZonePreset {
        CUSTOM, DEFAULT, TOXIC, INFERNO, ARCANE, ELECTRIC, FROST;

        @Override
        public String toString() {
            return "ability.preset." + name().toLowerCase();
        }
    }

    /**
     * Check current field values against each preset, return matching preset or CUSTOM.
     */
    @SideOnly(Side.CLIENT)
    public ZonePreset getCurrentPreset() {
        if (matchesPreset(true, 0.25f, true, 3, true, 1.0f, true, 0, false, false, 0, true))
            return ZonePreset.DEFAULT;
        if (matchesPreset(true, 0.30f, true, 3, false, 1.0f, true, 1, false, true, 0, true))
            return ZonePreset.TOXIC;
        if (matchesPreset(true, 0.35f, true, 3, true, 2.0f, true, 2, false, true, 0, true))
            return ZonePreset.INFERNO;
        if (matchesPreset(true, 0.20f, true, 1, true, 0.8f, true, 0, false, true, 1, true))
            return ZonePreset.ARCANE;
        if (matchesPreset(true, 0.15f, false, 1, false, 1.0f, false, 0, true, true, 2, true))
            return ZonePreset.ELECTRIC;
        if (matchesPreset(true, 0.25f, true, 3, true, 0.3f, true, 0, false, true, 1, true))
            return ZonePreset.FROST;
        return ZonePreset.CUSTOM;
    }

    @SideOnly(Side.CLIENT)
    private boolean matchesPreset(boolean gf, float ga, boolean r, int rc, boolean b, float bs,
                                   boolean a, int as, boolean l, boolean p, int pm, boolean pg) {
        return groundFill == gf && Math.abs(groundAlpha - ga) < 0.001f
            && rings == r && ringCount == rc && border == b && Math.abs(borderSpeed - bs) < 0.001f
            && accents == a && accentStyle == as && lightning == l
            && particles == p && particleMotion == pm && particleGlow == pg;
    }

    /**
     * Apply a preset's field values. CUSTOM does nothing (user configures manually).
     */
    @SideOnly(Side.CLIENT)
    public void applyPreset(ZonePreset preset) {
        if (preset == ZonePreset.CUSTOM) return;
        applyPresetDefaults(preset.name());
    }

    // ═════════════════════════════════════════════════════════════════
    // SHARED NBT
    // ═════════════════════════════════════════════════════════════════

    protected void writeZoneNBT(NBTTagCompound nbt) {
        nbt.setInteger("durationTicks", durationTicks);
        nbt.setString("zoneShape", zoneShape.name());
        nbt.setFloat("spawnRadius", spawnRadius);
        nbt.setFloat("telegraphSize", telegraphSize);
        nbt.setInteger("zoneCount", zoneCount);
        nbt.setFloat("zoneHeight", zoneHeight);
        nbt.setFloat("particleDensity", particleDensity);
        nbt.setFloat("particleScale", particleScale);
        nbt.setFloat("animSpeed", animSpeed);
        nbt.setFloat("lightningDensity", lightningDensity);
        colorData.writeNBT(nbt);

        // Visual layer fields — always written as top-level keys
        nbt.setBoolean("groundFill", groundFill);
        nbt.setFloat("groundAlpha", groundAlpha);
        nbt.setBoolean("rings", rings);
        nbt.setInteger("ringCount", ringCount);
        nbt.setBoolean("border", border);
        nbt.setFloat("borderSpeed", borderSpeed);
        nbt.setBoolean("accents", accents);
        nbt.setInteger("accentStyle", accentStyle);
        nbt.setBoolean("lightning", lightning);
        nbt.setBoolean("particles", particles);
        nbt.setInteger("particleMotion", particleMotion);
        nbt.setString("particleDir", particleDir);
        nbt.setInteger("particleSize", particleSize);
        nbt.setBoolean("particleGlow", particleGlow);
    }

    protected void readZoneNBT(NBTTagCompound nbt, int defaultDuration) {
        this.durationTicks = nbt.hasKey("durationTicks") ? nbt.getInteger("durationTicks") : defaultDuration;
        if (nbt.hasKey("zoneShape")) {
            try { this.zoneShape = ZoneShape.valueOf(nbt.getString("zoneShape")); }
            catch (Exception e) { this.zoneShape = ZoneShape.CIRCLE; }
        } else {
            this.zoneShape = ZoneShape.CIRCLE;
        }
        this.spawnRadius = nbt.hasKey("spawnRadius") ? nbt.getFloat("spawnRadius") : 5.0f;
        this.telegraphSize = nbt.hasKey("telegraphSize") ? nbt.getFloat("telegraphSize") : spawnRadius;
        this.zoneCount = nbt.hasKey("zoneCount") ? nbt.getInteger("zoneCount") : 1;
        this.zoneHeight = nbt.hasKey("zoneHeight") ? nbt.getFloat("zoneHeight") : 2.0f;
        this.particleDensity = nbt.hasKey("particleDensity") ? nbt.getFloat("particleDensity") : 1.0f;
        this.particleScale = nbt.hasKey("particleScale") ? nbt.getFloat("particleScale") : 1.0f;
        this.animSpeed = nbt.hasKey("animSpeed") ? nbt.getFloat("animSpeed") : 1.0f;
        this.lightningDensity = nbt.hasKey("lightningDensity") ? nbt.getFloat("lightningDensity") : 1.0f;
        colorData.readNBT(nbt);

        // Visual fields — backward compat with old zoneStyle format
        if (nbt.hasKey("zoneStyle")) {
            // OLD FORMAT: apply preset defaults, then overlay CustomVisual
            applyPresetDefaults(nbt.getString("zoneStyle"));
            if (nbt.hasKey("CustomVisual")) {
                NBTTagCompound custom = nbt.getCompoundTag("CustomVisual");
                this.groundFill = !custom.hasKey("groundFill") || custom.getBoolean("groundFill");
                this.groundAlpha = custom.hasKey("groundAlpha") ? custom.getFloat("groundAlpha") : 0.25f;
                this.rings = !custom.hasKey("rings") || custom.getBoolean("rings");
                this.ringCount = custom.hasKey("ringCount") ? custom.getInteger("ringCount") : 3;
                this.border = !custom.hasKey("border") || custom.getBoolean("border");
                this.borderSpeed = custom.hasKey("borderSpeed") ? custom.getFloat("borderSpeed") : 1.0f;
                this.accents = !custom.hasKey("accents") || custom.getBoolean("accents");
                this.accentStyle = custom.hasKey("accentStyle") ? custom.getInteger("accentStyle") : 0;
                this.lightning = custom.hasKey("lightning") && custom.getBoolean("lightning");
                this.particles = !custom.hasKey("particles") || custom.getBoolean("particles");
                this.particleMotion = custom.hasKey("particleMotion") ? custom.getInteger("particleMotion") : 0;
                this.particleDir = custom.hasKey("particleDir") ? custom.getString("particleDir") : "";
                this.particleSize = custom.hasKey("particleSize") ? custom.getInteger("particleSize") : 32;
                this.particleGlow = !custom.hasKey("particleGlow") || custom.getBoolean("particleGlow");
            }
        } else {
            // NEW FORMAT: read individual top-level keys
            this.groundFill = !nbt.hasKey("groundFill") || nbt.getBoolean("groundFill");
            this.groundAlpha = nbt.hasKey("groundAlpha") ? nbt.getFloat("groundAlpha") : 0.25f;
            this.rings = !nbt.hasKey("rings") || nbt.getBoolean("rings");
            this.ringCount = nbt.hasKey("ringCount") ? nbt.getInteger("ringCount") : 3;
            this.border = !nbt.hasKey("border") || nbt.getBoolean("border");
            this.borderSpeed = nbt.hasKey("borderSpeed") ? nbt.getFloat("borderSpeed") : 1.0f;
            this.accents = !nbt.hasKey("accents") || nbt.getBoolean("accents");
            this.accentStyle = nbt.hasKey("accentStyle") ? nbt.getInteger("accentStyle") : 0;
            this.lightning = nbt.hasKey("lightning") && nbt.getBoolean("lightning");
            this.particles = !nbt.hasKey("particles") || nbt.getBoolean("particles");
            this.particleMotion = nbt.hasKey("particleMotion") ? nbt.getInteger("particleMotion") : 0;
            this.particleDir = nbt.hasKey("particleDir") ? nbt.getString("particleDir") : "";
            this.particleSize = nbt.hasKey("particleSize") ? nbt.getInteger("particleSize") : 32;
            this.particleGlow = !nbt.hasKey("particleGlow") || nbt.getBoolean("particleGlow");
        }
    }

    // ═════════════════════════════════════════════════════════════════
    // SHARED GETTERS & SETTERS
    // ═════════════════════════════════════════════════════════════════

    public int getDurationTicks() { return durationTicks; }
    public void setDurationTicks(int durationTicks) { this.durationTicks = Math.max(1, durationTicks); }

    public ZoneShape getZoneShapeEnum() { return zoneShape; }
    public void setZoneShapeEnum(ZoneShape shape) { this.zoneShape = shape; }

    public int getZoneShapeOrdinal() { return zoneShape.ordinal(); }
    public void setZoneShapeOrdinal(int shape) {
        ZoneShape[] values = ZoneShape.values();
        this.zoneShape = shape >= 0 && shape < values.length ? values[shape] : ZoneShape.CIRCLE;
    }

    public float getSpawnRadius() { return spawnRadius; }
    public void setSpawnRadius(float spawnRadius) { this.spawnRadius = Math.max(0, spawnRadius); }
    public float getTelegraphSize() { return telegraphSize; }
    public void setTelegraphSize(float telegraphSize) { this.telegraphSize = Math.max(0, telegraphSize); }
    public int getZoneCount() { return zoneCount; }
    public void setZoneCount(int zoneCount) { this.zoneCount = Math.max(1, zoneCount); }
    public float getZoneHeight() { return zoneHeight; }
    public void setZoneHeight(float zoneHeight) { this.zoneHeight = Math.max(0.5f, zoneHeight); }

    public float getParticleDensity() { return particleDensity; }
    public void setParticleDensity(float v) { this.particleDensity = Math.max(0, Math.min(5, v)); }
    public float getParticleScale() { return particleScale; }
    public void setParticleScale(float v) { this.particleScale = Math.max(0.1f, Math.min(5, v)); }
    public float getAnimSpeed() { return animSpeed; }
    public void setAnimSpeed(float v) { this.animSpeed = Math.max(0.1f, Math.min(5, v)); }
    public float getLightningDensity() { return lightningDensity; }
    public void setLightningDensity(float v) { this.lightningDensity = Math.max(0, Math.min(3, v)); }

    // Color getters/setters
    public int getInnerColor() { return colorData.innerColor; }
    public void setInnerColor(int color) { colorData.innerColor = color; }
    public int getOuterColor() { return colorData.outerColor; }
    public void setOuterColor(int color) { colorData.outerColor = color; }
    public boolean isOuterColorEnabled() { return colorData.outerColorEnabled; }
    public void setOuterColorEnabled(boolean enabled) { colorData.outerColorEnabled = enabled; }

    // Visual layer getters/setters
    public boolean isGroundFill() { return groundFill; }
    public void setGroundFill(boolean v) { this.groundFill = v; }
    public float getGroundAlpha() { return groundAlpha; }
    public void setGroundAlpha(float v) { this.groundAlpha = Math.max(0, Math.min(1, v)); }
    public boolean isRings() { return rings; }
    public void setRings(boolean v) { this.rings = v; }
    public int getRingCount() { return ringCount; }
    public void setRingCount(int v) { this.ringCount = Math.max(1, Math.min(5, v)); }
    public boolean isBorder() { return border; }
    public void setBorder(boolean v) { this.border = v; }
    public float getBorderSpeed() { return borderSpeed; }
    public void setBorderSpeed(float v) { this.borderSpeed = Math.max(0.1f, Math.min(5, v)); }
    public boolean isAccents() { return accents; }
    public void setAccents(boolean v) { this.accents = v; }
    public int getAccentStyle() { return accentStyle; }
    public void setAccentStyle(int v) { this.accentStyle = Math.max(0, Math.min(AccentStyle.values().length - 1, v)); }
    public AccentStyle getAccentStyleEnum() { return AccentStyle.values()[Math.max(0, Math.min(accentStyle, AccentStyle.values().length - 1))]; }
    public void setAccentStyleEnum(AccentStyle s) { this.accentStyle = s.ordinal(); }
    public boolean isLightning() { return lightning; }
    public void setLightning(boolean v) { this.lightning = v; }
    public boolean isParticles() { return particles; }
    public void setParticles(boolean v) { this.particles = v; }
    public int getParticleMotion() { return particleMotion; }
    public void setParticleMotion(int v) { this.particleMotion = Math.max(0, Math.min(ParticleMotion.values().length - 1, v)); }
    public ParticleMotion getParticleMotionEnum() { return ParticleMotion.values()[Math.max(0, Math.min(particleMotion, ParticleMotion.values().length - 1))]; }
    public void setParticleMotionEnum(ParticleMotion m) { this.particleMotion = m.ordinal(); }
    public String getParticleDir() { return particleDir; }
    public void setParticleDir(String v) { this.particleDir = v != null ? v : ""; }
    public int getParticleSize() { return particleSize; }
    public void setParticleSize(int v) { this.particleSize = Math.max(1, Math.min(256, v)); }
    public boolean isParticleGlow() { return particleGlow; }
    public void setParticleGlow(boolean v) { this.particleGlow = v; }

    // ═════════════════════════════════════════════════════════════════
    // SHARED FIELD DEFINITIONS (client-only)
    // ═════════════════════════════════════════════════════════════════

    @SideOnly(Side.CLIENT)
    protected void addVisualFieldDefs(List<FieldDef> defs) {
        defs.addAll(Arrays.asList(
            // Preset dropdown
            FieldDef.enumField("gui.preset", ZonePreset.class, this::getCurrentPreset, this::applyPreset)
                .tab("ability.tab.visual"),

            // Ground Fill
            FieldDef.section("ability.section.ground").tab("ability.tab.visual"),
            FieldDef.row(
                FieldDef.boolField("gui.enabled", this::isGroundFill, this::setGroundFill),
                FieldDef.floatField("gui.alpha", this::getGroundAlpha, this::setGroundAlpha)
            ).tab("ability.tab.visual"),

            // Rings
            FieldDef.section("ability.section.rings").tab("ability.tab.visual"),
            FieldDef.row(
                FieldDef.boolField("gui.enabled", this::isRings, this::setRings),
                FieldDef.intField("gui.count", this::getRingCount, this::setRingCount).range(1, 5)
            ).tab("ability.tab.visual"),

            // Border
            FieldDef.section("ability.section.border").tab("ability.tab.visual"),
            FieldDef.row(
                FieldDef.boolField("gui.enabled", this::isBorder, this::setBorder),
                FieldDef.floatField("gui.speed", this::getBorderSpeed, this::setBorderSpeed)
            ).tab("ability.tab.visual"),

            // Accents
            FieldDef.section("ability.section.accents").tab("ability.tab.visual"),
            FieldDef.row(
                FieldDef.boolField("gui.enabled", this::isAccents, this::setAccents),
                FieldDef.enumField("gui.style", AccentStyle.class, this::getAccentStyleEnum, this::setAccentStyleEnum)
            ).tab("ability.tab.visual"),

            // Lightning
            FieldDef.section("ability.section.lightning").tab("ability.tab.visual"),
            FieldDef.row(
                FieldDef.boolField("gui.enabled", this::isLightning, this::setLightning),
                FieldDef.floatField("gui.density", this::getLightningDensity, this::setLightningDensity)
            ).tab("ability.tab.visual"),

            // Particles
            FieldDef.section("ability.section.particles").tab("ability.tab.visual"),
            FieldDef.row(
                FieldDef.boolField("gui.enabled", this::isParticles, this::setParticles),
                FieldDef.floatField("gui.density", this::getParticleDensity, this::setParticleDensity)
            ).tab("ability.tab.visual"),
            FieldDef.row(
                FieldDef.floatField("gui.scale", this::getParticleScale, this::setParticleScale),
                FieldDef.enumField("gui.motion", ParticleMotion.class, this::getParticleMotionEnum, this::setParticleMotionEnum)
            ).tab("ability.tab.visual"),
            FieldDef.boolField("gui.glow", this::isParticleGlow, this::setParticleGlow)
                .tab("ability.tab.visual"),
            FieldDef.stringField("gui.texture", this::getParticleDir, this::setParticleDir)
                .tab("ability.tab.visual").visibleWhen(this::isParticles),
            FieldDef.intField("gui.size", this::getParticleSize, this::setParticleSize).range(1, 256)
                .tab("ability.tab.visual").visibleWhen(this::isParticles),

            // Animation
            FieldDef.section("ability.section.animation").tab("ability.tab.visual"),
            FieldDef.floatField("gui.speed", this::getAnimSpeed, this::setAnimSpeed)
                .tab("ability.tab.visual"),

            // Colors
            FieldDef.section("ability.section.colors").tab("ability.tab.visual"),
            FieldDef.colorSubGui("ability.innerColor", this::getInnerColor, this::setInnerColor)
                .tab("ability.tab.visual"),
            FieldDef.boolField("ability.outerEnabled", this::isOuterColorEnabled, this::setOuterColorEnabled)
                .tab("ability.tab.visual"),
            FieldDef.colorSubGui("ability.outerColor", this::getOuterColor, this::setOuterColor)
                .tab("ability.tab.visual").visibleWhen(this::isOuterColorEnabled)
        ));
    }

    @SideOnly(Side.CLIENT)
    protected void addTelegraphSizeField(List<FieldDef> defs) {
        defs.add(FieldDef.floatField("gui.size", this::getTelegraphSize, this::setTelegraphSize)
            .tab("ability.tab.effects"));
    }
}
