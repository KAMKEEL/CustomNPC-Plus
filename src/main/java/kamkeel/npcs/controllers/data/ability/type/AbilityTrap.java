package kamkeel.npcs.controllers.data.ability.type;

import kamkeel.npcs.controllers.data.ability.data.EnergyDisplayData;
import kamkeel.npcs.entity.EntityAbilityZone;
import kamkeel.npcs.entity.EntityAbilityZone.ZoneShape;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import noppes.npcs.api.ability.type.IAbilityTrap;

import noppes.npcs.client.gui.builder.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldDefs;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Trap ability: Places proximity-triggered traps around the caster.
 * Spawns one or more EntityAbilityZone entities in random positions
 * within spawnRadius of the caster. Damage and trigger logic handled by the entity.
 */
public class AbilityTrap extends AbilityZone implements IAbilityTrap {

    // Trap-specific fields
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
    }

    @Override
    public float getZoneRadius() {
        return triggerRadius;
    }

    @Override
    public void onExecute(EntityLivingBase caster, EntityLivingBase target, World world) {
        if (world.isRemote && !isPreview()) {
            signalCompletion();
            return;
        }

        activeEntities.clear();

        // Use pre-calculated positions from telegraph phase, or generate new ones
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

            EntityAbilityZone entity = EntityAbilityZone.createTrap(world, caster,
                pos[0], caster.posY, pos[1],
                zoneShape,
                triggerRadius, armTime, maxTriggers, triggerCooldown,
                damage, damageRadius, knockback, durationTicks,
                colorData.innerColor, colorData.outerColor, colorData.outerColorEnabled,
                zoneHeight,
                particleDensity, particleScale, animSpeed, lightningDensity,
                getEffects());

            applyVisualToEntity(entity);

            if (isPreview()) {
                entity.setupPreview(caster);
            }

            spawnAbilityEntity(world, entity);
            activeEntities.add(entity);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // NBT
    // ═══════════════════════════════════════════════════════════════════

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

    // ═══════════════════════════════════════════════════════════════════
    // TRAP-SPECIFIC GETTERS & SETTERS
    // ═══════════════════════════════════════════════════════════════════

    public float getTriggerRadius() { return triggerRadius; }
    public void setTriggerRadius(float triggerRadius) { this.triggerRadius = triggerRadius; }
    public int getArmTime() { return armTime; }
    public void setArmTime(int armTime) { this.armTime = armTime; }
    public int getMaxTriggers() { return maxTriggers; }
    public void setMaxTriggers(int maxTriggers) { this.maxTriggers = maxTriggers; }
    public int getTriggerCooldown() { return triggerCooldown; }
    public void setTriggerCooldown(int triggerCooldown) { this.triggerCooldown = triggerCooldown; }
    public float getDamage() { return damage; }
    public void setDamage(float damage) { this.damage = damage; }
    public float getDamageRadius() { return damageRadius; }
    public void setDamageRadius(float damageRadius) { this.damageRadius = damageRadius; }
    public float getKnockback() { return knockback; }
    public void setKnockback(float knockback) { this.knockback = knockback; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    // ═══════════════════════════════════════════════════════════════════
    // GUI FIELD DEFINITIONS
    // ═══════════════════════════════════════════════════════════════════

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
