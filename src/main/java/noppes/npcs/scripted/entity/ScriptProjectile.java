package noppes.npcs.scripted.entity;

import net.minecraft.util.MathHelper;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IProjectile;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.entity.EntityProjectile;
import noppes.npcs.scripted.CustomNPCsException;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.constants.EntityType;

public class ScriptProjectile<T extends EntityProjectile> extends ScriptEntity<T> implements IProjectile {
    protected T entity;

    public ScriptProjectile(T entity) {
        super(entity);
        this.entity = entity;
    }

    public IItemStack getItem() {
        return NpcAPI.Instance().getIItemStack(entity.getItemDisplay());
    }

    public void setItem(IItemStack item) {
        if(item == null)
            entity.setThrownItem(null);
        else
            entity.setThrownItem(item.getMCItemStack());
    }

    public boolean getHasGravity() {
        return entity.hasGravity();
    }

    public void setHasGravity(boolean bo) {
        entity.setHasGravity(bo);
    }

    public int getAccuracy() {
        return entity.accuracy;
    }

    public void setAccuracy(int accuracy) {
        entity.accuracy = accuracy;
    }

    public void setHeading(IEntity entity) {
        setHeading(entity.getX(), entity.getMCEntity().boundingBox.minY + (double)(entity.getHeight() / 2.0F), entity.getZ());
    }

    public void setHeading(double x, double y, double z) {
        x = x - entity.posX;
        y = y - entity.posY;
        z = z - entity.posZ;
        float varF = entity.hasGravity() ? (float) Math.sqrt(x * x + z * z) : 0.0F;
        float angle = entity.getAngleForXYZ(x, y, z, varF, false);
        float acc = (float) (20.0F - Math.floor(entity.accuracy / 5.0F));
        entity.setThrowableHeading(x, y, z, angle, acc);
    }

    public void setHeading(float yaw, float pitch) {
        entity.prevRotationYaw = entity.rotationYaw = yaw;
        entity.prevRotationPitch = entity.rotationPitch = pitch;

        double varX = (double)(-MathHelper.sin(yaw / 180.0F * (float)Math.PI) * MathHelper.cos(pitch / 180.0F * (float)Math.PI));
        double varZ = (double)(MathHelper.cos(yaw / 180.0F * (float)Math.PI) * MathHelper.cos(pitch / 180.0F * (float)Math.PI));
        double varY = (double)(-MathHelper.sin(pitch / 180.0F * (float)Math.PI));

        float acc = (float) (20.0F - Math.floor(entity.accuracy / 5.0F));
        entity.setThrowableHeading(varX, varY, varZ, -pitch, acc);
    }

    public int getType() {
        return EntityType.PROJECTILE;
    }

    public boolean typeOf(int type){
        return type == EntityType.PROJECTILE || super.typeOf(type);
    }

    public IEntity getThrower() {
        return NpcAPI.Instance().getIEntity(entity.getThrower());
    }

    public void enableEvents() {
        if(ScriptContainer.Current == null)
            throw new CustomNPCsException("Can only be called during scripts");

        if(!entity.scripts.contains(ScriptContainer.Current)) {
            entity.scripts.add(ScriptContainer.Current);
        }
    }
}
