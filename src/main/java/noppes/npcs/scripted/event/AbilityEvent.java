package noppes.npcs.scripted.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import kamkeel.npcs.controllers.data.ability.Ability;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.ability.IAbility;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.event.IAbilityEvent;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.scripted.NpcAPI;

/**
 * Events fired during NPC ability execution lifecycle.
 */
public class AbilityEvent extends NpcEvent implements IAbilityEvent {
    protected final Ability ability;
    protected final IEntityLivingBase target;

    public AbilityEvent(ICustomNpc npc, Ability ability, EntityLivingBase target) {
        super(npc);
        this.ability = ability;
        this.target = target != null ? (IEntityLivingBase) NpcAPI.Instance().getIEntity(target) : null;
    }

    @Override
    public IAbility getAbility() {
        return ability;
    }

    @Override
    public IEntityLivingBase getTarget() {
        return target;
    }

    @Override
    public String getHookName() {
        return "abilityEvent";
    }

    /**
     * Fired when an ability starts executing (enters WINDUP phase).
     */
    @Cancelable
    public static class StartEvent extends AbilityEvent implements IAbilityEvent.StartEvent {
        public StartEvent(ICustomNpc npc, Ability ability, EntityLivingBase target) {
            super(npc, ability, target);
        }

        @Override
        public String getHookName() {
            return EnumScriptType.ABILITY_START.function;
        }
    }

    /**
     * Fired when an ability enters the ACTIVE phase and performs its effect.
     */
    @Cancelable
    public static class ExecuteEvent extends AbilityEvent implements IAbilityEvent.ExecuteEvent {
        public ExecuteEvent(ICustomNpc npc, Ability ability, EntityLivingBase target) {
            super(npc, ability, target);
        }

        @Override
        public String getHookName() {
            return EnumScriptType.ABILITY_EXECUTE.function;
        }
    }

    /**
     * Fired when an ability is interrupted by damage.
     */
    public static class InterruptEvent extends AbilityEvent implements IAbilityEvent.InterruptEvent {
        private final IDamageSource damageSource;
        private final float damage;

        public InterruptEvent(ICustomNpc npc, Ability ability, EntityLivingBase target, DamageSource source, float damage) {
            super(npc, ability, target);
            this.damageSource = NpcAPI.Instance().getIDamageSource(source);
            this.damage = damage;
        }

        @Override
        public IDamageSource getDamageSource() {
            return damageSource;
        }

        @Override
        public float getDamage() {
            return damage;
        }

        @Override
        public String getHookName() {
            return EnumScriptType.ABILITY_INTERRUPT.function;
        }
    }

    /**
     * Fired when an ability completes its full execution cycle.
     */
    public static class CompleteEvent extends AbilityEvent implements IAbilityEvent.CompleteEvent {
        public CompleteEvent(ICustomNpc npc, Ability ability, EntityLivingBase target) {
            super(npc, ability, target);
        }

        @Override
        public String getHookName() {
            return EnumScriptType.ABILITY_COMPLETE.function;
        }
    }

    /**
     * Fired when an ability hits an entity with damage.
     * This is a cancelable event - canceling prevents the damage from being applied.
     */
    @Cancelable
    public static class HitEvent extends AbilityEvent implements IAbilityEvent.HitEvent {
        private final IEntityLivingBase hitEntity;
        private float damage;
        private float knockback;
        private float knockbackUp;

        public HitEvent(ICustomNpc npc, Ability ability, EntityLivingBase target,
                       EntityLivingBase hitEntity, float damage, float knockback, float knockbackUp) {
            super(npc, ability, target);
            this.hitEntity = hitEntity != null ? (IEntityLivingBase) NpcAPI.Instance().getIEntity(hitEntity) : null;
            this.damage = damage;
            this.knockback = knockback;
            this.knockbackUp = knockbackUp;
        }

        @Override
        public IEntityLivingBase getHitEntity() {
            return hitEntity;
        }

        @Override
        public float getDamage() {
            return damage;
        }

        @Override
        public void setDamage(float damage) {
            this.damage = damage;
        }

        @Override
        public float getKnockback() {
            return knockback;
        }

        @Override
        public void setKnockback(float knockback) {
            this.knockback = knockback;
        }

        @Override
        public float getKnockbackUp() {
            return knockbackUp;
        }

        @Override
        public void setKnockbackUp(float knockbackUp) {
            this.knockbackUp = knockbackUp;
        }

        @Override
        public String getHookName() {
            return EnumScriptType.ABILITY_HIT.function;
        }
    }

    /**
     * Fired every tick while an ability is executing.
     * Provides information about the current phase and tick.
     */
    public static class TickEvent extends AbilityEvent implements IAbilityEvent.TickEvent {
        private final int abilityPhase;
        private final int tick;

        public TickEvent(ICustomNpc npc, Ability ability, EntityLivingBase target, int phase, int tick) {
            super(npc, ability, target);
            this.abilityPhase = phase;
            this.tick = tick;
        }

        @Override
        public int getAbilityPhase() {
            return abilityPhase;
        }

        @Override
        public int getTick() {
            return tick;
        }

        @Override
        public String getHookName() {
            return EnumScriptType.ABILITY_TICK.function;
        }
    }
}
