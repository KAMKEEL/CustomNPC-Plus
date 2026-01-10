package kamkeel.npcs.controllers.data.ability.type;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import kamkeel.npcs.controllers.data.ability.Ability;
import noppes.npcs.client.gui.advanced.IAbilityConfigCallback;
import noppes.npcs.client.gui.advanced.SubGuiAbilityConfig;
import noppes.npcs.client.gui.advanced.ability.SubGuiAbilityGuard;
import kamkeel.npcs.controllers.data.ability.TargetingMode;
import kamkeel.npcs.controllers.data.ability.telegraph.TelegraphType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.Random;

/**
 * Guard ability: Defensive stance that reduces incoming damage.
 * Can optionally counter-attack when hit.
 */
public class AbilityGuard extends Ability {

    private static final Random RANDOM = new Random();

    // Type-specific parameters
    private float damageReduction = 0.5f;
    private boolean canCounter = false;
    private float counterDamage = 10.0f;
    private float counterChance = 0.3f;

    // Runtime state
    private transient EntityLivingBase lastAttacker;
    private transient boolean counterTriggered;

    public AbilityGuard() {
        this.typeId = "cnpc:guard";
        this.name = "Guard";
        this.targetingMode = TargetingMode.SELF;
        this.lockMovement = true;
        this.cooldownTicks = 120;
        this.windUpTicks = 10;
        this.activeTicks = 60;
        this.recoveryTicks = 20;
        this.interruptible = false; // Hard to interrupt while guarding
        // No telegraph for guard - it's a defensive stance
        this.telegraphType = TelegraphType.NONE;
        this.showTelegraph = false;
    }

    @Override
    public boolean hasTypeSettings() { return true; }

    @Override
    @SideOnly(Side.CLIENT)
    public SubGuiAbilityConfig createConfigGui(
            IAbilityConfigCallback callback) {
        return new SubGuiAbilityGuard(this, callback);
    }

    @Override
    public boolean isTargetingModeLocked() { return true; }

    @Override
    public TargetingMode[] getAllowedTargetingModes() {
        return new TargetingMode[] { TargetingMode.SELF };
    }

    @Override
    public void onExecute(EntityNPCInterface npc, EntityLivingBase target, World world) {
        lastAttacker = null;
        counterTriggered = false;
    }

    @Override
    public void onActiveTick(EntityNPCInterface npc, EntityLivingBase target, World world, int tick) {
        // Counter attack logic if triggered
        if (canCounter && counterTriggered && lastAttacker != null && !world.isRemote) {
            // Apply counter damage with scripted event support
            boolean wasHit = applyAbilityDamage(npc, lastAttacker, counterDamage, 0.5f, 0);

            // Play counter sound if hit wasn't cancelled
            if (wasHit) {
                world.playSoundAtEntity(npc, "random.wood_click", 1.0f, 1.2f);
            }

            counterTriggered = false;
            lastAttacker = null;
        }
    }

    /**
     * Called externally when the NPC takes damage while guarding.
     * This should be called from the damage handling code.
     *
     * @param attacker The entity that attacked
     * @param damage The damage amount (after reduction)
     */
    public void onDamageTaken(EntityLivingBase attacker, float damage) {
        if (attacker != null && canCounter && RANDOM.nextFloat() < counterChance) {
            lastAttacker = attacker;
            counterTriggered = true;
        }
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
    }

    @Override
    public float getTelegraphRadius() {
        return 0; // No telegraph for guard
    }

    @Override
    public void writeTypeNBT(NBTTagCompound nbt) {
        nbt.setFloat("damageReduction", damageReduction);
        nbt.setBoolean("canCounter", canCounter);
        nbt.setFloat("counterDamage", counterDamage);
        nbt.setFloat("counterChance", counterChance);
    }

    @Override
    public void readTypeNBT(NBTTagCompound nbt) {
        this.damageReduction = nbt.hasKey("damageReduction") ? nbt.getFloat("damageReduction") : 0.5f;
        this.canCounter = nbt.hasKey("canCounter") && nbt.getBoolean("canCounter");
        this.counterDamage = nbt.hasKey("counterDamage") ? nbt.getFloat("counterDamage") : 10.0f;
        this.counterChance = nbt.hasKey("counterChance") ? nbt.getFloat("counterChance") : 0.3f;
    }

    // Getters & Setters
    public float getDamageReduction() { return damageReduction; }
    public void setDamageReduction(float damageReduction) { this.damageReduction = damageReduction; }

    public boolean isCanCounter() { return canCounter; }
    public void setCanCounter(boolean canCounter) { this.canCounter = canCounter; }

    public float getCounterDamage() { return counterDamage; }
    public void setCounterDamage(float counterDamage) { this.counterDamage = counterDamage; }

    public float getCounterChance() { return counterChance; }
    public void setCounterChance(float counterChance) { this.counterChance = counterChance; }
}
