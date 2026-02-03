package noppes.npcs.scripted.event.player;

import cpw.mods.fml.common.eventhandler.Cancelable;
import kamkeel.npcs.controllers.data.ability.Ability;
import net.minecraft.entity.EntityLivingBase;
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
