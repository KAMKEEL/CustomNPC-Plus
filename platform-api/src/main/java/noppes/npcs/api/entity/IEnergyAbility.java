package noppes.npcs.api.entity;

import noppes.npcs.api.INbt;
import noppes.npcs.api.handler.data.IMagicData;

/**
 * Base interface for all energy ability entities (Projectiles, Barriers).
 * Contains shared display, lightning, owner, and charging properties.
 */
public interface IEnergyAbility extends IEntity {

    // ==================== OWNER ====================

    /** @return Entity ID of the caster who spawned this energy ability. */
    int getOwnerEntityId();

    /** @return The caster entity who spawned this energy ability, or null if not found. */
    IEntity getOwner();

    // ==================== DISPLAY ====================

    /** @return Inner (core) color as a packed RGB integer. */
    int getInnerColor();

    /** @param color Inner color as a packed RGB integer. */
    void setInnerColor(int color);

    /** @return Alpha (opacity) of the inner color layer (0.0-1.0). */
    float getInnerAlpha();

    /** @param alpha Inner color opacity (0.0-1.0). */
    void setInnerAlpha(float alpha);

    /** @return Outer (glow) color as a packed RGB integer. */
    int getOuterColor();

    /** @param color Outer color as a packed RGB integer. */
    void setOuterColor(int color);

    /** @return Whether the outer color layer is rendered. */
    boolean isOuterColorEnabled();

    /** @param enabled Whether to render the outer color layer. */
    void setOuterColorEnabled(boolean enabled);

    /** @return Width of the outer color layer relative to the entity size. */
    float getOuterColorWidth();

    /** @param width Outer color layer width. */
    void setOuterColorWidth(float width);

    /** @return Alpha (opacity) of the outer color layer (0.0-1.0). */
    float getOuterColorAlpha();

    /** @param alpha Outer color opacity (0.0-1.0). */
    void setOuterColorAlpha(float alpha);

    // ==================== LIGHTNING ====================

    /** @return Whether the lightning visual effect is enabled. */
    boolean hasLightningEffect();

    /** @param enabled Whether to render the lightning effect. */
    void setLightningEffect(boolean enabled);

    /** @return Density of lightning arcs (higher = more arcs). */
    float getLightningDensity();

    /** @param density Lightning arc density. */
    void setLightningDensity(float density);

    /** @return Radius of the lightning effect around the entity, in blocks. */
    float getLightningRadius();

    /** @param radius Lightning effect radius in blocks. */
    void setLightningRadius(float radius);

    /** @return Fade-out time for lightning arcs in ticks. */
    int getLightningFadeTime();

    /** @param ticks Lightning fade-out time in ticks. */
    void setLightningFadeTime(int ticks);

    // ==================== STATE ====================

    /** @return Whether this energy ability is currently in its charging phase. */
    boolean isCharging();

    /** @return Charge progress as a fraction (0.0-1.0). */
    float getChargeProgress();

    /** @return Whether this entity ignores target invulnerability frames when dealing damage. */
    boolean isIgnoreIFrames();

    /** @param ignore Whether this entity should ignore target invulnerability frames when dealing damage. */
    void setIgnoreIFrames(boolean ignore);

    // ==================== CUSTOM DAMAGE DATA ====================

    /**
     * Get the custom damage data attached to this energy entity.
     * Used by addon handlers (e.g. DBC Addon) to carry damage configuration
     * directly on the entity, enabling DBC damage scaling without a sourceAbility.
     * @return Custom damage data as INbt, or null if none set.
     */
    INbt getDamageData();

    /**
     * Set custom damage data on this energy entity.
     * @param data Custom damage data as INbt. Pass null to clear.
     */
    void setDamageData(INbt data);

    /**
     * Get this entity's magic data. Defines magic types for outgoing damage splits
     * or barrier defense interactions. Inherited from the source ability on spawn.
     * @return the entity's magic data
     */
    IMagicData getMagicData();

    /**
     * Set magic data on this energy entity. Overrides any inherited ability magic.
     * @param data Magic data to set.
     */
    void setMagicData(IMagicData data);
}
