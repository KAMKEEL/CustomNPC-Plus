package kamkeel.npcs.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import kamkeel.npcs.controllers.data.ability.data.EnergyBarrierData;
import kamkeel.npcs.controllers.data.ability.data.EnergyDisplayData;
import kamkeel.npcs.controllers.data.ability.data.EnergyLightningData;
import kamkeel.npcs.controllers.data.ability.data.EnergyPanelData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NpcDamageSource;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.List;

/**
 * Energy Panel entity - a flat rectangular barrier used by Wall and Shield abilities.
 * Supports three modes: PLACED (stationary), HELD (tracks caster), LAUNCHED (moves forward).
 * Extends EntityEnergyBarrier for shared barrier logic.
 */
public class EntityEnergyPanel extends EntityEnergyBarrier {

    public enum PanelMode {
        PLACED,     // Stationary wall
        HELD,       // Follows caster, rotates with caster look
        LAUNCHED    // Moves forward, deals damage/knockback on contact
    }

    // ==================== PANEL-SPECIFIC PROPERTIES ====================
    protected EnergyPanelData panelData = new EnergyPanelData();
    protected PanelMode mode = PanelMode.PLACED;
    protected float panelYaw = 0.0f; // Rotation of the panel face (degrees)
    protected float prevPanelYaw = 0.0f; // Previous tick yaw for smooth interpolation

    // ==================== CHARGING (panel-specific targets) ====================
    protected float targetPanelWidth;
    protected float targetPanelHeight;

    public EntityEnergyPanel(World world) {
        super(world);
        this.setSize(0.5f, 0.5f);
    }

    public EntityEnergyPanel(World world, EntityLivingBase owner, double x, double y, double z,
                             float yaw, PanelMode mode,
                             EnergyDisplayData display, EnergyLightningData lightning,
                             EnergyBarrierData barrier, EnergyPanelData panel) {
        this(world);
        this.setPosition(x, y + panel.heightOffset, z);
        this.ownerEntityId = owner.getEntityId();
        this.panelYaw = yaw;
        this.prevPanelYaw = yaw;
        this.mode = mode;
        this.displayData = display;
        this.lightningData = lightning;
        this.barrierData = barrier;
        this.panelData = panel;
        this.currentHealth = barrier.maxHealth;

        // For launched panels, set initial velocity
        if (mode == PanelMode.LAUNCHED) {
            float yawRad = (float) Math.toRadians(yaw);
            this.motionX = -Math.sin(yawRad) * panel.launchSpeed;
            this.motionY = 0;
            this.motionZ = Math.cos(yawRad) * panel.launchSpeed;
        }
    }

    // ==================== POSITION / BOUNDING BOX ====================

    /**
     * Override setPosition to maintain panel-sized bounding box.
     * MC's default setPosition() resets BB based on width/height fields.
     * Network sync calls setPosition(), which would shrink the BB.
     * This ensures melee targeting always works against the full panel surface.
     */
    @Override
    public void setPosition(double x, double y, double z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
        if (panelData != null) {
            float halfW = panelData.panelWidth * 0.5f;
            float halfH = panelData.panelHeight * 0.5f;
            float extent = Math.max(halfW, 0.5f);
            this.boundingBox.setBounds(x - extent, y - halfH, z - extent, x + extent, y + halfH, z + extent);
        } else {
            float f = this.width / 2.0F;
            this.boundingBox.setBounds(x - f, y, z - f, x + f, y + this.height, z + f);
        }
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.prevPanelYaw = this.panelYaw;

        super.onUpdate();

        // Handle charging animation (both sides for smooth rendering)
        if (isCharging()) {
            chargeTick++;
            float progress = getChargeProgress();
            panelData.panelWidth = targetPanelWidth * progress;
            panelData.panelHeight = targetPanelHeight * progress;

            // During charging, held panels still track owner
            if (mode == PanelMode.HELD) {
                updateHeld();
            }
            return; // Don't tick duration/death during charging
        }

        ticksAlive++;

        if (updateBarrierTick()) return;

        // Process entity physics every tick (not during launched mode)
        // Server: solid + knockback for all entities
        // Client: solid prediction for local player only (smooth movement blocking)
        if ((barrierData.solid || barrierData.knockbackEnabled) && mode != PanelMode.LAUNCHED) {
            processEntityPhysics();
        }

        // Mode-specific updates
        switch (mode) {
            case HELD:
                updateHeld();
                break;
            case LAUNCHED:
                updateLaunched();
                break;
            case PLACED:
            default:
                // Stationary, no update needed
                break;
        }
    }

