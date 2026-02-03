package kamkeel.npcs.controllers.data.ability.type;

import kamkeel.npcs.controllers.data.ability.*;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.api.handler.data.IAnimation;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;

import noppes.npcs.api.ability.type.IAbilityGuard;

import noppes.npcs.client.gui.builder.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.AbilityFieldDefs;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;

/**
 * Guard ability: Defensive stance that reduces incoming damage.
 * Can optionally counter-attack when hit.
 */
public class AbilityGuard extends Ability implements IAbilityGuard {

    private int durationTicks = 60;
    private float damageReduction = 0.5f;
    private AbilityGuard.CounterType counterType = AbilityGuard.CounterType.FLAT;
    private float counterValue = 6.0f;
    private String counterSound = "random.anvil_land";
    private boolean canCounter = false;
    private int counterWindow = 10;
    private int counterAnimationId = -1;
    private String counterAnimationName;

    // Runtime state
    private transient EntityLivingBase lastAttacker;
    private transient boolean counterTriggered;
    private transient boolean counterEligible;
    private transient float lastDamageTaken;

    public AbilityGuard() {
        this.typeId = "ability.cnpc.guard";
        this.name = "Guard";
        this.targetingMode = TargetingMode.SELF;
        this.lockMovement = LockMovementType.ACTIVE;
        this.cooldownTicks = 0;
        this.windUpTicks = 0;
        this.interruptible = false; // Hard to interrupt while guarding
        // No telegraph for guard - it's a defensive stance
        this.telegraphType = TelegraphType.NONE;
        this.showTelegraph = false;
        this.windUpSound = "random.anvil_use";
        this.allowedBy = UserType.BOTH;

        this.activeAnimationName = "Ability_Guard_Active";
        this.counterAnimationName = "Ability_Guard_Counter";
    }

    public enum CounterType {
        FLAT,
        PERCENT;

        @Override
        public String toString() {
            switch (this) {
                case FLAT: return "ability.counter.flat";
                case PERCENT: return "ability.counter.percent";
                default: return name();
            }
        }
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
    public void onExecute(EntityLivingBase caster, EntityLivingBase target, World world) {
        lastAttacker = null;
        counterTriggered = false;
        lastDamageTaken = 0.0f;
        counterEligible = true;
    }

    @Override
    public void onActiveTick(EntityLivingBase caster, EntityLivingBase target, World world, int tick) {
        if (canCounter && counterEligible && counterTriggered && lastAttacker != null && !world.isRemote) {
            performCounter(caster, world);
        }

        if (tick > counterWindow && counterEligible) {
            counterEligible = false;
        }

        if (tick >= durationTicks) {
            signalCompletion();
        }

        // Just enough to see the counter animation
        if (counterTriggered && tick >= counterWindow + 10) {
            signalCompletion();
        }
    }

    public void onDamageTaken(EntityLivingBase caster, EntityLivingBase attacker, DamageSource source, float damage) {
        if (!isGuarding()) return;
        if (!canCounter || attacker == null || !counterEligible) return;
        if (!isDirectHit(source)) return;

        lastAttacker = attacker;
        lastDamageTaken = damage;
        counterTriggered = true;
        // Counter will be performed in onActiveTick - don't self-interrupt
    }

    private boolean isDirectHit(DamageSource source) {
        if (source == null) return false;
        if (source.isMagicDamage() || source.isFireDamage() || source.isExplosion()) return false;
        return source.getEntity() instanceof EntityLivingBase;
    }

    private void performCounter(EntityLivingBase caster, World world) {
        float counterDamage = counterType == AbilityGuard.CounterType.PERCENT
            ? lastDamageTaken * (counterValue / 100.0f)
            : counterValue;

        boolean wasHit = applyAbilityDamage(caster, lastAttacker, counterDamage, 0.5f);
        if (wasHit && counterSound != null && !counterSound.isEmpty()) {
            world.playSoundAtEntity(caster, counterSound, 1.0f, 1.2f);
        }
        // Use shared animation utility - only available for NPCs
        if (caster instanceof EntityNPCInterface) {
            if (getCounterAnimation() instanceof IAnimation) {
                ((EntityNPCInterface) caster).abilities.playAbilityAnimation((Animation) getCounterAnimation());
            }
        } else if (caster instanceof EntityPlayer) {
            if (getCounterAnimation() instanceof IAnimation) {
                PlayerData.get((EntityPlayer) caster).abilityData.playAbilityAnimation((Animation) getCounterAnimation());
            }
        }

        counterEligible = false;
        lastAttacker = null;
        lastDamageTaken = 0.0f;
    }

