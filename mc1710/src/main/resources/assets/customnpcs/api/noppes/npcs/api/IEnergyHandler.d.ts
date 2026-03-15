/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api
 */

/**
 * Central controller for creating energy entities and routing energy damage.
 * Access via API.getEnergyController().
 *
 * All factory methods create entities that are NOT spawned in the world.
 * Configure the entity, then call fire() (projectiles) or spawn() (zones/sweepers/panels)
 * to place it in the world.
  * @javaFqn noppes.npcs.api.IEnergyHandler
*/
export interface IEnergyHandler {
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
    createOrb(world: import('./IWorld').IWorld, owner: import('./IEntity').IEntity, x: import('./double').double, y: import('./double').double, z: import('./double').double, size: import('./float').float): import('./IEnergyOrb').IEnergyOrb;
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
    createBeam(world: import('./IWorld').IWorld, owner: import('./IEntity').IEntity, x: import('./double').double, y: import('./double').double, z: import('./double').double, beamWidth: import('./float').float, headSize: import('./float').float): import('./IEnergyBeam').IEnergyBeam;
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
    createDisc(world: import('./IWorld').IWorld, owner: import('./IEntity').IEntity, x: import('./double').double, y: import('./double').double, z: import('./double').double, radius: import('./float').float, thickness: import('./float').float): import('./IEnergyDisc').IEnergyDisc;
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
    createLaser(world: import('./IWorld').IWorld, owner: import('./IEntity').IEntity, x: import('./double').double, y: import('./double').double, z: import('./double').double, laserWidth: import('./float').float): import('./IEnergyLaser').IEnergyLaser;
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
    createHazard(world: import('./IWorld').IWorld, owner: import('./IEntity').IEntity, x: import('./double').double, y: import('./double').double, z: import('./double').double): import('./IEnergyZone').IEnergyZone;
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
    createTrap(world: import('./IWorld').IWorld, owner: import('./IEntity').IEntity, x: import('./double').double, y: import('./double').double, z: import('./double').double): import('./IEnergyZone').IEnergyZone;
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
    createSweeper(world: import('./IWorld').IWorld, owner: import('./IEntity').IEntity, x: import('./double').double, y: import('./double').double, z: import('./double').double): import('./IEnergySweeper').IEnergySweeper;
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
    createPanel(world: import('./IWorld').IWorld, owner: import('./IEntity').IEntity, x: import('./double').double, y: import('./double').double, z: import('./double').double): import('./IEnergyPanel').IEnergyPanel;
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
    createExplosion(world: import('./IWorld').IWorld, owner: import('./IEntity').IEntity, x: import('./double').double, y: import('./double').double, z: import('./double').double, radius: import('./float').float): import('./entity/IEnergyExplosion').IEnergyExplosion;
}