    private void updateHeld() {
        Entity owner = getOwnerEntity();
        if (owner == null) return;

        // Update yaw BEFORE position calc to avoid 1-tick lag
        this.panelYaw = owner.rotationYaw;

        // Follow owner position
        float frontDist = 1.5f;
        float yawRad = (float) Math.toRadians(panelYaw);
        double newX = owner.posX + (-Math.sin(yawRad) * frontDist);
        double newY = owner.posY + panelData.heightOffset + (owner.height * 0.5f);
        double newZ = owner.posZ + (Math.cos(yawRad) * frontDist);

        this.setPosition(newX, newY, newZ);

        // Sync prevPos with owner's prevPos for smooth interpolation
        float prevYawRad = (float) Math.toRadians(owner.prevRotationYaw);
        this.prevPosX = owner.prevPosX + (-Math.sin(prevYawRad) * frontDist);
        this.prevPosY = owner.prevPosY + panelData.heightOffset + (owner.height * 0.5f);
        this.prevPosZ = owner.prevPosZ + (Math.cos(prevYawRad) * frontDist);
    }

    private void updateLaunched() {
        // Move forward on both sides for smooth client rendering
        this.posX += motionX;
        this.posY += motionY;
        this.posZ += motionZ;
        this.setPosition(posX, posY, posZ);

        // Damage and knockback are server-only
        if (!worldObj.isRemote) {
            // Check entity collision for damage
            float halfW = panelData.panelWidth * 0.5f;
            float halfH = panelData.panelHeight * 0.5f;
            float halfD = 0.5f; // Panel collision depth
            float searchRadius = Math.max(halfW, halfD);
            AxisAlignedBB hitBox = AxisAlignedBB.getBoundingBox(
                posX - searchRadius, posY - halfH, posZ - searchRadius,
                posX + searchRadius, posY + halfH, posZ + searchRadius
            );

            @SuppressWarnings("unchecked")
            List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, hitBox);
            Entity owner = getOwnerEntity();

            for (EntityLivingBase target : entities) {
                if (target == owner) continue;
                if (owner instanceof EntityNPCInterface && target instanceof EntityNPCInterface) {
                    if (((EntityNPCInterface) owner).faction.id == ((EntityNPCInterface) target).faction.id) continue;
                }

                // Apply damage
                if (panelData.launchDamage > 0) {
                    if (owner instanceof EntityNPCInterface) {
                        target.attackEntityFrom(new NpcDamageSource("npc_ability", (EntityNPCInterface) owner), panelData.launchDamage);
                    } else if (owner instanceof EntityPlayer) {
                        target.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) owner), panelData.launchDamage);
                    } else {
                        target.attackEntityFrom(DamageSource.generic, panelData.launchDamage);
                    }
                }

                // Apply knockback
                if (panelData.launchKnockback > 0) {
                    double dx = target.posX - posX;
                    double dz = target.posZ - posZ;
                    double len = Math.sqrt(dx * dx + dz * dz);
                    if (len > 0) {
                        target.addVelocity(
                            (dx / len) * panelData.launchKnockback * 0.5,
                            0.1,
                            (dz / len) * panelData.launchKnockback * 0.5
                        );
                        target.velocityChanged = true;
                    }
                }
            }

            // Max lifetime check
            if (ticksAlive > 200) {
                onBarrierDestroyed();
                this.setDead();
            }
        }
    }

    // ==================== INCOMING CHECK ====================

    /**
     * Swept ray-plane intersection test for the panel.
     * Tests if the line segment from prevPos to currPos crosses the panel plane
     * within its width/height bounds. Handles fast projectiles that skip through
     * the thin panel in a single tick.
     *
     * @return true if the segment crosses the panel from either side
     */
    private boolean isIncomingRay(
        double currX, double currY, double currZ,
        double prevX, double prevY, double prevZ,
        int projOwnerEntityId)
    {
        if (isCharging()) return false;
        if (projOwnerEntityId == this.ownerEntityId) return false;

        float halfW = panelData.panelWidth * 0.5f;
        float halfH = panelData.panelHeight * 0.5f;
        float panelThickness = 0.5f;

        float yawRad = (float) Math.toRadians(panelYaw);
        double normalX = -Math.sin(yawRad);
        double normalZ = Math.cos(yawRad);
        double cos = Math.cos(yawRad);
        double sin = Math.sin(yawRad);

        // Signed distance from prevPos and currPos to panel plane
        double relPrevX = prevX - this.posX;
        double relPrevY = prevY - this.posY;
        double relPrevZ = prevZ - this.posZ;
        double prevDist = relPrevX * normalX + relPrevZ * normalZ;

        double relCurrX = currX - this.posX;
        double relCurrY = currY - this.posY;
        double relCurrZ = currZ - this.posZ;
        double currDist = relCurrX * normalX + relCurrZ * normalZ;

        // Check if segment crosses the plane (sign change)
        if (prevDist * currDist > 0) {
            // Both on same side — check if within thickness (slow projectile fallback)
            if (Math.abs(currDist) <= panelThickness) {
                double localRight = relCurrX * cos + relCurrZ * sin;
                if (Math.abs(localRight) > halfW) return false;
                if (Math.abs(relCurrY) > halfH) return false;
                // Check incoming direction
                double motX = currX - prevX;
                double motZ = currZ - prevZ;
                double dot = motX * normalX + motZ * normalZ;
                if (currDist > 0 && dot >= 0) return false;
                if (currDist < 0 && dot <= 0) return false;
                return true;
            }
            return false;
        }

        // Segment crosses plane — compute intersection parameter t
        double rayDirX = currX - prevX;
        double rayDirZ = currZ - prevZ;
        double denom = rayDirX * normalX + rayDirZ * normalZ;
        if (Math.abs(denom) < 1e-10) return false; // Parallel to plane

        double t = -prevDist / denom;
        if (t < 0.0 || t > 1.0) return false;

        // Compute intersection point
        double hitX = prevX + (currX - prevX) * t;
        double hitY = prevY + (currY - prevY) * t;
        double hitZ = prevZ + (currZ - prevZ) * t;

        // Transform hit point to panel-local space and check bounds
        double relHitX = hitX - this.posX;
        double relHitY = hitY - this.posY;
        double relHitZ = hitZ - this.posZ;

        double localRight = relHitX * cos + relHitZ * sin;
        if (Math.abs(localRight) > halfW) return false;
        if (Math.abs(relHitY) > halfH) return false;

        return true;
    }

    @Override
    public boolean isIncomingProjectile(EntityEnergyProjectile projectile) {
        // Faction check: don't block same-faction NPC projectiles
        Entity owner = getOwnerEntity();
        Entity projOwner = projectile.getOwnerEntity();
        if (owner instanceof EntityNPCInterface && projOwner instanceof EntityNPCInterface) {
            if (((EntityNPCInterface) owner).faction.id == ((EntityNPCInterface) projOwner).faction.id) {
                return false;
            }
        }

        double prevX = projectile.posX - projectile.motionX;
        double prevY = projectile.posY - projectile.motionY;
        double prevZ = projectile.posZ - projectile.motionZ;

        return isIncomingRay(
            projectile.posX, projectile.posY, projectile.posZ,
            prevX, prevY, prevZ,
            projectile.getOwnerEntityId());
    }

    @Override
    public boolean isIncomingGenericProjectile(
        double posX, double posY, double posZ,
        double motionX, double motionY, double motionZ,
        double prevPosX, double prevPosY, double prevPosZ,
        int ownerEntityId)
    {
        return isIncomingRay(posX, posY, posZ, prevPosX, prevPosY, prevPosZ, ownerEntityId);
    }

    @Override
    public float getMaxExtent() {
        return Math.max(panelData.panelWidth, panelData.panelHeight) * 0.5f + 1.0f;
    }

    // ==================== CHARGING ====================

    @Override
    public void setupCharging(int duration) {
        this.targetPanelWidth = panelData.panelWidth;
        this.targetPanelHeight = panelData.panelHeight;
        panelData.panelWidth = 0.01f;
        panelData.panelHeight = 0.01f;
        this.chargeDuration = duration;
        this.chargeTick = 0;
        setCharging(true);
    }

    @Override
    public void finishCharging() {
        panelData.panelWidth = targetPanelWidth;
        panelData.panelHeight = targetPanelHeight;
        setCharging(false);
    }

    // ==================== ENTITY PHYSICS (Solid + Knockback) ====================

    /**
     * Processes entity physics for the panel barrier. Two independent systems:
     * <p>
     * SOLID: Hard wall — entities cannot cross the panel plane within its bounds.
     *   Uses reactive crossing detection: compares current side to previous tick
     *   to detect plane crossings, then teleports back and applies velocity correction.
     *   Also preemptively cancels normal velocity when near the plane.
     * <p>
     * KNOCKBACK: Repulsion force — entities near the panel surface get pushed away.
     *   This is a push effect only; entities can still pass through with enough effort.
     *   Does NOT prevent passthrough — use solid for that.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void processEntityPhysics() {
        float halfW = panelData.panelWidth * 0.5f;
        float halfH = panelData.panelHeight * 0.5f;
        float searchExtension = 3.0f;
        boolean solid = barrierData.solid;
        boolean knockback = barrierData.knockbackEnabled;
        float strength = barrierData.knockbackStrength;

        AxisAlignedBB searchBox = AxisAlignedBB.getBoundingBox(
            posX - halfW - searchExtension, posY - halfH - searchExtension, posZ - halfW - searchExtension,
            posX + halfW + searchExtension, posY + halfH + searchExtension, posZ + halfW + searchExtension
        );

        // On client, identify local player for client-side solid prediction
        EntityPlayer localPlayer = worldObj.isRemote ? CustomNpcs.proxy.getPlayer() : null;

        List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, searchBox);
        float yawRad = (float) Math.toRadians(panelYaw);
        float normalX = -(float) Math.sin(yawRad);
        float normalZ = (float) Math.cos(yawRad);

        for (EntityLivingBase ent : entities) {
            if (ent.getEntityId() == ownerEntityId) continue;
            if (isAllyOfOwner(ent)) continue;

            // Client-side: only process local player for solid prediction
            if (worldObj.isRemote && (localPlayer == null || ent != localPlayer)) continue;

            // Current position relative to panel center
            double dx = ent.posX - posX;
            double dy = (ent.posY + ent.height * 0.5) - posY;
            double dz = ent.posZ - posZ;

            // Panel-local coordinates
            float localForward = (float) (dx * normalX + dz * normalZ);
            float localRight = (float) (dx * (-normalZ) + dz * normalX);

            // Skip if outside panel bounds (with margin)
            if (Math.abs(localRight) > halfW + 1.0f) continue;
            if (Math.abs(dy) > halfH + 1.0f) continue;
            if (Math.abs(localForward) > searchExtension) continue;

            // Determine which side the entity is on
            float side = localForward >= 0 ? 1.0f : -1.0f;

            // Check if within panel width/height bounds
            boolean inBounds = Math.abs(localRight) <= halfW && Math.abs(dy) <= halfH;

            // --- SOLID: hard wall preventing plane crossing ---
            if (solid && inBounds) {
                // Previous tick: entity position relative to panel
                double prevDx = ent.prevPosX - prevPosX;
                double prevDz = ent.prevPosZ - prevPosZ;
                float prevLocalForward = (float) (prevDx * normalX + prevDz * normalZ);
                float prevSide = prevLocalForward >= 0 ? 1.0f : -1.0f;

                // Normal velocity component (positive = moving in normal direction)
                double normalVel = ent.motionX * normalX + ent.motionZ * normalZ;

                if (prevSide != side) {
                    // Entity crossed the panel plane this tick — push back to original side
                    double pushDist = 0.3;
                    double newX = posX + normalX * prevSide * pushDist + (localRight * (-normalZ));
                    double newZ = posZ + normalZ * prevSide * pushDist + (localRight * normalX);
                    teleportEntity(ent, newX, ent.posY, newZ);

                    // Cancel normal velocity and add corrective push
                    ent.motionX -= normalVel * normalX;
                    ent.motionZ -= normalVel * normalZ;
                    ent.motionX += normalX * prevSide * 0.15;
                    ent.motionZ += normalZ * prevSide * 0.15;
                    ent.velocityChanged = true;
                } else {
                    // Preemptive: near the plane and moving toward it — cancel normal velocity
                    double absDist = Math.abs(localForward);
                    if (absDist < 1.5) {
                        boolean movingToward = (side > 0 && normalVel < -0.01)
                            || (side < 0 && normalVel > 0.01);
                        if (movingToward) {
                            ent.motionX -= normalVel * normalX;
                            ent.motionZ -= normalVel * normalZ;
                            ent.motionX += normalX * side * 0.05;
                            ent.motionZ += normalZ * side * 0.05;
                            ent.velocityChanged = true;
                        }
                    }
                }
            }

            // --- KNOCKBACK: repulsion push (server only, synced via velocityChanged) ---
            if (!worldObj.isRemote && knockback && inBounds) {
                double absDist = Math.abs(localForward);
                if (absDist < searchExtension) {
                    double proximity = 1.0 - (absDist / searchExtension);
                    double force = proximity * strength * 0.06;
                    ent.motionX += normalX * side * force;
                    ent.motionZ += normalZ * side * force;
                    ent.velocityChanged = true;
                }
            }
        }
    }

    // ==================== RENDER ====================

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        double d = Math.max(panelData.panelWidth, panelData.panelHeight) * 8.0D;
        d *= 64.0D;
        return distance < d * d;
    }

    // ==================== DISTANCE (for render sorting) ====================

    /**
     * Returns squared distance from the given point to the closest point on the panel surface.
     * Accounts for panel rotation (panelYaw), width, height, and thickness.
     * This ensures correct transparency render ordering when the camera is near or behind the panel.
     */
    @Override
    public double getDistanceSq(double x, double y, double z) {
        double dx = x - this.posX;
        double dy = y - this.posY;
        double dz = z - this.posZ;

        float yawRad = (float) Math.toRadians(panelYaw);
        double cos = Math.cos(yawRad);
        double sin = Math.sin(yawRad);

        // Transform to panel-local space
        // Panel normal (forward): (-sin, 0, cos)
        // Panel right (width axis): (cos, 0, sin)
        double localForward = dx * (-sin) + dz * cos;
        double localRight = dx * cos + dz * sin;
        double localUp = dy;

        float halfW = panelData.panelWidth * 0.5f;
        float halfH = panelData.panelHeight * 0.5f;
        float halfThickness = 0.25f;

        // Clamp to panel bounds to find closest point on the panel volume
        double clampedForward = Math.max(-halfThickness, Math.min(halfThickness, localForward));
        double clampedRight = Math.max(-halfW, Math.min(halfW, localRight));
        double clampedUp = Math.max(-halfH, Math.min(halfH, localUp));

        // Distance from point to closest point on panel
        double df = localForward - clampedForward;
        double dr = localRight - clampedRight;
        double du = localUp - clampedUp;

        return df * df + dr * dr + du * du;
    }

    // ==================== GETTERS ====================

    public PanelMode getMode() {
        return mode;
    }

    public float getPanelYaw() {
        return panelYaw;
    }

    public float getPrevPanelYaw() {
        return prevPanelYaw;
    }

    public EnergyPanelData getPanelData() {
        return panelData;
    }

    // ==================== NBT ====================

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        // Intentionally empty — ability entities are transient (not saved to world)
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        // Intentionally empty — ability entities are transient (not saved to world)
    }

    @Override
    protected void writeSpawnNBT(NBTTagCompound nbt) {
        writeBarrierBaseNBT(nbt);
        nbt.setFloat("PanelYaw", panelYaw);
        nbt.setInteger("PanelMode", mode.ordinal());
        nbt.setFloat("TargetPanelWidth", targetPanelWidth);
        nbt.setFloat("TargetPanelHeight", targetPanelHeight);
        panelData.writeNBT(nbt);
    }

    @Override
    protected void readSpawnNBT(NBTTagCompound nbt) {
        readBarrierBaseNBT(nbt);
        this.panelYaw = nbt.getFloat("PanelYaw");
        int modeOrdinal = nbt.getInteger("PanelMode");
        this.mode = (modeOrdinal >= 0 && modeOrdinal < PanelMode.values().length)
            ? PanelMode.values()[modeOrdinal] : PanelMode.PLACED;
        panelData.readNBT(nbt);
        panelData.panelWidth = sanitize(panelData.panelWidth, 3.0f, MAX_ENTITY_SIZE);
        panelData.panelHeight = sanitize(panelData.panelHeight, 3.0f, MAX_ENTITY_SIZE);
        this.targetPanelWidth = sanitize(nbt.hasKey("TargetPanelWidth") ? nbt.getFloat("TargetPanelWidth") : panelData.panelWidth, 3.0f, MAX_ENTITY_SIZE);
        this.targetPanelHeight = sanitize(nbt.hasKey("TargetPanelHeight") ? nbt.getFloat("TargetPanelHeight") : panelData.panelHeight, 3.0f, MAX_ENTITY_SIZE);
    }
}
