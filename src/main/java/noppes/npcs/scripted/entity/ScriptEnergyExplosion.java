package noppes.npcs.scripted.entity;

import kamkeel.npcs.entity.EntityEnergyExplosion;
import kamkeel.npcs.network.PacketHandler;
import kamkeel.npcs.network.packets.data.energyexplosion.EnergyExplosionSpawnPacket;
import noppes.npcs.api.entity.IEnergyExplosion;
import noppes.npcs.scripted.constants.EntityType;

import java.util.UUID;

/**
 * Script wrapper for standalone energy explosion entities.
 */
public class ScriptEnergyExplosion<T extends EntityEnergyExplosion> extends ScriptEnergyAbility<T> implements IEnergyExplosion {

    public ScriptEnergyExplosion(T entity) {
        super(entity);
    }

    // ==================== TYPE ====================

    @Override
    public int getType() {
        return EntityType.ENERGY_EXPLOSION;
    }

    @Override
    public boolean typeOf(int type) {
        return type == EntityType.ENERGY_EXPLOSION || super.typeOf(type);
    }

    // ==================== RADIUS & DURATION ====================

    @Override
    public float getRadius() {
        return entity.getExplosionRadius();
    }

    @Override
    public void setRadius(float radius) {
        entity.setExplosionRadius(radius);
    }

    @Override
    public int getDuration() {
        return entity.getDurationTicks();
    }

    // ==================== DAMAGE ====================

    @Override
    public float getDamage() {
        return entity.getDamage();
    }

    @Override
    public void setDamage(float damage) {
        entity.setDamage(damage);
    }

    @Override
    public float getKnockback() {
        return entity.getKnockback();
    }

    @Override
    public void setKnockback(float knockback) {
        entity.setKnockback(knockback);
    }

    @Override
    public float getKnockbackUp() {
        return entity.getKnockbackUp();
    }

    @Override
    public void setKnockbackUp(float knockbackUp) {
        entity.setKnockbackUp(knockbackUp);
    }

    @Override
    public float getDamageFalloff() {
        return entity.getDamageFalloff();
    }

    @Override
    public void setDamageFalloff(float falloff) {
        entity.setDamageFalloff(falloff);
    }

    // ==================== SPAWNING ====================

    @Override
    public void spawn() {
        if (entity.addedToChunk || entity.worldObj == null) return;

        if (!entity.worldObj.isRemote) {
            // Send visual to clients via packet (explosion is packet-driven, not a tracked entity)
            String instanceId = "energy_explosion_" + UUID.randomUUID();
            PacketHandler.Instance.sendToAll(new EnergyExplosionSpawnPacket(
                instanceId, entity.exportSpawnNBT()));

            // Spawn server-side for damage logic (if damage enabled)
            if (entity.isDamageEnabled()) {
                entity.worldObj.spawnEntityInWorld(entity);
            }
        }
    }
}
