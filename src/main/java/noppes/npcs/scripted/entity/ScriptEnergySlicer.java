package noppes.npcs.scripted.entity;

import kamkeel.npcs.entity.EntityEnergySlicer;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.api.entity.IEnergySlicer;
import noppes.npcs.scripted.constants.EntityType;

public class ScriptEnergySlicer<T extends EntityEnergySlicer> extends ScriptEnergyProjectile<T> implements IEnergySlicer {

    public ScriptEnergySlicer(T entity) {
        super(entity);
    }

    @Override
    public int getType() {
        return EntityType.ENERGY_SLICER;
    }

    @Override
    public boolean typeOf(int type) {
        return type == EntityType.ENERGY_SLICER || super.typeOf(type);
    }

    @Override
    public int getEnergyType() {
        return 4;
    }

    // ==================== SLICER-SPECIFIC ====================

    public float getSliceWidth() {
        return entity.getSliceWidth();
    }

    public void setSliceWidth(float width) {
        entity.setSliceWidth(width);
    }

    public float getSliceThickness() {
        return entity.getSliceThickness();
    }

    public void setSliceThickness(float thickness) {
        entity.setSliceThickness(thickness);
    }

    // ==================== FIRE ====================

    @Override
    protected void launchFromOwner(EntityLivingBase target) {
        entity.startMoving(target);
    }
}
