package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.Ability;
import kamkeel.npcs.controllers.data.ability.LockMovementType;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.advanced.ability.SubGuiAbilityGuard;
import noppes.npcs.client.gui.util.IAbilityConfigCallback;
import noppes.npcs.entity.EntityNPCInterface;

import noppes.npcs.api.ability.type.IAbilityGuard;

import java.util.Random;

/**
 * Guard ability: Defensive stance that reduces incoming damage.
 * Can optionally counter-attack when hit.
 */
public class AbilityGuard extends Ability implements IAbilityGuard {

    private static final Random RANDOM = new Random();

    // Type-specific parameters
    private int durationTicks = 60;
    private float damageReduction = 0.5f;
    private boolean canCounter = false;
    private CounterType counterType = CounterType.FLAT;
    private float counterValue = 6.0f;
    private float counterChance = 0.3f;
    private String counterSound = "random.wood_click";
    private int counterAnimationId = -1;

    // Runtime state
    private transient EntityLivingBase lastAttacker;
    private transient boolean counterTriggered;
    private transient float lastDamageTaken;

    public enum CounterType {
        FLAT,
        PERCENT
    }

    public AbilityGuard() {
        this.typeId = "ability.cnpc.guard";
        this.name = "Guard";
        this.targetingMode = TargetingMode.SELF;
        this.lockMovement = LockMovementType.ACTIVE;
        this.cooldownTicks = 0;
        this.windUpTicks = 10;
        this.interruptible = false; // Hard to interrupt while guarding
        // No telegraph for guard - it's a defensive stance
        this.telegraphType = TelegraphType.NONE;
        this.showTelegraph = false;
        this.windUpSound = "random.anvil_use";
        this.activeSound = "random.anvil_land";
    }

    @Override
    public boolean hasTypeSettings() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiAbilityConfig createConfigGui(
        IAbilityConfigCallback callback) {
        return new SubGuiAbilityGuard(this, callback);
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
    public void onExecute(EntityNPCInterface npc, EntityLivingBase target, World world) {
        lastAttacker = null;
        counterTriggered = false;
        lastDamageTaken = 0.0f;
    }

    @Override
    public void onActiveTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
        // Counter attack logic if triggered
        if (canCounter && counterTriggered && lastAttacker != null && !world.isRemote) {
            performCounter(npc, world);
        }

        // Check if guard duration has ended
        if (tick >= durationTicks) {
            signalCompletion();
        }
    }

    /**
     * Called externally when the NPC takes damage while guarding.
     * This should be called from the damage handling code.
     *
     * @param attacker The entity that attacked
     * @param damage   The damage amount (after reduction)
     */
    public void onDamageTaken(EntityNPCInterface npc, EntityLivingBase attacker, DamageSource source, float damage) {
        if (!canCounter || attacker == null) return;
        if (!isDirectHit(source)) return;
        if (RANDOM.nextFloat() >= counterChance) return;

        lastAttacker = attacker;
        lastDamageTaken = damage;
        counterTriggered = true;
        // Counter will be performed in onActiveTick - don't self-interrupt
    }

    private void performCounter(EntityNPCInterface npc, World world) {
        float counterDamage = counterType == CounterType.PERCENT
            ? lastDamageTaken * (counterValue / 100.0f)
            : counterValue;

        boolean wasHit = applyAbilityDamage(npc, lastAttacker, counterDamage, 0.5f);
        if (wasHit && counterSound != null && !counterSound.isEmpty()) {
            world.playSoundAtEntity(npc, counterSound, 1.0f, 1.2f);
        }
        // Use shared animation utility
        npc.abilities.playAbilityAnimation(counterAnimationId);

        counterTriggered = false;
        lastAttacker = null;
        lastDamageTaken = 0.0f;
    }

    private boolean isDirectHit(DamageSource source) {
        if (source == null) return false;
        if (source.isMagicDamage() || source.isFireDamage() || source.isExplosion()) return false;
        return source.getEntity() instanceof EntityLivingBase;
    }

