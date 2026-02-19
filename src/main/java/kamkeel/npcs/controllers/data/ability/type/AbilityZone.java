package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.AbilityPotionEffect;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.UserType;
import kamkeel.npcs.controllers.data.ability.data.EnergyDisplayData;
import kamkeel.npcs.controllers.data.telegraph.Telegraph;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.entity.EntityAbilityZone;
import kamkeel.npcs.entity.EntityAbilityZone.AccentStyle;
import kamkeel.npcs.entity.EntityAbilityZone.ParticleMotion;
import kamkeel.npcs.entity.EntityAbilityZone.ZoneShape;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import noppes.npcs.client.gui.SubGuiZonePresetSelector;
import noppes.npcs.client.gui.builder.FieldDef;
import noppes.npcs.constants.EnumPotionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public abstract class AbilityZone extends Ability {

    protected int durationTicks;
    protected ZoneShape zoneShape = ZoneShape.CIRCLE;
    protected float spawnRadius = 10.0f;
    protected int zoneCount = 3;
    protected float zoneHeight = 2.0f;

    protected float particleDensity = 1.0f;
    protected float particleScale = 1.0f;
    protected float animSpeed = 1.0f;
    protected float lightningDensity = 1.0f;
    protected EnergyDisplayData colorData;

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

    protected static final Random RANDOM = new Random();
    protected transient List<EntityAbilityZone> activeEntities = new ArrayList<>();
    protected transient List<double[]> preCalculatedPositions = new ArrayList<>();

    protected AbilityZone(int defaultDuration, EnergyDisplayData defaultColors) {
        this.durationTicks = defaultDuration;
        this.colorData = defaultColors;
        this.targetingMode = TargetingMode.AGGRO_TARGET;
        this.maxRange = 20.0f;
        this.lockMovement = LockMovementType.WINDUP;
        this.cooldownTicks = 0;
        this.allowedBy = UserType.NPC_ONLY;
    }

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

    public abstract float getZoneRadius();

    @Override
    public List<TelegraphInstance> createTelegraphs(EntityLivingBase caster, EntityLivingBase target) {
        if (!showTelegraph || windUpTicks <= 0) return new ArrayList<>();

        preCalculatedPositions.clear();
        List<double[]> placedPositions = new ArrayList<>();
        float minSeparation = getZoneRadius() * 2.0f;

        for (int i = 0; i < zoneCount; i++) {
            double[] pos = findSpawnPosition(caster, placedPositions, minSeparation);
            placedPositions.add(pos);
            preCalculatedPositions.add(pos);
        }

        List<TelegraphInstance> telegraphs = new ArrayList<>();
        float zoneRadius = getZoneRadius();

        for (double[] pos : preCalculatedPositions) {
            Telegraph telegraph;
            if (zoneShape == ZoneShape.SQUARE) {
                telegraph = Telegraph.square(zoneRadius);
            } else {
                telegraph = Telegraph.circle(zoneRadius);
            }

            telegraph.setDurationTicks(windUpTicks);
            telegraph.setColor(windUpColor);
            telegraph.setWarningColor(activeColor);
            telegraph.setWarningStartTick(Math.max(5, windUpTicks / 4));
            telegraph.setHeightOffset(telegraphHeightOffset);

            double groundY = findGroundLevel(caster.worldObj, pos[0], caster.posY, pos[1]);
            TelegraphInstance instance = new TelegraphInstance(telegraph, pos[0], groundY, pos[1], 0);
            instance.setCasterEntityId(caster.getEntityId());
            instance.setEntityIdToFollow(-1);
            telegraphs.add(instance);
        }

        return telegraphs;
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, int tick) {
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
        preCalculatedPositions.clear();
    }

    @Override
    public int getMaxPreviewDuration() {
        return durationTicks + 10;
    }

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

    protected void applyVisualToEntity(EntityAbilityZone entity) {
        entity.applyVisual(groundFill, groundAlpha,
            rings, ringCount, border, borderSpeed,
            accents, accentStyle, lightning,
            particles, particleMotion, particleDir,
            particleSize, particleGlow);
    }

    private void applyPresetDefaults(String styleName) {
        effects.clear();
        particleDir = "";
        switch (styleName) {
            case "TOXIC":
                groundFill = true;
                groundAlpha = 0.30f;
                rings = true;
                ringCount = 3;
                border = false;
                borderSpeed = 1.0f;
                accents = true;
                accentStyle = 1;
                lightning = false;
                particles = true;
                particleDensity = 1.5f;
                particleScale = 0.8f;
                particleMotion = 1;
                particleGlow = true;
                particleDir = "mc:mobSpell";
                colorData.innerColor = 0x44DD44;
                colorData.outerColor = 0x116611;
                windUpColor = 0x6044DD44;
                activeColor = 0xC044FF44;
                effects.add(new AbilityPotionEffect(EnumPotionType.Poison, 100, 0));
                break;
            case "INFERNO":
                groundFill = true;
                groundAlpha = 0.35f;
                rings = true;
                ringCount = 3;
                border = true;
                borderSpeed = 2.0f;
                accents = true;
                accentStyle = 2;
                lightning = false;
                particles = true;
                particleDensity = 2.0f;
                particleScale = 1.2f;
                particleMotion = 0;
                particleGlow = true;
                particleDir = "mc:flame";
                colorData.innerColor = 0xFF6611;
                colorData.outerColor = 0xCC2200;
                windUpColor = 0x60FF6611;
                activeColor = 0xC0FF4400;
                effects.add(new AbilityPotionEffect(EnumPotionType.Fire, 60, 0));
                break;
            case "ARCANE":
                groundFill = true;
                groundAlpha = 0.20f;
                rings = true;
                ringCount = 1;
                border = true;
                borderSpeed = 0.8f;
                accents = true;
                accentStyle = 0;
                lightning = false;
                particles = true;
                particleDensity = 1.0f;
                particleScale = 1.0f;
                particleMotion = 1;
                particleGlow = true;
                particleDir = "mc:portal";
                colorData.innerColor = 0xAA44FF;
                colorData.outerColor = 0x6622BB;
                windUpColor = 0x60AA44FF;
                activeColor = 0xC0CC66FF;
                effects.add(new AbilityPotionEffect(EnumPotionType.Weakness, 100, 0));
                break;
            case "ELECTRIC":
                groundFill = true;
                groundAlpha = 0.15f;
                rings = false;
                ringCount = 1;
                border = false;
                borderSpeed = 1.0f;
                accents = false;
                accentStyle = 0;
                lightning = true;
                particles = true;
                particleDensity = 0.8f;
                particleScale = 0.6f;
                particleMotion = 2;
                particleGlow = true;
                particleDir = "mc:enchantmenttable";
                colorData.innerColor = 0x4488FF;
                colorData.outerColor = 0x2244BB;
                windUpColor = 0x604488FF;
                activeColor = 0xC066AAFF;
                effects.add(new AbilityPotionEffect(EnumPotionType.MiningFatigue, 80, 1));
                break;
            case "FROST":
                groundFill = true;
                groundAlpha = 0.25f;
                rings = true;
                ringCount = 3;
                border = true;
                borderSpeed = 0.3f;
                accents = true;
                accentStyle = 0;
                lightning = false;
                particles = true;
                particleDensity = 1.5f;
                particleScale = 1.0f;
                particleMotion = 1;
                particleGlow = true;
                particleDir = "mc:snowshovel";
                colorData.innerColor = 0x88CCFF;
                colorData.outerColor = 0x4488CC;
                windUpColor = 0x6088CCFF;
                activeColor = 0xC0AADDFF;
                effects.add(new AbilityPotionEffect(EnumPotionType.Slowness, 100, 1));
                break;
            case "DEFAULT":
                groundFill = true;
                groundAlpha = 0.25f;
                rings = true;
                ringCount = 3;
                border = true;
                borderSpeed = 1.0f;
                accents = true;
                accentStyle = 0;
                lightning = false;
                particles = false;
                particleDensity = 1.0f;
                particleScale = 1.0f;
                particleMotion = 0;
                particleGlow = true;
                colorData.innerColor = 0xCCCCCC;
                colorData.outerColor = 0x666666;
                windUpColor = 0x60CCCCCC;
                activeColor = 0xC0FFFFFF;
                break;
            default:
                break;
        }
    }

    @SideOnly(Side.CLIENT)
    public enum ZonePreset {
        DEFAULT, TOXIC, INFERNO, ARCANE, ELECTRIC, FROST;

        @Override
        public String toString() {
            return "ability.preset." + name().toLowerCase();
        }
    }

    protected void writeZoneNBT(NBTTagCompound nbt) {
        nbt.setInteger("durationTicks", durationTicks);
        nbt.setString("zoneShape", zoneShape.name());
        nbt.setFloat("spawnRadius", spawnRadius);
        nbt.setInteger("zoneCount", zoneCount);
        nbt.setFloat("zoneHeight", zoneHeight);
        nbt.setFloat("particleDensity", particleDensity);
        nbt.setFloat("particleScale", particleScale);
        nbt.setFloat("animSpeed", animSpeed);
        nbt.setFloat("lightningDensity", lightningDensity);
        colorData.writeNBT(nbt);

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
        this.durationTicks = nbt.getInteger("durationTicks");
        try {
            this.zoneShape = ZoneShape.valueOf(nbt.getString("zoneShape"));
        } catch (Exception e) {
            this.zoneShape = ZoneShape.CIRCLE;
        }
        this.spawnRadius = nbt.getFloat("spawnRadius");
        this.zoneCount = nbt.getInteger("zoneCount");
        this.zoneHeight = nbt.getFloat("zoneHeight");
        this.particleDensity = nbt.getFloat("particleDensity");
        this.particleScale = nbt.getFloat("particleScale");
        this.animSpeed = nbt.getFloat("animSpeed");
        this.lightningDensity = nbt.getFloat("lightningDensity");
        colorData.readNBT(nbt);

        this.groundFill = nbt.getBoolean("groundFill");
        this.groundAlpha = nbt.getFloat("groundAlpha");
        this.rings = nbt.getBoolean("rings");
        this.ringCount = nbt.getInteger("ringCount");
        this.border = nbt.getBoolean("border");
        this.borderSpeed = nbt.getFloat("borderSpeed");
        this.accents = nbt.getBoolean("accents");
        this.accentStyle = nbt.getInteger("accentStyle");
        this.lightning = nbt.getBoolean("lightning");
        this.particles = nbt.getBoolean("particles");
        this.particleMotion = nbt.getInteger("particleMotion");
        this.particleDir = nbt.getString("particleDir");
        this.particleSize = nbt.getInteger("particleSize");
        this.particleGlow = nbt.getBoolean("particleGlow");
    }

    public int getDurationTicks() {
        return durationTicks;
    }

    public void setDurationTicks(int durationTicks) {
        this.durationTicks = Math.max(1, durationTicks);
    }

    public ZoneShape getZoneShapeEnum() {
        return zoneShape;
    }

    public void setZoneShapeEnum(ZoneShape shape) {
        this.zoneShape = shape;
    }

    public int getZoneShapeOrdinal() {
        return zoneShape.ordinal();
    }

    public void setZoneShapeOrdinal(int shape) {
        ZoneShape[] values = ZoneShape.values();
        this.zoneShape = shape >= 0 && shape < values.length ? values[shape] : ZoneShape.CIRCLE;
    }

    public float getSpawnRadius() {
        return spawnRadius;
    }

    public void setSpawnRadius(float spawnRadius) {
        this.spawnRadius = Math.max(0, spawnRadius);
    }

    public int getZoneCount() {
        return zoneCount;
    }

    public void setZoneCount(int zoneCount) {
        this.zoneCount = Math.max(1, zoneCount);
    }

    public float getZoneHeight() {
        return zoneHeight;
    }

    public void setZoneHeight(float zoneHeight) {
        this.zoneHeight = Math.max(0.5f, zoneHeight);
    }

    public float getParticleDensity() {
        return particleDensity;
    }

    public void setParticleDensity(float v) {
        this.particleDensity = Math.max(0, Math.min(5, v));
    }

    public float getParticleScale() {
        return particleScale;
    }

    public void setParticleScale(float v) {
        this.particleScale = Math.max(0.1f, Math.min(5, v));
    }

    public float getAnimSpeed() {
        return animSpeed;
    }

    public void setAnimSpeed(float v) {
        this.animSpeed = Math.max(0.1f, Math.min(5, v));
    }

    public float getLightningDensity() {
        return lightningDensity;
    }

    public void setLightningDensity(float v) {
        this.lightningDensity = Math.max(0, Math.min(3, v));
    }

    public int getInnerColor() {
        return colorData.innerColor;
    }

    public void setInnerColor(int color) {
        colorData.innerColor = color;
    }

    public int getOuterColor() {
        return colorData.outerColor;
    }

    public void setOuterColor(int color) {
        colorData.outerColor = color;
    }

    public boolean isOuterColorEnabled() {
        return colorData.outerColorEnabled;
    }

    public void setOuterColorEnabled(boolean enabled) {
        colorData.outerColorEnabled = enabled;
    }

    public float getInnerAlpha() {
        return colorData.innerAlpha;
    }

    public void setInnerAlpha(float alpha) {
        colorData.innerAlpha = alpha;
    }

    public float getOuterColorWidth() {
        return colorData.outerColorWidth;
    }

    public void setOuterColorWidth(float width) {
        colorData.outerColorWidth = width;
    }

    public float getOuterColorAlpha() {
        return colorData.outerColorAlpha;
    }

    public void setOuterColorAlpha(float alpha) {
        colorData.outerColorAlpha = alpha;
    }

    public boolean isGroundFill() {
        return groundFill;
    }

    public void setGroundFill(boolean v) {
        this.groundFill = v;
    }

    public float getGroundAlpha() {
        return groundAlpha;
    }

    public void setGroundAlpha(float v) {
        this.groundAlpha = Math.max(0, Math.min(1, v));
    }

    public boolean isRings() {
        return rings;
    }

    public void setRings(boolean v) {
        this.rings = v;
    }

    public int getRingCount() {
        return ringCount;
    }

    public void setRingCount(int v) {
        this.ringCount = Math.max(1, Math.min(5, v));
    }

    public boolean isBorder() {
        return border;
    }

    public void setBorder(boolean v) {
        this.border = v;
    }

    public float getBorderSpeed() {
        return borderSpeed;
    }

    public void setBorderSpeed(float v) {
        this.borderSpeed = Math.max(0.1f, Math.min(5, v));
    }

    public boolean isAccents() {
        return accents;
    }

    public void setAccents(boolean v) {
        this.accents = v;
    }

    public int getAccentStyle() {
        return accentStyle;
    }

    public void setAccentStyle(int v) {
        this.accentStyle = Math.max(0, Math.min(AccentStyle.values().length - 1, v));
    }

    public AccentStyle getAccentStyleEnum() {
        return AccentStyle.values()[Math.max(0, Math.min(accentStyle, AccentStyle.values().length - 1))];
    }

    public void setAccentStyleEnum(AccentStyle s) {
        this.accentStyle = s.ordinal();
    }

    public boolean isLightning() {
        return lightning;
    }

    public void setLightning(boolean v) {
        this.lightning = v;
    }

    public boolean isParticles() {
        return particles;
    }

    public void setParticles(boolean v) {
        this.particles = v;
    }

    public int getParticleMotion() {
        return particleMotion;
    }

    public void setParticleMotion(int v) {
        this.particleMotion = Math.max(0, Math.min(ParticleMotion.values().length - 1, v));
    }

    public ParticleMotion getParticleMotionEnum() {
        return ParticleMotion.values()[Math.max(0, Math.min(particleMotion, ParticleMotion.values().length - 1))];
    }

    public void setParticleMotionEnum(ParticleMotion m) {
        this.particleMotion = m.ordinal();
    }

    public String getParticleDir() {
        return particleDir;
    }

    public void setParticleDir(String v) {
        this.particleDir = v != null ? v : "";
    }

    public int getParticleSize() {
        return particleSize;
    }

    public void setParticleSize(int v) {
        this.particleSize = Math.max(1, Math.min(256, v));
    }

    public boolean isParticleGlow() {
        return particleGlow;
    }

    public void setParticleGlow(boolean v) {
        this.particleGlow = v;
    }

    @SideOnly(Side.CLIENT)
    protected void addPresetFieldDef(List<FieldDef> defs) {
        defs.add(FieldDef.subGuiField("gui.applyPreset",
            SubGuiZonePresetSelector::new,
            gui -> {
                SubGuiZonePresetSelector selector = (SubGuiZonePresetSelector) gui;
                if (selector.selectedPreset != null) {
                    applyPresetDefaults(selector.selectedPreset);
                }
            }));
    }

    @SideOnly(Side.CLIENT)
    protected void addVisualFieldDefs(List<FieldDef> defs) {
        defs.addAll(Arrays.asList(
            FieldDef.section("ability.section.ground").tab("ability.tab.visual"),
            FieldDef.row(
                FieldDef.boolField("gui.enabled", this::isGroundFill, this::setGroundFill),
                FieldDef.floatField("gui.alpha", this::getGroundAlpha, this::setGroundAlpha)
            ).tab("ability.tab.visual"),

            FieldDef.section("ability.section.rings").tab("ability.tab.visual"),
            FieldDef.row(
                FieldDef.boolField("gui.enabled", this::isRings, this::setRings),
                FieldDef.intField("gui.count", this::getRingCount, this::setRingCount).range(1, 5)
            ).tab("ability.tab.visual"),

            FieldDef.section("ability.section.border").tab("ability.tab.visual"),
            FieldDef.row(
                FieldDef.boolField("gui.enabled", this::isBorder, this::setBorder),
                FieldDef.floatField("gui.speed", this::getBorderSpeed, this::setBorderSpeed)
            ).tab("ability.tab.visual"),

            FieldDef.section("ability.section.accents").tab("ability.tab.visual"),
            FieldDef.row(
                FieldDef.boolField("gui.enabled", this::isAccents, this::setAccents),
                FieldDef.enumField("gui.style", AccentStyle.class, this::getAccentStyleEnum, this::setAccentStyleEnum)
            ).tab("ability.tab.visual"),

            FieldDef.section("ability.section.lightning").tab("ability.tab.visual"),
            FieldDef.row(
                FieldDef.boolField("gui.enabled", this::isLightning, this::setLightning),
                FieldDef.floatField("gui.density", this::getLightningDensity, this::setLightningDensity)
            ).tab("ability.tab.visual"),

            FieldDef.section("ability.section.particles").tab("ability.tab.visual"),
            FieldDef.boolField("gui.enabled", this::isParticles, this::setParticles)
                .tab("ability.tab.visual"),
            FieldDef.row(
                FieldDef.floatField("gui.density", this::getParticleDensity, this::setParticleDensity),
                FieldDef.floatField("gui.scale", this::getParticleScale, this::setParticleScale)
            ).tab("ability.tab.visual").visibleWhen(this::isParticles),
            FieldDef.row(
                FieldDef.enumField("gui.motion", ParticleMotion.class, this::getParticleMotionEnum, this::setParticleMotionEnum),
                FieldDef.boolField("gui.glow", this::isParticleGlow, this::setParticleGlow)
            ).tab("ability.tab.visual").visibleWhen(this::isParticles),
            FieldDef.stringField("gui.texture", this::getParticleDir, this::setParticleDir)
                .tab("ability.tab.visual").visibleWhen(this::isParticles),

            FieldDef.section("ability.section.animation").tab("ability.tab.visual"),
            FieldDef.floatField("gui.speed", this::getAnimSpeed, this::setAnimSpeed)
                .tab("ability.tab.visual"),

            FieldDef.section("ability.section.colors").tab("ability.tab.visual"),
            FieldDef.row(
                FieldDef.colorSubGui("ability.innerColor", this::getInnerColor, this::setInnerColor),
                FieldDef.floatField("ability.innerAlpha", this::getInnerAlpha, this::setInnerAlpha).range(0, 1)
            ).tab("ability.tab.visual"),
            FieldDef.boolField("ability.outerEnabled", this::isOuterColorEnabled, this::setOuterColorEnabled)
                .tab("ability.tab.visual"),
            FieldDef.colorSubGui("ability.outerColor", this::getOuterColor, this::setOuterColor)
                .tab("ability.tab.visual").visibleWhen(this::isOuterColorEnabled),
            FieldDef.row(
                FieldDef.floatField("ability.outerWidth", this::getOuterColorWidth, this::setOuterColorWidth)
                    .visibleWhen(this::isOuterColorEnabled),
                FieldDef.floatField("ability.outerAlpha", this::getOuterColorAlpha, this::setOuterColorAlpha)
                    .range(0, 1).visibleWhen(this::isOuterColorEnabled)
            ).tab("ability.tab.visual")
        ));
    }

}
