package kamkeel.npcs.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import noppes.npcs.LogWriter;
import noppes.npcs.entity.EntityNPCInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Beam projectile - head with trailing path that curves with homing.
 * Stays attached to origin point, trail shows path of head.
 *
 * IMPORTANT: Trail points are stored RELATIVE to startX/Y/Z (origin).
 * This ensures rendering is stable regardless of entity position interpolation.
 *
 * Design inspired by LouisXIV's energy attack system.
 */
public class EntityAbilityBeam extends EntityAbilityProjectile {

    // Beam-specific movement properties
    private float speed = 0.5f;
    private boolean homing = true;
    private float homingStrength = 0.35f;  // Increased from 0.15 for better tracking
    private float homingRange = 20.0f;

    // Beam shape properties
    private float beamWidth = 0.3f;
    private float headSize = 0.5f;

    // Trail - list of points showing beam path (RELATIVE to origin!)
    private List<Vec3> trailPoints = new ArrayList<>();
    private static final int MAX_TRAIL_POINTS = 200;
    private static final double MIN_POINT_DISTANCE = 0.2;

    // Head position relative to origin
    private double headOffsetX, headOffsetY, headOffsetZ;
    private double prevHeadOffsetX, prevHeadOffsetY, prevHeadOffsetZ;

    // Origin stays fixed (or follows owner)
    private boolean attachedToOwner = true;

    // Debug logging
    private static final boolean DEBUG_LOGGING = true;

    public EntityAbilityBeam(World world) {
        super(world);
    }

    /**
     * Full constructor with all parameters.
     */
    public EntityAbilityBeam(World world, EntityNPCInterface owner, EntityLivingBase target,
                              double x, double y, double z,
                              float beamWidth, float headSize, int innerColor, int outerColor, float rotationSpeed,
                              float damage, float knockback, float knockbackUp,
                              float speed, boolean homing, float homingStrength, float homingRange,
                              boolean explosive, float explosionRadius, float explosionDamageFalloff,
                              int stunDuration, int slowDuration, int slowLevel,
                              float maxDistance, int maxLifetime) {
        super(world);

        // Initialize base properties
        initProjectile(owner, target, x, y, z,
            headSize, innerColor, outerColor, rotationSpeed,
            damage, knockback, knockbackUp,
            explosive, explosionRadius, explosionDamageFalloff,
            stunDuration, slowDuration, slowLevel,
            maxDistance, maxLifetime);

        // Beam-specific properties
        this.speed = speed;
        this.homing = homing;
        this.homingStrength = homingStrength;
        this.homingRange = homingRange;
        this.beamWidth = beamWidth;
        this.headSize = headSize;

        // Initialize head offset at origin (0,0,0 relative)
        this.headOffsetX = 0;
        this.headOffsetY = 0;
        this.headOffsetZ = 0;
        this.prevHeadOffsetX = 0;
        this.prevHeadOffsetY = 0;
        this.prevHeadOffsetZ = 0;

        // Add initial trail point at origin (relative 0,0,0)
        trailPoints.add(Vec3.createVectorHelper(0, 0, 0));

        if (DEBUG_LOGGING && !world.isRemote) {
            LogWriter.info("[Beam] Created at origin " + x + ", " + y + ", " + z + " maxDist=" + maxDistance);
        }

        // Calculate initial velocity toward target
        if (target != null) {
            double dx = target.posX - x;
            double dy = (target.posY + target.height * 0.5) - y;
            double dz = target.posZ - z;
            double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (len > 0) {
                this.motionX = (dx / len) * speed;
                this.motionY = (dy / len) * speed;
                this.motionZ = (dz / len) * speed;
            }
        } else {
            float yaw = (float) Math.toRadians(owner.rotationYaw);
            float pitch = (float) Math.toRadians(owner.rotationPitch);
            this.motionX = -Math.sin(yaw) * Math.cos(pitch) * speed;
            this.motionY = -Math.sin(pitch) * speed;
            this.motionZ = Math.cos(yaw) * Math.cos(pitch) * speed;
        }
    }

