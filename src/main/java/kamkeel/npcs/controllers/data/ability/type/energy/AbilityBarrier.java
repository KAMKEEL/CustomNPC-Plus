package kamkeel.npcs.controllers.data.ability.type.energy;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.AbilityController;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyBarrierData;
import kamkeel.npcs.controllers.data.ability.data.energy.EnergyDisplayData;
import kamkeel.npcs.entity.EntityEnergyBarrier;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import noppes.npcs.client.gui.builder.FieldDef;
import noppes.npcs.controllers.data.MagicData;
import noppes.npcs.wrapper.nbt.MC1710NBTCompound;

import java.util.List;

/**
 * Abstract base for energy barrier abilities (Dome, Wall, Shield).
 * Handles shared visual data, barrier configuration, and lifecycle management.
 */
public abstract class AbilityBarrier extends AbilityEnergy {

    protected final EnergyBarrierData barrierData;

    // Spawn position offsets (relative to caster)
    protected float offsetX = 0.0f;
    protected float offsetY = 0.0f;
    protected float offsetZ = 0.0f;

    // Runtime entity tracking
    protected transient EntityEnergyBarrier barrierEntity;

    protected AbilityBarrier(EnergyDisplayData displayData, EnergyBarrierData barrierData) {
        super(displayData);
        this.barrierData = barrierData;
    }

    // ==================== ABSTRACT METHODS ====================

    /**
     * Create and spawn the barrier entity during execution.
     */
    protected abstract EntityEnergyBarrier createBarrierEntity(EntityLivingBase caster, EntityLivingBase target);

    /**
     * Add type-specific GUI field definitions.
     */
    @SideOnly(Side.CLIENT)
    protected abstract void addBarrierTypeDefinitions(List<FieldDef> defs);

    /**
     * Write type-specific fields to NBT.
     */
    protected abstract void writeBarrierTypeNBT(NBTTagCompound nbt);

    /**
     * Read type-specific fields from NBT.
     */
    protected abstract void readBarrierTypeNBT(NBTTagCompound nbt);

    // ==================== LIFECYCLE ====================

    @Override
    public boolean allowBurst() {
        return false;
    }

    @Override
    public boolean hasDamage() {
        return false; // Barriers are defensive
    }

    @Override
    public boolean hasMagic() {
        return true; // Barriers use magic for defense interactions
    }

    @Override
    public boolean allowFreeOnCast() {
        return true;
    }

    @Override
    public void detach() {
        barrierEntity = null;
    }

    @Override
    public void onWindUpTick(EntityLivingBase caster, EntityLivingBase target, int tick) {
        if (caster.worldObj.isRemote) return;

        if (tick == 1) {
            barrierEntity = createBarrierEntity(caster, target);
            if (barrierEntity != null) {
                inheritMagicData(barrierEntity, caster);
                applyBarrierHealthModifiers(caster);
                barrierEntity.setupCharging(getWindUpTicks());
                spawnAbilityEntity(barrierEntity);
            }
        }
    }

    @Override
    public void onExecute(EntityLivingBase caster, EntityLivingBase target) {
        if (barrierEntity != null && !barrierEntity.isDead) {
            // Barrier was spawned during windup — transition to active
            barrierEntity.finishCharging();
        } else {
            // No windup or entity died — create fresh
            barrierEntity = createBarrierEntity(caster, target);
            if (barrierEntity != null) {
                inheritMagicData(barrierEntity, caster);
                applyBarrierHealthModifiers(caster);
                spawnAbilityEntity(barrierEntity);
            }
        }
    }

    private void inheritMagicData(EntityEnergyBarrier entity, EntityLivingBase caster) {
        MagicData resolved = resolveMagicData(caster);
        if (resolved != null) {
            entity.setMagicData(resolved.copy());
        }
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, int tick) {
        if (barrierEntity == null || barrierEntity.isDead) {
            signalCompletion();
            return;
        }

        // Free on Cast: complete immediately after barrier becomes active
        if (isFreeOnCast()) {
            signalCompletion();
            return;
        }

        // Duration tracked by entity itself, but check here too as a failsafe
        if (barrierData.useDuration && tick >= barrierData.durationTicks) {
            cleanup();
            signalCompletion();
        }
    }

    @Override
    public void onComplete(EntityLivingBase caster, EntityLivingBase target) {
    }

    /**
     * Allow extenders to modify the barrier's max health at spawn time.
     */
    private void applyBarrierHealthModifiers(EntityLivingBase caster) {
        float modHealth = AbilityController.Instance.fireModifyBarrierHealth(this, caster, barrierData.maxHealth);
        if (modHealth != barrierData.maxHealth) {
            barrierEntity.setBarrierMaxHealth(modHealth);
        }
    }

