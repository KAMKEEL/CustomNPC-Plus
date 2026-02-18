package noppes.npcs.scripted.event.player;

import cpw.mods.fml.common.eventhandler.Cancelable;
import kamkeel.npcs.controllers.data.ability.Ability;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.ability.IAbility;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.event.IPlayerAbilityEvent;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.scripted.NpcAPI;

/**
 * Events fired during player ability execution lifecycle.
 */
public class PlayerAbilityEvent extends PlayerEvent implements IPlayerAbilityEvent {
    protected final Ability ability;
    protected final IEntityLivingBase target;

    public PlayerAbilityEvent(IPlayer player, Ability ability, EntityLivingBase target) {
        super(player);
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
        return "playerAbilityEvent";
    }

    /**
     * Fired when a player's ability starts executing (enters WINDUP phase).
     */
    @Cancelable
    public static class StartEvent extends PlayerAbilityEvent implements IPlayerAbilityEvent.StartEvent {
        public StartEvent(IPlayer player, Ability ability, EntityLivingBase target) {
            super(player, ability, target);
        }

        @Override
        public String getHookName() {
            return EnumScriptType.ABILITY_START.function;
        }
    }

    /**
     * Fired when a player's ability enters the ACTIVE phase and performs its effect.
     */
    @Cancelable
    public static class ExecuteEvent extends PlayerAbilityEvent implements IPlayerAbilityEvent.ExecuteEvent {
        public ExecuteEvent(IPlayer player, Ability ability, EntityLivingBase target) {
            super(player, ability, target);
        }

        @Override
        public String getHookName() {
            return EnumScriptType.ABILITY_EXECUTE.function;
        }
    }

    /**
     * Fired when a player's ability is interrupted by damage.
     */
    public static class InterruptEvent extends PlayerAbilityEvent implements IPlayerAbilityEvent.InterruptEvent {
        private final IDamageSource damageSource;
        private final float damage;

        public InterruptEvent(IPlayer player, Ability ability, EntityLivingBase target, DamageSource source, float damage) {
            super(player, ability, target);
            this.damageSource = source != null ? NpcAPI.Instance().getIDamageSource(source) : null;
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
     * Fired when a player's ability completes its full execution cycle.
     */
    public static class CompleteEvent extends PlayerAbilityEvent implements IPlayerAbilityEvent.CompleteEvent {
        public CompleteEvent(IPlayer player, Ability ability, EntityLivingBase target) {
            super(player, ability, target);
        }

        @Override
        public String getHookName() {
            return EnumScriptType.ABILITY_COMPLETE.function;
        }
    }

    /**
     * Fired every tick while a player's ability is executing.
     */
    public static class TickEvent extends PlayerAbilityEvent implements IPlayerAbilityEvent.TickEvent {
        private final int abilityPhase;
        private final int tick;

        public TickEvent(IPlayer player, Ability ability, EntityLivingBase target, int phase, int tick) {
            super(player, ability, target);
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

    /**
     * Fired when a player's ability hits an entity with damage.
     * Canceling prevents the damage from being applied.
     */
    @Cancelable
    public static class HitEvent extends PlayerAbilityEvent implements IPlayerAbilityEvent.HitEvent {
        private final IEntityLivingBase hitEntity;
        private float damage;
        private float knockback;
        private float knockbackUp;

        public HitEvent(IPlayer player, Ability ability, EntityLivingBase target,
                        EntityLivingBase hitEntity, float damage, float knockback, float knockbackUp) {
            super(player, ability, target);
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
}