    @Override
    protected boolean checkMaxDistance() {
        // Check distance using head offset (distance from origin)
        double distFromOrigin = Math.sqrt(
            headOffsetX * headOffsetX +
            headOffsetY * headOffsetY +
            headOffsetZ * headOffsetZ
        );
        boolean exceeded = distFromOrigin >= maxDistance;
        if (exceeded && !worldObj.isRemote && DEBUG_LOGGING) {
            LogWriter.info("[Beam] DEAD: Max distance exceeded. dist=" + distFromOrigin + " max=" + maxDistance);
        }
        return exceeded;
    }

    @Override
    protected void updateProjectile() {
        // Store previous head offset for interpolation
        prevHeadOffsetX = headOffsetX;
        prevHeadOffsetY = headOffsetY;
        prevHeadOffsetZ = headOffsetZ;

        if (worldObj.isRemote) {
            // Client: interpolate entity position, derive head from offset
            handleClientInterpolation();

            // Client also updates head offset to match motion for smooth movement
            headOffsetX += motionX;
            headOffsetY += motionY;
            headOffsetZ += motionZ;

            // Add trail point on client
            addTrailPoint();
        } else {
            // Server-side logic
            updateHoming();

            // Move head offset
            headOffsetX += motionX;
            headOffsetY += motionY;
            headOffsetZ += motionZ;

            // Update origin if attached to owner
            if (attachedToOwner) {
                Entity owner = getOwner();
                if (owner != null) {
                    startX = owner.posX;
                    startY = owner.posY + owner.height * 0.7;
                    startZ = owner.posZ;
                }
            }

            // Calculate world position of head for collision detection
            double headWorldX = startX + headOffsetX;
            double headWorldY = startY + headOffsetY;
            double headWorldZ = startZ + headOffsetZ;

            // Update entity position to head (for network sync)
            this.setPosition(headWorldX, headWorldY, headWorldZ);

            // Add trail point
            addTrailPoint();

            // Debug log periodically
            if (DEBUG_LOGGING && ticksExisted % 20 == 0) {
                LogWriter.info("[Beam] Server tick=" + ticksExisted + " headOffset=(" +
                    String.format("%.2f", headOffsetX) + "," +
                    String.format("%.2f", headOffsetY) + "," +
                    String.format("%.2f", headOffsetZ) + ") origin=(" +
                    String.format("%.2f", startX) + "," +
                    String.format("%.2f", startY) + "," +
                    String.format("%.2f", startZ) + ") trail=" + trailPoints.size());
            }

            // Skip collision checks on first few ticks
            if (ticksExisted > 2) {
                checkBlockCollision(headWorldX, headWorldY, headWorldZ);
                checkEntityCollision(headWorldX, headWorldY, headWorldZ);
            }
        }
    }

    private void addTrailPoint() {
        // Trail points are RELATIVE to origin
        if (trailPoints.isEmpty()) {
            trailPoints.add(Vec3.createVectorHelper(headOffsetX, headOffsetY, headOffsetZ));
            return;
        }

        Vec3 lastPoint = trailPoints.get(trailPoints.size() - 1);
        double dist = Math.sqrt(
            (headOffsetX - lastPoint.xCoord) * (headOffsetX - lastPoint.xCoord) +
            (headOffsetY - lastPoint.yCoord) * (headOffsetY - lastPoint.yCoord) +
            (headOffsetZ - lastPoint.zCoord) * (headOffsetZ - lastPoint.zCoord)
        );

        if (dist >= MIN_POINT_DISTANCE) {
            trailPoints.add(Vec3.createVectorHelper(headOffsetX, headOffsetY, headOffsetZ));

            // Limit trail length
            while (trailPoints.size() > MAX_TRAIL_POINTS) {
                trailPoints.remove(0);
            }
        }
    }

