package noppes.npcs.scripted.entity;

import kamkeel.npcs.entity.EntityEnergyDome;
import noppes.npcs.api.entity.IEnergyDome;
import noppes.npcs.scripted.constants.EntityType;

public class ScriptEnergyDome<T extends EntityEnergyDome> extends ScriptEnergyBarrier<T> implements IEnergyDome {

    public ScriptEnergyDome(T entity) {
        super(entity);
    }

    @Override
    public int getType() {
        return EntityType.ENERGY_DOME;
    }

    @Override
    public boolean typeOf(int type) {
        return type == EntityType.ENERGY_DOME || super.typeOf(type);
    }

    @Override
    public int getBarrierType() {
        return 0;
    }

    // ==================== DOME-SPECIFIC ====================

    public float getDomeRadius() {
        return entity.getDomeRadius();
    }

    public void setDomeRadius(float radius) {
        entity.setDomeRadius(radius);
    }
}
