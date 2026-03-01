package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.enums.AbilityPhase;
import kamkeel.npcs.controllers.data.ability.enums.LockMode;
import kamkeel.npcs.controllers.data.ability.enums.TargetingMode;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import noppes.npcs.api.ability.type.IAbilityDefend;
import noppes.npcs.client.gui.builder.FieldDef;

import java.util.List;

/**
 * Abstract base class for defensive abilities (Guard, Counter, Dodge).
 * <p>
 * {@link #onDefend} handles shared checks (physical melee only) then delegates
 * to {@link #performDefend(float)} which subclasses override for their specific behavior.
 */
public abstract class AbilityDefend extends Ability implements IAbilityDefend {

    // ═══════════════════════════════════════════════════════════════════
    // SHARED PERSIST STATE
    // ═══════════════════════════════════════════════════════════════════

    protected int durationTicks = 60;
    protected int maxHitAmount = 3;

    // ═══════════════════════════════════════════════════════════════════
    // TRANSIENT RUNTIME STATE
    // ═══════════════════════════════════════════════════════════════════

    protected transient int hitCount;
    protected transient EntityLivingBase lastAttacker;
    protected transient float lastDamageTaken;

    // ═══════════════════════════════════════════════════════════════════
    // CONSTRUCTOR DEFAULTS
    // ═══════════════════════════════════════════════════════════════════

    protected AbilityDefend() {
        this.targetingMode = TargetingMode.SELF;
        this.lockMovement = LockMode.ACTIVE;
        this.interruptible = false;
        this.telegraphType = TelegraphType.NONE;
        this.showTelegraph = false;
        this.windUpTicks = 0;
    }

    // ═══════════════════════════════════════════════════════════════════
    // LIFECYCLE
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public void onExecute(EntityLivingBase caster, EntityLivingBase target) {
        hitCount = 0;
        lastAttacker = null;
        lastDamageTaken = 0.0f;
        onDefendStart(caster);
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, int tick) {
        onDefendTick(caster, target, tick);

        if (tick >= durationTicks) {
            signalCompletion();
        }
    }

    @Override
    public void reset() {
        super.reset();
        hitCount = 0;
        lastAttacker = null;
        lastDamageTaken = 0.0f;
    }

    // ═══════════════════════════════════════════════════════════════════
    // DEFEND HOOK
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Called when the caster is hit while defending.
     * Checks for physical melee damage only, then delegates to {@link #performDefend(float)}.
     *
     * @param attacker The entity that hit the caster
     * @param source   The damage source
     * @param amount   The original incoming damage
     * @return The modified damage to apply (unchanged if not physical melee)
     */
    public final float onDefend(EntityLivingBase attacker, DamageSource source, float amount) {
        if (!isDefending())
            return amount;
        if (attacker == null || source == null)
            return amount;

        // Only react to physical melee damage
        if (source.isMagicDamage() || source.isFireDamage() || source.isExplosion() || source.isProjectile())
            return amount;

        lastAttacker = attacker;
        lastDamageTaken = amount;
        hitCount++;

        float result = performDefend(amount);

        // Auto-complete after max hits
        if (maxHitAmount > 0 && hitCount >= maxHitAmount) {
            signalCompletion();
        }

        return result;
    }

    /**
     * Subclass-specific defend behavior. Called only for physical melee hits while defending.
     *
     * @param amount The incoming damage
     * @return The modified damage the caster should take
     */
    protected abstract float performDefend(float amount);

    // ═══════════════════════════════════════════════════════════════════
    // SUBCLASS HOOKS
    // ═══════════════════════════════════════════════════════════════════

    protected void onDefendStart(EntityLivingBase caster) {}

    protected abstract void onDefendTick(EntityLivingBase caster, EntityLivingBase target, int tick);

    /**
     * Subclass-specific NBT persistence.
     */
    protected abstract void writeSubTypeNBT(NBTTagCompound nbt);
    protected abstract void readSubTypeNBT(NBTTagCompound nbt);

    @SideOnly(Side.CLIENT)
    protected abstract void getTypeDefinitions(List<FieldDef> defs);

    // ═══════════════════════════════════════════════════════════════════
    // STATE QUERIES
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public boolean isDefending() {
        return isExecuting() && getPhase() == AbilityPhase.ACTIVE;
    }

    // ═══════════════════════════════════════════════════════════════════
    // OVERRIDES
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public boolean hasDamage() {
        return false;
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
        return new TargetingMode[]{TargetingMode.SELF};
    }

    @Override
    public float getTelegraphRadius() {
        return 0;
    }

    /**
     * Suppresses the auto-play of activeSound on ACTIVE phase entry.
     * Defend abilities play their sound on hit reaction, not on phase start.
     */
    @Override
    public boolean keepTelegraphDuringActive() {
        return true;
    }

    // ═══════════════════════════════════════════════════════════════════
    // NBT
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public final void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setInteger("durationTicks", durationTicks);
        nbt.setInteger("maxHitAmount", maxHitAmount);
        writeSubTypeNBT(nbt);
    }

    @Override
    public final void readTypeNBT(NBTTagCompound nbt) {
        this.durationTicks = nbt.hasKey("durationTicks") ? nbt.getInteger("durationTicks") : 60;
        this.maxHitAmount = nbt.hasKey("maxHitAmount") ? nbt.getInteger("maxHitAmount") : 3;
        readSubTypeNBT(nbt);
    }

    // ═══════════════════════════════════════════════════════════════════
    // GUI
    // ═══════════════════════════════════════════════════════════════════

    @SideOnly(Side.CLIENT)
    @Override
    public final void getAbilityDefinitions(List<FieldDef> defs) {
        defs.add(FieldDef.intField("ability.duration", this::getDurationTicks, this::setDurationTicks).range(1, 6000));
        defs.add(FieldDef.intField("ability.maxHitAmount", this::getMaxHitAmount, this::setMaxHitAmount).range(0, 100)
            .hover("ability.hover.maxHitAmount"));

        getTypeDefinitions(defs);
    }

    // ═══════════════════════════════════════════════════════════════════
    // GETTERS / SETTERS
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public int getDurationTicks() {
        return durationTicks;
    }

    @Override
    public void setDurationTicks(int ticks) {
        this.durationTicks = Math.max(1, ticks);
    }

    @Override
    public int getMaxHitAmount() {
        return maxHitAmount;
    }

    @Override
    public void setMaxHitAmount(int amount) {
        this.maxHitAmount = Math.max(0, amount);
    }

    public int getHitCount() {
        return hitCount;
    }
}