    private void updateHoming() {
        if (!homing) return;

        Entity target = getTarget();
        if (target == null || !target.isEntityAlive()) return;

        // Calculate world position of head
        double headWorldX = startX + headOffsetX;
        double headWorldY = startY + headOffsetY;
        double headWorldZ = startZ + headOffsetZ;

        double dx = target.posX - headWorldX;
        double dy = (target.posY + target.height * 0.5) - headWorldY;
        double dz = target.posZ - headWorldZ;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (dist <= homingRange && dist > 0) {
            // Calculate effective homing strength - increases when closer to commit to target
            float effectiveStrength = homingStrength;
            if (dist < homingRange * 0.3) {
                effectiveStrength = Math.min(1.0f, homingStrength * 2.5f);
            } else if (dist < homingRange * 0.6) {
                effectiveStrength = Math.min(0.8f, homingStrength * 1.5f);
            }

            double desiredVX = (dx / dist) * speed;
            double desiredVY = (dy / dist) * speed;
            double desiredVZ = (dz / dist) * speed;

            motionX += (desiredVX - motionX) * effectiveStrength;
            motionY += (desiredVY - motionY) * effectiveStrength;
            motionZ += (desiredVZ - motionZ) * effectiveStrength;

            double vLen = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
            if (vLen > 0) {
                motionX = (motionX / vLen) * speed;
                motionY = (motionY / vLen) * speed;
                motionZ = (motionZ / vLen) * speed;
            }
        }
    }

    private void checkBlockCollision(double headX, double headY, double headZ) {
        // Previous head world position
        double prevHeadWorldX = startX + prevHeadOffsetX;
        double prevHeadWorldY = startY + prevHeadOffsetY;
        double prevHeadWorldZ = startZ + prevHeadOffsetZ;

        Vec3 currentPos = Vec3.createVectorHelper(prevHeadWorldX, prevHeadWorldY, prevHeadWorldZ);
        Vec3 nextPos = Vec3.createVectorHelper(headX, headY, headZ);
        MovingObjectPosition blockHit = worldObj.rayTraceBlocks(currentPos, nextPos);

        if (blockHit != null && blockHit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            if (DEBUG_LOGGING) {
                LogWriter.info("[Beam] DEAD: Block collision at tick " + ticksExisted);
            }
            hasHit = true;
            if (explosive) {
                posX = blockHit.hitVec.xCoord;
                posY = blockHit.hitVec.yCoord;
                posZ = blockHit.hitVec.zCoord;
                doExplosion();
            }
            this.setDead();
        }
    }

    private void checkEntityCollision(double headX, double headY, double headZ) {
        double hitSize = headSize * 0.5;
        AxisAlignedBB hitBox = AxisAlignedBB.getBoundingBox(
            headX - hitSize, headY - hitSize, headZ - hitSize,
            headX + hitSize, headY + hitSize, headZ + hitSize
        );

        @SuppressWarnings("unchecked")
        List<EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class, hitBox);

