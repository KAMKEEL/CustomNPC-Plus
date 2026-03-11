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
import noppes.npcs.controllers.data.Animation;

import java.util.List;

/**
 * Abstract base class for defensive abilities (Guard, Counter, Dodge).
 * <p>
 * {@link #onDefend} handles shared checks then delegates to {@link #performDefend(EntityLivingBase, float)}
 * which subclasses override for their specific behavior.
 * <p>
 * Damage source filtering is controlled by {@link #isValidDamageSource(DamageSource)}.
 * By default only physical melee is accepted; subclasses can override to widen (e.g. Guard accepts all).
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

    protected transient EntityLivingBase caster;
    protected transient int hitCount;
    protected transient EntityLivingBase lastAttacker;
    protected transient float lastDamageTaken;
    protected transient Animation pendingDefendAnimation;
    protected transient int defendAnimEndTick = -1;
    /** Prevents double-counting hits when multiple damage paths call onDefend for the same hit in a single tick. */
    protected transient long lastDefendTick = -1;
    /** Defers signalCompletion to the next tick so all damage paths for the last hit still see isDefending()=true. */
    protected transient boolean pendingCompletion;

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
        this.caster = caster;
        hitCount = 0;
        lastAttacker = null;
        lastDamageTaken = 0.0f;
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, int tick) {
        if (pendingCompletion || tick >= durationTicks) {
            signalCompletion();
        }
    }

    @Override
    public void reset() {
        super.reset();
        caster = null;
        hitCount = 0;
        lastAttacker = null;
        lastDamageTaken = 0.0f;
        pendingDefendAnimation = null;
        defendAnimEndTick = -1;
        lastDefendTick = -1;
        pendingCompletion = false;
    }

    // ═══════════════════════════════════════════════════════════════════
    // DEFEND HOOK
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Called when the caster is hit while defending.
     * Checks damage source validity via {@link #isValidDamageSource}, then delegates
     * to {@link #performDefend(EntityLivingBase, float)}.
     *
     * @param attacker The entity that hit the caster
     * @param source   The damage source
     * @param amount   The original incoming damage
     * @return The modified damage to apply (unchanged if source is invalid)
     */
    public final float onDefend(EntityLivingBase attacker, DamageSource source, float amount) {
        if (!isDefending()) {
            return amount;
        }
        if (attacker == null || source == null) {
            return amount;
        }

        if (!isValidDamageSource(source)) {
            return amount;
        }

        // Deduplicate per tick: when multiple damage paths (e.g. vanilla + DBC) call onDefend
        // for the same hit, only count it once for hitCount / signalCompletion / animation.
        long currentTick = (caster != null && caster.worldObj != null)
            ? caster.worldObj.getTotalWorldTime() : -1;
        boolean firstCallThisTick = (currentTick < 0 || currentTick != lastDefendTick);

        // If max hits reached on a previous tick and this is a NEW hit, reject it
        if (pendingCompletion && firstCallThisTick) {
            return amount;
        }

        if (firstCallThisTick) {
            lastDefendTick = currentTick;
            lastAttacker = attacker;
            lastDamageTaken = amount;
            hitCount++;

            // Queue defend reaction animation (played on next tick by AbstractDataAbilities)
            pendingDefendAnimation = getDefendAnimation();

            // Defer completion to next tick so all damage paths in this tick
            // still see isDefending()=true and apply guard reduction.
            if (maxHitAmount > 0 && hitCount >= maxHitAmount) {
                pendingCompletion = true;
            }
        }

        // Always calculate the correct reduction for the caller's damage amount
        return performDefend(attacker, amount);
    }

    /**
     * Subclass-specific defend behavior. Called only for valid hits while defending
     * (as determined by {@link #isValidDamageSource}).
     *
     * @param attacker The entity that hit the caster
     * @param amount   The incoming damage
     * @return The modified damage the caster should take
     */
    protected abstract float performDefend(EntityLivingBase attacker, float amount);

    /**
     * Determines whether this defend ability reacts to the given damage source.
     * Default implementation accepts only physical melee damage (rejects magic, fire, explosion, projectile).
     * Subclasses can override to accept additional damage types (e.g. Guard accepts all).
     */
    protected boolean isValidDamageSource(DamageSource source) {
        return !source.isMagicDamage() && !source.isFireDamage() && !source.isExplosion() && !source.isProjectile();
    }

    // ═══════════════════════════════════════════════════════════════════
    // SUBCLASS HOOKS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Subclass-specific NBT persistence.
     */
    protected abstract void writeSubTypeNBT(NBTTagCompound nbt);
    protected abstract void readSubTypeNBT(NBTTagCompound nbt);

    @SideOnly(Side.CLIENT)
    protected abstract void getTypeDefinitions(List<FieldDef> defs);

    // ═══════════════════════════════════════════════════════════════════
    // DEFEND ANIMATION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Returns the animation to play when this defend ability reacts to a hit.
     * Subclasses override to provide their specific animation (counter strike, dodge roll, etc).
     * Guard returns null (no reaction animation).
     */
    protected Animation getDefendAnimation() {
        return null;
    }

    /**
     * Consume and return the pending defend animation (set during onDefend).
     * Called by AbstractDataAbilities to play the animation on the next tick.
     */
    public Animation consumeDefendAnimation() {
        Animation anim = pendingDefendAnimation;
        pendingDefendAnimation = null;
        return anim;
    }

    /**
     * Schedule return to active animation after the defend animation finishes.
     */
    public void scheduleReturnToActive(int currentTick, Animation defendAnim) {
        if (defendAnim != null) {
            defendAnimEndTick = currentTick + (int) defendAnim.getTotalTime();
        }
    }

    /**
     * Check if the defend animation has finished and we should return to the active animation.
     */
    public boolean shouldReturnToActiveAnimation(int currentTick) {
        if (defendAnimEndTick >= 0 && currentTick >= defendAnimEndTick) {
            defendAnimEndTick = -1;
            return true;
        }
        return false;
    }

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