    @Override
    public void onInterrupt(EntityLivingBase caster, DamageSource source, float damage) {
        cleanup();
    }

    @Override
    public void cleanup() {
        if (barrierEntity != null && !barrierEntity.isDead) {
            barrierEntity.setDead();
        }
        barrierEntity = null;
    }

    // ==================== NBT ====================

    @Override
    public final void writeTypeNBT(NBTTagCompound nbt) {
        writeBarrierTypeNBT(nbt);
        writeEnergyNBT(nbt);
        barrierData.writeNBT(new MC1710NBTCompound(nbt));
        nbt.setFloat("barrierOffsetX", offsetX);
        nbt.setFloat("barrierOffsetY", offsetY);
        nbt.setFloat("barrierOffsetZ", offsetZ);
    }

    @Override
    public final void readTypeNBT(NBTTagCompound nbt) {
        offsetX = nbt.hasKey("barrierOffsetX") ? nbt.getFloat("barrierOffsetX") : 0.0f;
        offsetY = nbt.hasKey("barrierOffsetY") ? nbt.getFloat("barrierOffsetY") : 0.0f;
        offsetZ = nbt.hasKey("barrierOffsetZ") ? nbt.getFloat("barrierOffsetZ") : 0.0f;
        readBarrierTypeNBT(nbt);
        readEnergyNBT(nbt);
        barrierData.readNBT(new MC1710NBTCompound(nbt));
    }

    // ==================== API GETTERS & SETTERS ====================

    // Display and lightning data inherited from AbilityEnergy

    // Barrier data
    public float getBarrierMaxHealth() {
        return barrierData.maxHealth;
    }

    public void setBarrierMaxHealth(float maxHealth) {
        barrierData.setMaxHealth(maxHealth);
    }

    public boolean isUseHealth() {
        return barrierData.useHealth;
    }

    public void setUseHealth(boolean useHealth) {
        barrierData.useHealth = useHealth;
    }

    @Override
    public float getDisplayBarrierHealth() { return barrierData.useHealth ? barrierData.maxHealth : 0; }

    @Override
    public boolean isDisplayReflect() { return barrierData.reflect; }

    @Override
    public float getDisplayReflectStrength() { return barrierData.reflectStrengthPct; }

    @Override
    public boolean isDisplayAbsorbing() { return barrierData.absorbing; }

    public int getBarrierDuration() {
        return barrierData.durationTicks;
    }

    public void setBarrierDuration(int ticks) {
        barrierData.setDurationTicks(ticks);
    }

    public boolean isUseDuration() {
        return barrierData.useDuration;
    }

    public void setUseDuration(boolean useDuration) {
        barrierData.useDuration = useDuration;
    }

    public float getDefaultMultiplier() {
        return barrierData.defaultMultiplier;
    }

    public void setDefaultMultiplier(float mult) {
        barrierData.defaultMultiplier = mult;
    }

    public void setDamageMultiplier(String typeId, float mult) {
        barrierData.setMultiplier(typeId, mult);
    }

    public float getDamageMultiplier(String typeId) {
        return barrierData.getMultiplier(typeId);
    }

    // Solid data
    public boolean isSolid() {
        return barrierData.solid;
    }

    public void setSolid(boolean solid) {
        barrierData.solid = solid;
    }

    // Knockback data
    public boolean isKnockbackEnabled() {
        return barrierData.knockbackEnabled;
    }

    public void setKnockbackEnabled(boolean enabled) {
        barrierData.knockbackEnabled = enabled;
    }

    public float getKnockbackStrength() {
        return barrierData.knockbackStrength;
    }

    public void setKnockbackStrength(float strength) {
        barrierData.knockbackStrength = strength;
    }

    // Absorbing data
    public boolean isAbsorbing() {
        return barrierData.absorbing;
    }

    public void setAbsorbing(boolean absorbing) {
        barrierData.absorbing = absorbing;
    }

    public float getAbsorbRadius() {
        return barrierData.absorbRadius;
    }

    public void setAbsorbRadius(float radius) {
        barrierData.absorbRadius = radius;
    }

    // Reflection data
    public boolean isReflect() {
        return barrierData.reflect;
    }

    public void setReflect(boolean reflect) {
        barrierData.reflect = reflect;
    }

    public float getReflectStrengthPct() {
        return barrierData.reflectStrengthPct;
    }

    public void setReflectStrengthPct(float strengthPct) {
        barrierData.setReflectStrengthPct(strengthPct);
    }