        for (EntityLivingBase entity : entities) {
            if (shouldIgnoreEntity(entity)) continue;

            if (DEBUG_LOGGING) {
                LogWriter.info("[Beam] DEAD: Entity collision with " + entity.getClass().getSimpleName() + " at tick " + ticksExisted);
            }
            hasHit = true;

            if (explosive) {
                posX = headX;
                posY = headY;
                posZ = headZ;
                doExplosion();
            } else {
                applyDamage(entity);
            }

            this.setDead();
            return;
        }
    }

    // ==================== GETTERS FOR RENDERER ====================

    public float getBeamWidth() {
        return beamWidth;
    }

    public float getHeadSize() {
        return headSize;
    }

    /**
     * Get interpolated head offset for smooth rendering.
     * Returns offset RELATIVE to origin.
     */
    public double getInterpolatedHeadOffsetX(float partialTicks) {
        return prevHeadOffsetX + (headOffsetX - prevHeadOffsetX) * partialTicks;
    }

    public double getInterpolatedHeadOffsetY(float partialTicks) {
        return prevHeadOffsetY + (headOffsetY - prevHeadOffsetY) * partialTicks;
    }

    public double getInterpolatedHeadOffsetZ(float partialTicks) {
        return prevHeadOffsetZ + (headOffsetZ - prevHeadOffsetZ) * partialTicks;
    }

    /**
     * Get trail points. These are RELATIVE to origin (startX/Y/Z).
     */
    public List<Vec3> getTrailPoints() {
        return trailPoints;
    }

    public double getOriginX() {
        return startX;
    }

    public double getOriginY() {
        return startY;
    }

    public double getOriginZ() {
        return startZ;
    }

    // ==================== NBT ====================

    @Override
    protected void readProjectileNBT(NBTTagCompound nbt) {
        this.speed = nbt.hasKey("Speed") ? nbt.getFloat("Speed") : 0.5f;
        this.homing = !nbt.hasKey("Homing") || nbt.getBoolean("Homing");
        this.homingStrength = nbt.hasKey("HomingStrength") ? nbt.getFloat("HomingStrength") : 0.15f;
        this.homingRange = nbt.hasKey("HomingRange") ? nbt.getFloat("HomingRange") : 20.0f;
        this.beamWidth = nbt.hasKey("BeamWidth") ? nbt.getFloat("BeamWidth") : 0.3f;
        this.headSize = nbt.hasKey("HeadSize") ? nbt.getFloat("HeadSize") : 0.5f;
        this.headOffsetX = nbt.hasKey("HeadOffsetX") ? nbt.getDouble("HeadOffsetX") : 0;
        this.headOffsetY = nbt.hasKey("HeadOffsetY") ? nbt.getDouble("HeadOffsetY") : 0;
        this.headOffsetZ = nbt.hasKey("HeadOffsetZ") ? nbt.getDouble("HeadOffsetZ") : 0;
        this.prevHeadOffsetX = this.headOffsetX;
        this.prevHeadOffsetY = this.headOffsetY;
        this.prevHeadOffsetZ = this.headOffsetZ;
        this.attachedToOwner = !nbt.hasKey("AttachedToOwner") || nbt.getBoolean("AttachedToOwner");

        // Read trail points (relative to origin)
        trailPoints.clear();
        if (nbt.hasKey("Trail")) {
            NBTTagList trailList = nbt.getTagList("Trail", 10);
            for (int i = 0; i < trailList.tagCount(); i++) {
                NBTTagCompound point = trailList.getCompoundTagAt(i);
                trailPoints.add(Vec3.createVectorHelper(
                    point.getDouble("X"),
                    point.getDouble("Y"),
                    point.getDouble("Z")
                ));
            }
        }
    }

    @Override
    protected void writeProjectileNBT(NBTTagCompound nbt) {
        nbt.setFloat("Speed", speed);
        nbt.setBoolean("Homing", homing);
        nbt.setFloat("HomingStrength", homingStrength);
        nbt.setFloat("HomingRange", homingRange);
        nbt.setFloat("BeamWidth", beamWidth);
        nbt.setFloat("HeadSize", headSize);
        nbt.setDouble("HeadOffsetX", headOffsetX);
        nbt.setDouble("HeadOffsetY", headOffsetY);
        nbt.setDouble("HeadOffsetZ", headOffsetZ);
        nbt.setBoolean("AttachedToOwner", attachedToOwner);

        // Write trail points
        NBTTagList trailList = new NBTTagList();
        for (Vec3 point : trailPoints) {
            NBTTagCompound pointNbt = new NBTTagCompound();
            pointNbt.setDouble("X", point.xCoord);
            pointNbt.setDouble("Y", point.yCoord);
            pointNbt.setDouble("Z", point.zCoord);
            trailList.appendTag(pointNbt);
        }
        nbt.setTag("Trail", trailList);
    }
}
