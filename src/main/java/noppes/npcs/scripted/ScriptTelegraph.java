package noppes.npcs.scripted;

import kamkeel.npcs.controllers.TelegraphController;
import kamkeel.npcs.controllers.data.telegraph.Telegraph;
import kamkeel.npcs.controllers.data.telegraph.TelegraphInstance;
import kamkeel.npcs.controllers.data.telegraph.TelegraphType;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import noppes.npcs.api.ITelegraph;
import noppes.npcs.api.ITelegraphInstance;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntity;

/**
 * Script wrapper for Telegraph configuration.
 * Implements ITelegraph API interface.
 */
public class ScriptTelegraph implements ITelegraph {

    private final Telegraph telegraph;

    public ScriptTelegraph(Telegraph telegraph) {
        this.telegraph = telegraph;
    }

    /**
     * Get the underlying Telegraph object.
     */
    public Telegraph getMCTelegraph() {
        return telegraph;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TYPE
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public String getType() {
        return telegraph.getType().name();
    }

    @Override
    public void setType(String type) {
        if (type == null) return;
        try {
            telegraph.setType(TelegraphType.valueOf(type.toUpperCase()));
        } catch (IllegalArgumentException ignored) {
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SHAPE PARAMETERS
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public float getRadius() {
        return telegraph.getRadius();
    }

    @Override
    public void setRadius(float radius) {
        telegraph.setRadius(radius);
    }

    @Override
    public float getInnerRadius() {
        return telegraph.getInnerRadius();
    }

    @Override
    public void setInnerRadius(float innerRadius) {
        telegraph.setInnerRadius(innerRadius);
    }

    @Override
    public float getLength() {
        return telegraph.getLength();
    }

    @Override
    public void setLength(float length) {
        telegraph.setLength(length);
    }

    @Override
    public float getWidth() {
        return telegraph.getWidth();
    }

    @Override
    public void setWidth(float width) {
        telegraph.setWidth(width);
    }

    @Override
    public float getAngle() {
        return telegraph.getAngle();
    }

    @Override
    public void setAngle(float angle) {
        telegraph.setAngle(angle);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TIMING
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public int getDuration() {
        return telegraph.getDurationTicks();
    }

    @Override
    public void setDuration(int ticks) {
        telegraph.setDurationTicks(ticks);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // COLORS
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public int getColor() {
        return telegraph.getColor();
    }

    @Override
    public void setColor(int argb) {
        telegraph.setColor(argb);
    }

    @Override
    public int getWarningColor() {
        return telegraph.getWarningColor();
    }

    @Override
    public void setWarningColor(int argb) {
        telegraph.setWarningColor(argb);
    }

    @Override
    public int getWarningStartTick() {
        return telegraph.getWarningStartTick();
    }

    @Override
    public void setWarningStartTick(int tick) {
        telegraph.setWarningStartTick(tick);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ANIMATION
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public boolean isAnimated() {
        return telegraph.isAnimated();
    }

    @Override
    public void setAnimated(boolean animated) {
        telegraph.setAnimated(animated);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // POSITIONING
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public float getHeightOffset() {
        return telegraph.getHeightOffset();
    }

    @Override
    public void setHeightOffset(float offset) {
        telegraph.setHeightOffset(offset);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SPAWN METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public ITelegraphInstance spawn(IWorld world, double x, double y, double z) {
        return spawn(world, x, y, z, 0);
    }

    @Override
    public ITelegraphInstance spawn(IWorld world, double x, double y, double z, float yaw) {
        if (world == null || world.getMCWorld() == null) return null;
        World mcWorld = (World) world.getMCWorld();
        TelegraphInstance instance = TelegraphController.Instance.spawn(telegraph, mcWorld, x, y, z, yaw);
        return new ScriptTelegraphInstance(instance);
    }

    @Override
    public ITelegraphInstance spawn(IEntity entity) {
        return spawn(entity, 0);
    }

    @Override
    public ITelegraphInstance spawn(IEntity entity, float yaw) {
        if (entity == null || entity.getMCEntity() == null) return null;
        Entity mcEntity = (Entity) entity.getMCEntity();
        TelegraphInstance instance = TelegraphController.Instance.spawn(telegraph, mcEntity, yaw);
        return new ScriptTelegraphInstance(instance);
    }
}
