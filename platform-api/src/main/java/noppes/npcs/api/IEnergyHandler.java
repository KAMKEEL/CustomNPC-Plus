package noppes.npcs.api;

import noppes.npcs.api.entity.*;

/**
 * Central controller for creating energy entities and routing energy damage.
 * Access via API.getEnergyController().
 *
 * All factory methods create entities that are NOT spawned in the world.
 * Configure the entity, then call fire() (projectiles) or spawn() (zones/sweepers/panels)
 * to place it in the world.
 */
public interface IEnergyHandler {

    // ==================== PROJECTILE CREATION ====================

    /**
     * Create an energy orb projectile. Not spawned until fire() is called.
     * @param world the world
     * @param owner the owning entity
     * @param x X position
     * @param y Y position
     * @param z Z position
     * @param size the orb size
     * @return the energy orb entity
     */
    IEnergyOrb createOrb(IWorld world, IEntity owner, double x, double y, double z, float size);

    /**
     * Create an energy beam projectile. Not spawned until fire() is called.
     * @param world the world
     * @param owner the owning entity
     * @param x X position
     * @param y Y position
     * @param z Z position
     * @param beamWidth the beam width
     * @param headSize the head size
     * @return the energy beam entity
     */
    IEnergyBeam createBeam(IWorld world, IEntity owner, double x, double y, double z, float beamWidth, float headSize);

    /**
     * Create an energy disc projectile. Not spawned until fire() is called.
     * @param world the world
     * @param owner the owning entity
     * @param x X position
     * @param y Y position
     * @param z Z position
     * @param radius the disc radius
     * @param thickness the disc thickness
     * @return the energy disc entity
     */
    IEnergyDisc createDisc(IWorld world, IEntity owner, double x, double y, double z, float radius, float thickness);

    /**
     * Create an energy laser projectile. Not spawned until fire() is called.
     * @param world the world
     * @param owner the owning entity
     * @param x X position
     * @param y Y position
     * @param z Z position
     * @param laserWidth the laser width
     * @return the energy laser entity
     */
    IEnergyLaser createLaser(IWorld world, IEntity owner, double x, double y, double z, float laserWidth);

    // ==================== ZONE CREATION ====================

    /**
     * Create a hazard zone entity with defaults. Not spawned until spawn() is called.
     * Hazards deal continuous damage to entities within the zone.
     * @param world the world
     * @param owner the owning entity
     * @param x X position
     * @param y Y position
     * @param z Z position
     * @return the energy zone entity
     */
    IEnergyZone createHazard(IWorld world, IEntity owner, double x, double y, double z);

    /**
     * Create a trap zone entity with defaults. Not spawned until spawn() is called.
     * Traps trigger when entities enter and deal burst damage.
     * @param world the world
     * @param owner the owning entity
     * @param x X position
     * @param y Y position
     * @param z Z position
     * @return the energy zone entity
     */
    IEnergyZone createTrap(IWorld world, IEntity owner, double x, double y, double z);

    // ==================== SWEEPER CREATION ====================

    /**
     * Create a sweeper entity with defaults. Not spawned until spawn() is called.
     * Sweepers rotate a beam that damages entities in its path.
     * @param world the world
     * @param owner the owning entity
     * @param x X position
     * @param y Y position
     * @param z Z position
     * @return the energy sweeper entity
     */
    IEnergySweeper createSweeper(IWorld world, IEntity owner, double x, double y, double z);

    // ==================== PANEL CREATION ====================

    /**
     * Create a panel entity with defaults. Not spawned until spawn() is called.
     * Panels are flat energy barriers that can deal damage on contact.
     * @param world the world
     * @param owner the owning entity
     * @param x X position
     * @param y Y position
     * @param z Z position
     * @return the energy panel entity
     */
    IEnergyPanel createPanel(IWorld world, IEntity owner, double x, double y, double z);

    // ==================== EXPLOSION CREATION ====================

    /**
     * Create an energy explosion entity. Not spawned until spawn() is called.
     * Visual-only by default. Set damage &gt; 0 to enable area damage with falloff.
     * @param world the world
     * @param owner the owning entity
     * @param x X position
     * @param y Y position
     * @param z Z position
     * @param radius the explosion radius
     * @return the energy explosion entity
     */
    IEnergyExplosion createExplosion(IWorld world, IEntity owner, double x, double y, double z, float radius);
}
