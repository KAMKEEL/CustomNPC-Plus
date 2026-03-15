package noppes.npcs.scripted.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.entity.IEnergyBarrier;
import noppes.npcs.api.entity.IEnergyProjectile;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.event.IEnergyBarrierEvent;
import noppes.npcs.constants.EnumScriptType;

public class EnergyBarrierEvent extends CustomNPCsEvent implements IEnergyBarrierEvent {
    public final IEnergyBarrier barrier;
    public final IEntity owner;

    public EnergyBarrierEvent(IEnergyBarrier barrier) {
        this.barrier = barrier;
        this.owner = barrier.getOwner();
    }

    @Override
    public IEnergyBarrier getBarrier() {
        return barrier;
    }

    @Override
    public IEntity getOwner() {
        return owner;
    }

    @Override
    public String getHookName() {
        return "energyBarrierEvent";
    }

    public static class SpawnedEvent extends EnergyBarrierEvent implements IEnergyBarrierEvent.SpawnedEvent {
        public SpawnedEvent(IEnergyBarrier barrier) {
            super(barrier);
        }

        @Override
        public String getHookName() {
            return EnumScriptType.ENERGY_BARRIER_SPAWNED.function;
        }
    }

    public static class UpdateEvent extends EnergyBarrierEvent implements IEnergyBarrierEvent.UpdateEvent {
        public UpdateEvent(IEnergyBarrier barrier) {
            super(barrier);
        }

        @Override
        public String getHookName() {
            return EnumScriptType.ENERGY_BARRIER_TICK.function;
        }
    }

    @Cancelable
    public static class HitEvent extends EnergyBarrierEvent implements IEnergyBarrierEvent.HitEvent {
        private final IEnergyProjectile projectile;
        private float damage;

        public HitEvent(IEnergyBarrier barrier, IEnergyProjectile projectile, float damage) {
            super(barrier);
            this.projectile = projectile;
            this.damage = damage;
        }

        @Override
        public IEnergyProjectile getProjectile() {
            return projectile;
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
            return EnumScriptType.ENERGY_BARRIER_HIT.function;
        }
    }

    public static class DestroyedEvent extends EnergyBarrierEvent implements IEnergyBarrierEvent.DestroyedEvent {
        public DestroyedEvent(IEnergyBarrier barrier) {
            super(barrier);
        }

        @Override
        public String getHookName() {
            return EnumScriptType.ENERGY_BARRIER_DESTROYED.function;
        }
    }
}
