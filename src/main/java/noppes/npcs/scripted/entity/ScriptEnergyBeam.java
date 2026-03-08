package noppes.npcs.scripted.entity;

import kamkeel.npcs.entity.EntityAbilityBeam;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.api.entity.IEnergyBeam;
import noppes.npcs.scripted.constants.EntityType;

public class ScriptEnergyBeam<T extends EntityAbilityBeam> extends ScriptEnergyProjectile<T> implements IEnergyBeam {

    public ScriptEnergyBeam(T entity) {
        super(entity);
    }

    @Override
    public int getType() {
        return EntityType.ENERGY_BEAM;
    }

    @Override
    public boolean typeOf(int type) {
        return type == EntityType.ENERGY_BEAM || super.typeOf(type);
    }

    @Override
    public int getEnergyType() {
        return 1;
    }

    // ==================== BEAM-SPECIFIC ====================

    public float getBeamWidth() {
        return entity.getBeamWidth();
    }

    public void setBeamWidth(float width) {
        entity.setBeamWidth(width);
    }

    public float getHeadSize() {
        return entity.getHeadSize();
    }

    public void setHeadSize(float size) {
        entity.setHeadSize(size);
    }

    public boolean isAttachedToOwner() {
        return entity.isAttachedToOwner();
    }

    public void setAttachedToOwner(boolean attached) {
        entity.setAttachedToOwner(attached);
    }

    public boolean shouldRenderTailOrb() {
        return entity.shouldRenderTailOrb();
    }

    // ==================== FIRE ====================

    @Override
    protected void launchFromOwner(EntityLivingBase target) {
        entity.startFiring(target);
    }
}
