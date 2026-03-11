package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.data.effect.AbilityPotionEffect;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldDefs;
import kamkeel.npcs.entity.EntityAbilityZone;
import kamkeel.npcs.entity.EntityAbilityZone.ZoneShape;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.ability.type.IAbilityTrap;
import noppes.npcs.client.gui.SubGuiTrapPresetSelector;
import noppes.npcs.client.gui.builder.FieldDef;
import noppes.npcs.constants.EnumPotionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AbilityTrap extends AbilityZone implements IAbilityTrap {

    private float triggerRadius = 2.0f;
    private int armTime = 20;
    private int maxTriggers = 1;
    private int triggerCooldown = 20;
    private float damage = 6.0f;
    private float damageRadius = 0.0f;
    private float knockback = 0.5f;
    private boolean visible = true;

    public AbilityTrap() {
        super(300, new EnergyDisplayData(0xFF6600, 0xFF0000, true, 1.0f, 0.5f, 1.5f));
        this.typeId = "ability.cnpc.trap";
        this.name = "Trap";
        this.windUpTicks = 30;
        this.windUpAnimationName = "Ability_Zone_Windup";
        this.activeAnimationName = "Ability_Zone_Active";

        this.defaultIconLayers = new DefaultIconLayer[]{
            new DefaultIconLayer("customnpcs:textures/gui/ability/trap.png",
                this::getActiveColor)
        };
    }

    @Override
    public boolean isConcurrentCapable() {
        return true;
    }

    @Override
    public float getZoneRadius() {
        return triggerRadius;
    }

    @Override
    public void onExecute(EntityLivingBase caster, EntityLivingBase target) {
        activeEntities.clear();

        List<double[]> positions;
        if (!preCalculatedPositions.isEmpty() && preCalculatedPositions.size() == zoneCount) {
            positions = new ArrayList<>(preCalculatedPositions);
            preCalculatedPositions.clear();
        } else {
            positions = new ArrayList<>();
            List<double[]> placedPositions = new ArrayList<>();
            float minSeparation = triggerRadius * 2.0f;
            for (int i = 0; i < zoneCount; i++) {
                double[] pos = findSpawnPosition(caster, placedPositions, minSeparation);
                placedPositions.add(pos);
                positions.add(pos);
            }
        }

        for (int i = 0; i < zoneCount; i++) {
            double[] pos = positions.get(i);

            EntityAbilityZone entity = EntityAbilityZone.createTrap(caster.worldObj, caster,
                pos[0], caster.posY, pos[1],
                zoneShape,
                triggerRadius, armTime, maxTriggers, triggerCooldown,
                damage, damageRadius, knockback, durationTicks,
                isIgnoreIFrames(),
                colorData.innerColor, colorData.outerColor, colorData.outerColorEnabled,
                zoneHeight,
                particleDensity, particleScale, animSpeed, lightningDensity,
                visible,
                getEffects());
            entity.setSourceAbility(this);

            applyVisualToEntity(entity);

            if (isPreview()) {
                entity.setupPreview(caster);
            }

            spawnAbilityEntity(entity);
            activeEntities.add(entity);
        }
    }

    @SideOnly(Side.CLIENT)
    public enum TrapPreset {
        HIDDEN, VENOM, EXPLOSIVE, CURSED, SHOCK, SNARE;

        @Override
        public String toString() {
            return "ability.preset." + name().toLowerCase();
        }
    }

    private void applyTrapPresetDefaults(String preset) {
        effects.clear();
        particleDir = "";

        groundFill = true;
        rings = false;
        ringCount = 1;
        border = false;
        borderSpeed = 1.0f;
        accents = false;
        accentStyle = 0;
        lightning = false;
        particleGlow = false;

        switch (preset) {
            case "VENOM":
                groundAlpha = 0.04f;
                particles = true;
                particleDensity = 0.3f;
                particleScale = 0.5f;
                particleMotion = 1;
                particleDir = "mc:mobSpell";
                colorData.innerColor = 0x44DD44;
                colorData.outerColor = 0x116611;
                windUpColor = 0x6044DD44;
                activeColor = 0xC044FF44;
                effects.add(new AbilityPotionEffect(EnumPotionType.Poison, 100, 0));
                break;
            case "EXPLOSIVE":
                groundAlpha = 0.04f;
                particles = true;
                particleDensity = 0.3f;
                particleScale = 0.6f;
                particleMotion = 0;
                particleDir = "mc:smoke";
                colorData.innerColor = 0xFF6611;
                colorData.outerColor = 0xCC2200;
                windUpColor = 0x60FF6611;
                activeColor = 0xC0FF4400;
                effects.add(new AbilityPotionEffect(EnumPotionType.Fire, 60, 0));
                break;
            case "CURSED":
                groundAlpha = 0.04f;
                particles = true;
                particleDensity = 0.3f;
                particleScale = 0.5f;
                particleMotion = 1;
                particleDir = "mc:portal";
                colorData.innerColor = 0xAA44FF;
                colorData.outerColor = 0x6622BB;
                windUpColor = 0x60AA44FF;
                activeColor = 0xC0CC66FF;
                effects.add(new AbilityPotionEffect(EnumPotionType.Weakness, 100, 0));
                break;
            case "SHOCK":
                groundAlpha = 0.04f;
                particles = true;
                particleDensity = 0.3f;
                particleScale = 0.4f;
                particleMotion = 2;
                particleDir = "mc:enchantmenttable";
                colorData.innerColor = 0x4488FF;
                colorData.outerColor = 0x2244BB;
                windUpColor = 0x604488FF;
                activeColor = 0xC066AAFF;
                effects.add(new AbilityPotionEffect(EnumPotionType.MiningFatigue, 80, 1));
                break;
            case "SNARE":
                groundAlpha = 0.04f;
                particles = true;
                particleDensity = 0.3f;
                particleScale = 0.5f;
                particleMotion = 1;
                particleDir = "mc:snowshovel";
                colorData.innerColor = 0x88CCFF;
                colorData.outerColor = 0x4488CC;
                windUpColor = 0x6088CCFF;
                activeColor = 0xC0AADDFF;
                effects.add(new AbilityPotionEffect(EnumPotionType.Slowness, 100, 1));
                break;
            case "HIDDEN":
            default:
                groundAlpha = 0.03f;
                particles = false;
                particleDensity = 0.0f;
                particleScale = 1.0f;
                particleMotion = 0;
                colorData.innerColor = 0x888888;
                colorData.outerColor = 0x444444;
                windUpColor = 0x60888888;
                activeColor = 0xC0AAAAAA;
                break;
        }
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        writeZoneNBT(nbt);
        nbt.setFloat("triggerRadius", triggerRadius);
        nbt.setInteger("armTime", armTime);
        nbt.setInteger("maxTriggers", maxTriggers);
        nbt.setInteger("triggerCooldown", triggerCooldown);
        nbt.setFloat("damage", damage);
        nbt.setFloat("damageRadius", damageRadius);
        nbt.setFloat("knockback", knockback);
        nbt.setBoolean("visible", visible);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        readZoneNBT(nbt, 300);
        this.triggerRadius = nbt.getFloat("triggerRadius");
        this.armTime = nbt.getInteger("armTime");
        this.maxTriggers = nbt.getInteger("maxTriggers");
        this.triggerCooldown = nbt.getInteger("triggerCooldown");
        this.damage = nbt.getFloat("damage");
        this.damageRadius = nbt.getFloat("damageRadius");
        this.knockback = nbt.getFloat("knockback");
        this.visible = nbt.getBoolean("visible");
    }

    public float getTriggerRadius() {
        return triggerRadius;
    }

    public void setTriggerRadius(float triggerRadius) {
        this.triggerRadius = triggerRadius;
    }

    public int getArmTime() {
        return armTime;
    }

    public void setArmTime(int armTime) {
        this.armTime = armTime;
    }

    public int getMaxTriggers() {
        return maxTriggers;
    }

    public void setMaxTriggers(int maxTriggers) {
        this.maxTriggers = maxTriggers;
    }

    public int getTriggerCooldown() {
        return triggerCooldown;
    }

    public void setTriggerCooldown(int triggerCooldown) {
        this.triggerCooldown = triggerCooldown;
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    public float getDisplayDamage() { return damage; }

    public float getDamageRadius() {
        return damageRadius;
    }

    public void setDamageRadius(float damageRadius) {
        this.damageRadius = damageRadius;
    }

    public float getKnockback() {
        return knockback;
    }

    public void setKnockback(float knockback) {
        this.knockback = knockback;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected void addPresetFieldDef(List<FieldDef> defs) {
        defs.add(FieldDef.subGuiField("gui.applyPreset",
            SubGuiTrapPresetSelector::new,
            gui -> {
                SubGuiTrapPresetSelector selector = (SubGuiTrapPresetSelector) gui;
                if (selector.selectedPreset != null) {
                    applyTrapPresetDefaults(selector.selectedPreset);
                }
            }));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getAbilityDefinitions(List<FieldDef> defs) {
        addPresetFieldDef(defs);

        defs.addAll(Arrays.asList(
            FieldDef.row(
                FieldDef.intField("ability.duration", this::getDurationTicks, this::setDurationTicks).range(1, 2000),
                FieldDef.enumField("ability.zoneShape", ZoneShape.class, this::getZoneShapeEnum, this::setZoneShapeEnum)
            ),
            FieldDef.section("ability.section.zone"),
            FieldDef.row(
                FieldDef.floatField("gui.radius", this::getSpawnRadius, this::setSpawnRadius),
                FieldDef.intField("gui.count", this::getZoneCount, this::setZoneCount).range(1, 20)
            ),
            FieldDef.row(
                FieldDef.floatField("gui.height", this::getZoneHeight, this::setZoneHeight),
                FieldDef.boolField("ability.visible", this::isVisible, this::setVisible)
            ),
            FieldDef.section("ability.section.trigger"),
            FieldDef.row(
                FieldDef.floatField("gui.radius", this::getTriggerRadius, this::setTriggerRadius),
                FieldDef.intField("ability.armTime", this::getArmTime, this::setArmTime)
            ),
            FieldDef.row(
                FieldDef.intField("gui.max", this::getMaxTriggers, this::setMaxTriggers),
                FieldDef.intField("gui.cooldown", this::getTriggerCooldown, this::setTriggerCooldown)
            ),
            FieldDef.section("ability.section.damage"),
            FieldDef.row(
                FieldDef.floatField("enchantment.damage", this::getDamage, this::setDamage),
                FieldDef.floatField("gui.radius", this::getDamageRadius, this::setDamageRadius)
            ),
            FieldDef.floatField("ability.knockback", this::getKnockback, this::setKnockback),
            AbilityFieldDefs.effectsListField("ability.effects", this::getEffects, this::setEffects)
        ));

        addVisualFieldDefs(defs);
    }
}
