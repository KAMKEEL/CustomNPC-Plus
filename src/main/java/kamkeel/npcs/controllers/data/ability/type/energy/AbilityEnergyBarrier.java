package kamkeel.npcs.controllers.data.ability.type.energy;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.data.EnergyBarrierData;
import kamkeel.npcs.controllers.data.ability.data.EnergyDisplayData;
import kamkeel.npcs.entity.EntityEnergyBarrier;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import noppes.npcs.client.gui.builder.FieldDef;

import java.util.List;

/**
 * Abstract base for energy barrier abilities (Dome, Wall, Shield).
 * Handles shared visual data, barrier configuration, and lifecycle management.
 */
public abstract class AbilityEnergyBarrier extends AbilityEnergy {

    protected final EnergyBarrierData barrierData;

    // Runtime entity tracking
    protected transient EntityEnergyBarrier barrierEntity;

    protected AbilityEnergyBarrier(EnergyDisplayData displayData, EnergyBarrierData barrierData) {
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
    public void onWindUpTick(EntityLivingBase caster, EntityLivingBase target, int tick) {
        if (caster.worldObj.isRemote) return;

        if (tick == 1) {
            barrierEntity = createBarrierEntity(caster, target);
            if (barrierEntity != null) {
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
                spawnAbilityEntity(barrierEntity);
            }
        }
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, int tick) {
        if (barrierEntity == null || barrierEntity.isDead) {
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
        barrierData.writeNBT(nbt);
    }

    @Override
    public final void readTypeNBT(NBTTagCompound nbt) {
        readBarrierTypeNBT(nbt);
        readEnergyNBT(nbt);
        barrierData.readNBT(nbt);
    }

    // ==================== API GETTERS & SETTERS ====================

    // Display and lightning data inherited from AbilityEnergy

    // Barrier data
    public float getBarrierMaxHealth() { return barrierData.maxHealth; }
    public void setBarrierMaxHealth(float maxHealth) { barrierData.setMaxHealth(maxHealth); }

    public boolean isUseHealth() { return barrierData.useHealth; }
    public void setUseHealth(boolean useHealth) { barrierData.useHealth = useHealth; }

    public int getBarrierDuration() { return barrierData.durationTicks; }
    public void setBarrierDuration(int ticks) { barrierData.setDurationTicks(ticks); }

    public boolean isUseDuration() { return barrierData.useDuration; }
    public void setUseDuration(boolean useDuration) { barrierData.useDuration = useDuration; }

    public float getDefaultMultiplier() { return barrierData.defaultMultiplier; }
    public void setDefaultMultiplier(float mult) { barrierData.defaultMultiplier = mult; }

    public void setDamageMultiplier(String typeId, float mult) { barrierData.setMultiplier(typeId, mult); }
    public float getDamageMultiplier(String typeId) { return barrierData.getMultiplier(typeId); }

    // Knockback data
    public boolean isKnockbackEnabled() { return barrierData.knockbackEnabled; }
    public void setKnockbackEnabled(boolean enabled) { barrierData.knockbackEnabled = enabled; }
    public float getKnockbackStrength() { return barrierData.knockbackStrength; }
    public void setKnockbackStrength(float strength) { barrierData.knockbackStrength = strength; }
    public String getKnockbackTargetKey() { return barrierData.getKnockbackTargetKey(); }
    public void setKnockbackTargetKey(String key) { barrierData.setKnockbackTargetFromKey(key); }

    // Melee data
    public boolean isMeleeEnabled() { return barrierData.meleeEnabled; }
    public void setMeleeEnabled(boolean enabled) { barrierData.meleeEnabled = enabled; }
    public float getMeleeDamageMultiplier() { return barrierData.meleeDamageMultiplier; }
    public void setMeleeDamageMultiplier(float mult) { barrierData.meleeDamageMultiplier = mult; }

    // ==================== GUI ====================

    @SideOnly(Side.CLIENT)
    @Override
    public final void getAbilityDefinitions(List<FieldDef> defs) {
        // Type-specific fields first
        addBarrierTypeDefinitions(defs);

        // Barrier section
        defs.add(FieldDef.section("ability.section.barrier"));
        defs.add(FieldDef.boolField("ability.useHealth", this::isUseHealth, this::setUseHealth));
        defs.add(FieldDef.floatField("ability.maxHealth", this::getBarrierMaxHealth, this::setBarrierMaxHealth)
            .visibleWhen(this::isUseHealth));
        defs.add(FieldDef.boolField("ability.useDuration", this::isUseDuration, this::setUseDuration));
        defs.add(FieldDef.intField("ability.duration", this::getBarrierDuration, this::setBarrierDuration)
            .range(1, 6000).visibleWhen(this::isUseDuration));
        defs.add(FieldDef.floatField("ability.defaultMultiplier", this::getDefaultMultiplier, this::setDefaultMultiplier));

        // Knockback section
        defs.add(FieldDef.section("ability.section.knockback"));
        defs.add(FieldDef.boolField("ability.knockbackEnabled", this::isKnockbackEnabled, this::setKnockbackEnabled));
        defs.add(FieldDef.row(
            FieldDef.floatField("ability.knockbackStrength", this::getKnockbackStrength, this::setKnockbackStrength)
                .range(0, 10).visibleWhen(this::isKnockbackEnabled),
            FieldDef.stringEnumField("ability.knockbackTarget", EnergyBarrierData.getKnockbackTargetKeys(),
                this::getKnockbackTargetKey, this::setKnockbackTargetKey)
                .visibleWhen(this::isKnockbackEnabled)
        ));

        // Melee section
        defs.add(FieldDef.section("ability.section.melee"));
        defs.add(FieldDef.boolField("ability.meleeEnabled", this::isMeleeEnabled, this::setMeleeEnabled));
        defs.add(FieldDef.floatField("ability.meleeDamageMultiplier", this::getMeleeDamageMultiplier, this::setMeleeDamageMultiplier)
            .range(0, 10).visibleWhen(this::isMeleeEnabled));

        // Visual tab - colors + effects (from AbilityEnergy)
        addEnergyVisualDefinitions(defs);
    }
}
