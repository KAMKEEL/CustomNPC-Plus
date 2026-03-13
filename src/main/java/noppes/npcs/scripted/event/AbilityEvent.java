package noppes.npcs.scripted.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import kamkeel.npcs.controllers.data.ability.Ability;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.ability.IAbility;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.event.IAbilityEvent;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.scripted.NpcAPI;

/**
 * Unified ability events for both NPCs and Players.
 */
public class AbilityEvent extends CustomNPCsEvent implements IAbilityEvent {
    protected final IEntityLivingBase entity;
    public final IPlayer player;
    public final ICustomNpc<?> npc;
    protected final Ability ability;
    protected final IEntityLivingBase target;
    private transient IAbility abilityCopy;

    public AbilityEvent(EntityLivingBase entity, Ability ability, EntityLivingBase target) {
        this.entity = entity != null ? (IEntityLivingBase) NpcAPI.Instance().getIEntity(entity) : null;
        this.ability = ability;
        this.target = target != null ? (IEntityLivingBase) NpcAPI.Instance().getIEntity(target) : null;
        this.player = (this.entity instanceof IPlayer) ? (IPlayer) this.entity : null;
        this.npc = (this.entity instanceof ICustomNpc) ? (ICustomNpc<?>) this.entity : null;
    }

    @Override
    public IEntityLivingBase getEntity() {
        return entity;
    }

    @Override
    public IPlayer getPlayer() {
        return player;
    }

    @Override
    public ICustomNpc<?> getNpc() {
        return npc;
    }

    @Override
    public boolean isNPC() {
        return entity instanceof ICustomNpc;
    }

    @Override
    public IAbility getAbility() {
        if (abilityCopy == null && ability != null) {
            abilityCopy = ability.deepCopy();
        }
        return abilityCopy;
    }

    @Override
    public IEntityLivingBase getTarget() {
        return target;
    }

    @Override
    public String getHookName() {
        return "abilityEvent";
    }

    @Cancelable
    public static class StartEvent extends AbilityEvent implements IAbilityEvent.StartEvent {
        public StartEvent(EntityLivingBase entity, Ability ability, EntityLivingBase target) {
            super(entity, ability, target);
        }

        @Override
        public String getHookName() {
            return EnumScriptType.ABILITY_START.function;
        }
    }

    @Cancelable
    public static class ExecuteEvent extends AbilityEvent implements IAbilityEvent.ExecuteEvent {
        public ExecuteEvent(EntityLivingBase entity, Ability ability, EntityLivingBase target) {
            super(entity, ability, target);
        }

        @Override
        public String getHookName() {
            return EnumScriptType.ABILITY_EXECUTE.function;
        }
    }

    public static class InterruptEvent extends AbilityEvent implements IAbilityEvent.InterruptEvent {
        private final IDamageSource damageSource;
        private final float damage;

        public InterruptEvent(EntityLivingBase entity, Ability ability, EntityLivingBase target, DamageSource source, float damage) {
            super(entity, ability, target);
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

    public static class CompleteEvent extends AbilityEvent implements IAbilityEvent.CompleteEvent {
        public CompleteEvent(EntityLivingBase entity, Ability ability, EntityLivingBase target) {
            super(entity, ability, target);
        }

        @Override
        public String getHookName() {
            return EnumScriptType.ABILITY_COMPLETE.function;
        }
    }

    @Cancelable
    public static class HitEvent extends AbilityEvent implements IAbilityEvent.HitEvent {
        private final IEntityLivingBase hitEntity;
        private float damage;
        private float knockback;
        private float knockbackUp;

        public HitEvent(EntityLivingBase entity, Ability ability, EntityLivingBase target,
                        EntityLivingBase hitEntity, float damage, float knockback, float knockbackUp) {
            super(entity, ability, target);
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

    public static class TickEvent extends AbilityEvent implements IAbilityEvent.TickEvent {
        private final int abilityPhase;
        private final int tick;

        public TickEvent(EntityLivingBase entity, Ability ability, EntityLivingBase target, int phase, int tick) {
            super(entity, ability, target);
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

    @Cancelable
    public static class DefendEvent extends AbilityEvent implements IAbilityEvent.DefendEvent {
        private final IEntityLivingBase attacker;
        private final IEntityLivingBase lastAttacker;
        private float damage;

        public DefendEvent(EntityLivingBase entity, Ability ability, EntityLivingBase attacker,
                        EntityLivingBase lastAttacker, float damage) {
            super(entity, ability, null);
            this.attacker = attacker != null ? (IEntityLivingBase) NpcAPI.Instance().getIEntity(attacker) : null;
            this.lastAttacker = lastAttacker != null ? (IEntityLivingBase) NpcAPI.Instance().getIEntity(lastAttacker) : null;
            this.damage = damage;
        }

        @Override
        public IEntityLivingBase getAttacker() {
            return attacker;
        }

        @Override
        public IEntityLivingBase getLastAttacker() {
            return lastAttacker;
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
        public String getHookName() {
            return EnumScriptType.ABILITY_DEFEND.function;
        }
    }

    @Cancelable
    public static class ToggleEvent extends AbilityEvent implements IAbilityEvent.ToggleEvent {
        private final int oldState;
        private final int newState;

        public ToggleEvent(EntityLivingBase entity, Ability ability, int oldState, int newState) {
            super(entity, ability, null);
            this.oldState = oldState;
            this.newState = newState;
        }

        @Override
        public boolean isTogglingOn() {
            return newState > 0;
        }

        @Override
        public int getOldState() {
            return oldState;
        }

        @Override
        public int getNewState() {
            return newState;
        }

        @Override
        public String getHookName() {
            return EnumScriptType.ABILITY_TOGGLE.function;
        }
    }

    public static class ToggleUpdateEvent extends AbilityEvent implements IAbilityEvent.ToggleUpdateEvent {
        private final int tick;
        private final int state;
        private boolean enabled = true;

        public ToggleUpdateEvent(EntityLivingBase entity, Ability ability, int tick, int state) {
            super(entity, ability, null);
            this.tick = tick;
            this.state = state;
        }

        @Override
        public int getTick() {
            return tick;
        }

        @Override
        public int getState() {
            return state;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public String getHookName() {
            return EnumScriptType.ABILITY_TOGGLE_TICK.function;
        }
    }
}