    public boolean isTargetOwner() {
        return barrierData.targetOwner;
    }

    public void setTargetOwner(boolean targetOwner) {
        barrierData.targetOwner = targetOwner;
    }

    // Melee data
    public boolean isMeleeEnabled() {
        return barrierData.meleeEnabled;
    }

    public void setMeleeEnabled(boolean enabled) {
        barrierData.meleeEnabled = enabled;
    }

    public float getMeleeDamageMultiplier() {
        return barrierData.meleeDamageMultiplier;
    }

    public void setMeleeDamageMultiplier(float mult) {
        barrierData.meleeDamageMultiplier = mult;
    }

    // Offset data
    public float getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }

    public float getOffsetZ() {
        return offsetZ;
    }

    public void setOffsetZ(float offsetZ) {
        this.offsetZ = offsetZ;
    }

    // ==================== GUI ====================

    @SideOnly(Side.CLIENT)
    @Override
    public final void getAbilityDefinitions(List<FieldDef> defs) {
        // Type-specific fields first
        addBarrierTypeDefinitions(defs);

        // Offset section
        defs.add(FieldDef.section("ability.section.offset"));
        defs.add(FieldDef.row(
            FieldDef.floatField("ability.offsetX", this::getOffsetX, this::setOffsetX).min(Float.NEGATIVE_INFINITY),
            FieldDef.floatField("ability.offsetY", this::getOffsetY, this::setOffsetY).min(Float.NEGATIVE_INFINITY)
        ));
        defs.add(FieldDef.floatField("ability.offsetZ", this::getOffsetZ, this::setOffsetZ).min(Float.NEGATIVE_INFINITY));

        // Barrier section
        defs.add(FieldDef.section("ability.section.barrier"));
        defs.add(FieldDef.boolField("ability.useHealth", this::isUseHealth, this::setUseHealth));
        defs.add(FieldDef.floatField("ability.maxHealth", this::getBarrierMaxHealth, this::setBarrierMaxHealth)
            .visibleWhen(this::isUseHealth));
        defs.add(FieldDef.boolField("ability.useDuration", this::isUseDuration, this::setUseDuration));
        defs.add(FieldDef.intField("ability.duration", this::getBarrierDuration, this::setBarrierDuration)
            .range(1, 12000).visibleWhen(this::isUseDuration));
        defs.add(FieldDef.floatField("ability.defaultMultiplier", this::getDefaultMultiplier, this::setDefaultMultiplier));

        // Properties section
        defs.add(FieldDef.section("ability.section.properties"));
        defs.add(FieldDef.boolField("ability.solid", this::isSolid, this::setSolid)
            .hover("ability.hover.solid"));
        defs.add(FieldDef.boolField("ability.knockbackEnabled", this::isKnockbackEnabled, this::setKnockbackEnabled)
            .hover("ability.hover.knockbackEnabled"));
        defs.add(FieldDef.floatField("ability.knockbackStrength", this::getKnockbackStrength, this::setKnockbackStrength)
            .range(0, 10).visibleWhen(this::isKnockbackEnabled));
        defs.add(FieldDef.boolField("ability.absorbing", this::isAbsorbing, this::setAbsorbing)
            .hover("ability.hover.absorbing"));
        defs.add(FieldDef.floatField("ability.absorbRadius", this::getAbsorbRadius, this::setAbsorbRadius)
            .range(-1, 128).visibleWhen(this::isAbsorbing)
            .hover("ability.hover.absorbRadius"));
        defs.add(FieldDef.boolField("ability.reflect", this::isReflect, this::setReflect)
            .hover("ability.hover.reflect"));
        defs.add(FieldDef.floatField("ability.reflectStrength", this::getReflectStrengthPct, this::setReflectStrengthPct)
            .range(0, 100).visibleWhen(this::isReflect));
        defs.add(FieldDef.boolField("ability.targetOwner", this::isTargetOwner, this::setTargetOwner)
            .hover("ability.hover.targetOwner")
            .visibleWhen(this::isReflect));
        defs.add(FieldDef.boolField("ability.meleeEnabled", this::isMeleeEnabled, this::setMeleeEnabled)
            .hover("ability.hover.meleeEnabled"));
        defs.add(FieldDef.floatField("ability.meleeDamageMultiplier", this::getMeleeDamageMultiplier, this::setMeleeDamageMultiplier)
            .range(0, 10).visibleWhen(this::isMeleeEnabled));

        // Visual tab - colors + effects (from AbilityEnergy)
        addEnergyVisualDefinitions(defs);
    }
}