    /**
     * Get the damage reduction factor while guarding.
     * Returns 0.5 for 50% reduction.
     */
    public float getDamageReductionFactor() {
        if (isExecuting() && getPhase() == kamkeel.npcs.controllers.data.ability.AbilityPhase.ACTIVE) {
            return damageReduction;
        }
        return 0.0f;
    }

    /**
     * Check if the NPC is currently in guard stance.
     */
    public boolean isGuarding() {
        return isExecuting() && getPhase() == kamkeel.npcs.controllers.data.ability.AbilityPhase.ACTIVE;
    }

    @Override
    public void reset() {
        super.reset();
        lastAttacker = null;
        counterTriggered = false;
        lastDamageTaken = 0.0f;
    }

    @Override
    public float getTelegraphRadius() {
        return 0; // No telegraph for guard
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setInteger("durationTicks", durationTicks);
        nbt.setFloat("damageReduction", damageReduction);
        nbt.setBoolean("canCounter", canCounter);
        nbt.setString("counterType", counterType.name());
        nbt.setFloat("counterValue", counterValue);
        nbt.setFloat("counterChance", counterChance);
        nbt.setString("counterSound", counterSound);
        nbt.setInteger("counterAnimationId", counterAnimationId);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.durationTicks = nbt.hasKey("durationTicks") ? nbt.getInteger("durationTicks") : 60;
        this.damageReduction = nbt.hasKey("damageReduction") ? nbt.getFloat("damageReduction") : 0.5f;
        this.canCounter = nbt.hasKey("canCounter") && nbt.getBoolean("canCounter");
        try {
            this.counterType = CounterType.valueOf(nbt.getString("counterType"));
        } catch (Exception e) {
            this.counterType = CounterType.FLAT;
        }
        if (nbt.hasKey("counterValue")) {
            this.counterValue = nbt.getFloat("counterValue");
        } else if (nbt.hasKey("counterDamage")) {
            this.counterValue = nbt.getFloat("counterDamage");
        } else {
            this.counterValue = 6.0f;
        }
        this.counterChance = nbt.hasKey("counterChance") ? nbt.getFloat("counterChance") : 0.3f;
        this.counterSound = nbt.hasKey("counterSound") ? nbt.getString("counterSound") : "random.wood_click";
        this.counterAnimationId = nbt.hasKey("counterAnimationId") ? nbt.getInteger("counterAnimationId") : -1;
    }

    // Getters & Setters
    public int getDurationTicks() {
        return durationTicks;
    }

    public void setDurationTicks(int durationTicks) {
        this.durationTicks = Math.max(1, durationTicks);
    }

    public float getDamageReduction() {
        return damageReduction;
    }

    public void setDamageReduction(float damageReduction) {
        this.damageReduction = damageReduction;
    }

    public boolean isCanCounter() {
        return canCounter;
    }

    @Override
    public boolean canCounter() {
        return canCounter;
    }

    public void setCanCounter(boolean canCounter) {
        this.canCounter = canCounter;
    }

    public CounterType getCounterTypeEnum() {
        return counterType;
    }

    public void setCounterTypeEnum(CounterType counterType) {
        this.counterType = counterType;
    }

    @Override
    public int getCounterType() {
        return counterType.ordinal();
    }

    @Override
    public void setCounterType(int type) {
        CounterType[] values = CounterType.values();
        this.counterType = type >= 0 && type < values.length ? values[type] : CounterType.FLAT;
    }

    public float getCounterValue() {
        return counterValue;
    }

    public void setCounterValue(float counterValue) {
        this.counterValue = counterValue;
    }

    public float getCounterChance() {
        return counterChance;
    }

    public void setCounterChance(float counterChance) {
        this.counterChance = counterChance;
    }

    public String getCounterSound() {
        return counterSound;
    }

    public void setCounterSound(String counterSound) {
        this.counterSound = counterSound;
    }

    public int getCounterAnimationId() {
        return counterAnimationId;
    }

    public void setCounterAnimationId(int counterAnimationId) {
        this.counterAnimationId = counterAnimationId;
    }
}