    private IAnimation getCounterAnimation() {
        if (AnimationController.Instance == null) return null;

        if (counterAnimationId > 0) {
            return AnimationController.Instance.get(counterAnimationId);
        }

        if (!counterAnimationName.isEmpty()) {
            return AnimationController.Instance.get(counterAnimationName, true);
        }

        return null;
    }

    /**
     * Get the damage reduction factor while guarding.
     * Returns 0.5 for 50% reduction.
     */
    public float getDamageReductionFactor() {
        if (isExecuting() && getPhase() == AbilityPhase.ACTIVE) {
            return damageReduction;
        }
        return 0.0f;
    }

    /**
     * Check if the caster is currently in guard stance.
     */
    public boolean isGuarding() {
        return isExecuting() && getPhase() == AbilityPhase.ACTIVE;
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
        nbt.setInteger("counterWindow", counterWindow);
        nbt.setString("counterType", counterType.name());
        nbt.setFloat("counterValue", counterValue);
        nbt.setString("counterSound", counterSound);
        nbt.setInteger("counterAnimationId", counterAnimationId);
        nbt.setString("counterAnimationName", counterAnimationName);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.durationTicks = nbt.hasKey("durationTicks") ? nbt.getInteger("durationTicks") : 60;
        this.damageReduction = nbt.hasKey("damageReduction") ? nbt.getFloat("damageReduction") : 0.5f;
        this.canCounter = nbt.hasKey("canCounter") && nbt.getBoolean("canCounter");
        this.counterWindow = nbt.hasKey("counterWindow") ? nbt.getInteger("counterWindow") : 10;
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
        this.counterSound = nbt.hasKey("counterSound") ? nbt.getString("counterSound") : "random.wood_click";
        this.counterAnimationId = nbt.hasKey("counterAnimationId") ? nbt.getInteger("counterAnimationId") : -1;
        this.counterAnimationName = nbt.hasKey("counterAnimationName") ?
            nbt.getString("counterAnimationName") :
            "Ability_Guard_Counter";
    }

    // Getters & Setters
    public int getDurationTicks() {
        return durationTicks;
    }

    public void setDurationTicks(int durationTicks) {
        this.durationTicks = Math.max(1, durationTicks);
    }

    @Override
    public int getCounterWindow() {
        return this.counterWindow;
    }

    @Override
    public void setCounterWindow(int ticks) {
        this.counterWindow = Math.min(durationTicks, ticks);
    }

    @Override
    public float getDamageReduction() {
        return damageReduction;
    }

    @Override
    public void setDamageReduction(float damageReduction) {
        this.damageReduction = damageReduction;
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

    public String getCounterAnimationName() {
        return counterAnimationName;
    }

    public void setCounterAnimationName(String name) {
        this.counterAnimationName = name;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public List<FieldDef> getFieldDefinitions() {
        return Arrays.asList(
            FieldDef.intField("ability.duration", this::getDurationTicks, this::setDurationTicks).range(1, 1000),
            FieldDef.floatField("ability.damageReduction", this::getDamageReduction, this::setDamageReduction),
            FieldDef.section("ability.section.counter"),
            FieldDef.boolField("gui.enabled", this::canCounter, this::setCanCounter).hover("ability.hover.canCounter"),
            FieldDef.enumField("gui.type", CounterType.class, this::getCounterTypeEnum, this::setCounterTypeEnum)
                .hover("ability.hover.counterType").visibleWhen(this::canCounter),
            FieldDef.row(
                FieldDef.floatField("gui.value", this::getCounterValue, this::setCounterValue)
                    .visibleWhen(this::canCounter),
                FieldDef.intField("ability.counterWindow", this::getCounterWindow, this::setCounterWindow)
                    .visibleWhen(this::canCounter)
            ),
            FieldDef.stringField("gui.sound", this::getCounterSound, this::setCounterSound)
                .visibleWhen(this::canCounter),
            AbilityFieldDefs.effectsListField("ability.effects", this::getEffects, this::setEffects)

        );
    }
}
